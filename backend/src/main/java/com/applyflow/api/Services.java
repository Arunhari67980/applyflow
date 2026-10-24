package com.applyflow.api;

import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
class AuthService {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
  AuthService(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
  AuthResponse register(RegisterRequest r){if(users.findByEmailIgnoreCase(r.email()).isPresent())throw new ConflictException("EMAIL_EXISTS","An account already uses this email"); User u=users.save(new User(r.name().trim(),r.email().trim().toLowerCase(),encoder.encode(r.password())));return new AuthResponse(jwt.create(u.id,u.email),u.id,u.name,u.email);}
  AuthResponse login(LoginRequest r){User u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new UnauthorizedException("INVALID_CREDENTIALS","Email or password is incorrect"));if(!encoder.matches(r.password(),u.passwordHash))throw new UnauthorizedException("INVALID_CREDENTIALS","Email or password is incorrect");return new AuthResponse(jwt.create(u.id,u.email),u.id,u.name,u.email);}
}

@Service
class ApplicationService {
  private final ApplicationRepository apps; private final UserRepository users; private final StatusHistoryRepository history;
  ApplicationService(ApplicationRepository a,UserRepository u,StatusHistoryRepository h){apps=a;users=u;history=h;}
  Page<ApplicationResponse> list(Pageable page){return apps.findByUserId(CurrentUser.id(),page).map(ApplicationResponse::from);}
  @Transactional ApplicationResponse create(ApplicationRequest r){User u=users.getReferenceById(CurrentUser.id());var a=new JobApplication(u,r.companyName().trim(),r.jobTitle().trim(),r.source(),r.jobType(),r.workMode(),r.appliedDate(),r.priority());copy(a,r);return ApplicationResponse.from(apps.save(a));}
  @Transactional ApplicationResponse update(UUID id,ApplicationRequest r){var a=owned(id);var previous=a.status;copy(a,r);if(r.status()!=null&&previous!=a.status)history.save(new StatusHistory(a,previous,a.status,r.notes()));a.updatedAt=Instant.now();return ApplicationResponse.from(a);}
  @Transactional void delete(UUID id){apps.delete(owned(id));}
  List<HistoryResponse> timeline(UUID id){owned(id);return history.findByApplicationIdOrderByChangedAtAsc(id).stream().map(h->new HistoryResponse(h.id,h.previousStatus,h.newStatus,h.note,h.changedAt)).toList();}
  String csv(){StringBuilder out=new StringBuilder("company,job_title,status,source,applied_date,priority\n");for(var a:apps.findByUserIdOrderByAppliedDateDesc(CurrentUser.id()))out.append(csv(a.companyName)).append(',').append(csv(a.jobTitle)).append(',').append(a.status).append(',').append(a.source).append(',').append(a.appliedDate).append(',').append(a.priority).append('\n');return out.toString();}
  private String csv(String value){return "\"" + value.replace("\"","\"\"") + "\"";}
  private JobApplication owned(UUID id){return apps.findByIdAndUserId(id,CurrentUser.id()).orElseThrow(()->new NotFoundException("APPLICATION_NOT_FOUND","Application not found"));}
  private void copy(JobApplication a,ApplicationRequest r){a.companyName=r.companyName().trim();a.jobTitle=r.jobTitle().trim();a.description=r.description();a.source=r.source();a.jobType=r.jobType();a.workMode=r.workMode();a.location=r.location();a.postingUrl=r.postingUrl();a.salaryRange=r.salaryRange();a.appliedDate=r.appliedDate();a.priority=r.priority();a.nextFollowUpDate=r.nextFollowUpDate();a.notes=r.notes();if(r.status()!=null)a.status=r.status();}
}

@Service
class DashboardService {
  private final ApplicationRepository apps; private final InterviewRepository interviews; private final FollowUpRepository follows;
  DashboardService(ApplicationRepository a,InterviewRepository i,FollowUpRepository f){apps=a;interviews=i;follows=f;}
  DashboardResponse get(){UUID id=CurrentUser.id();long total=apps.countByUserId(id);var map=new EnumMap<ApplicationStatus,Long>(ApplicationStatus.class);for(var s:ApplicationStatus.values())map.put(s,apps.countByUserIdAndStatus(id,s));LocalDate today=LocalDate.now();long last7=apps.countByUserIdAndAppliedDateAfter(id,today.minusDays(7));long last30=apps.countByUserIdAndAppliedDateAfter(id,today.minusDays(30));long upcoming=interviews.findByUserIdAndScheduledAtAfterAndCancelledFalseOrderByScheduledAtAsc(id,Instant.now()).size();var fs=follows.findByUserIdOrderByDueDateAsc(id);long pending=fs.stream().filter(f->!f.completed).count(), overdue=fs.stream().filter(f->!f.completed&&f.dueDate.isBefore(today)).count();return new DashboardResponse(total,last7,last30,map,upcoming,pending,overdue,total==0?0:(double)map.get(ApplicationStatus.INTERVIEW)/total,total==0?0:(double)map.get(ApplicationStatus.OFFER)/total);}
}

@Service
class ReminderScheduler {
  private final FollowUpRepository follows; private final NotificationRepository notifications;
  ReminderScheduler(FollowUpRepository f,NotificationRepository n){follows=f;notifications=n;}
  @Scheduled(cron="${app.reminders.cron:0 0 9 * * *}") @Transactional void createDueReminders(){for(var f:follows.findByDueDateLessThanEqualAndCompletedFalse(LocalDate.now()))if(!notifications.existsByFollowUpId(f.id)){var n=new Notification();n.id=UUID.randomUUID();n.user=f.user;n.followUp=f;n.message="Follow-up due: "+f.type;n.createdAt=Instant.now();notifications.save(n);}}
}
