package com.applyflow.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/auth")
class AuthController {
  private final AuthService service; AuthController(AuthService s){service=s;}
  @PostMapping("/register") AuthResponse register(@Valid @RequestBody RegisterRequest r){return service.register(r);}
  @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest r){return service.login(r);}
}

@RestController @RequestMapping("/api/v1/applications")
class ApplicationController {
  private final ApplicationService service; ApplicationController(ApplicationService s){service=s;}
  @GetMapping Page<ApplicationResponse> list(@PageableDefault(size=20,sort="appliedDate",direction=Sort.Direction.DESC) Pageable page){return service.list(page);}
  @PostMapping ApplicationResponse create(@Valid @RequestBody ApplicationRequest request){return service.create(request);}
  @PutMapping("/{id}") ApplicationResponse update(@PathVariable UUID id,@Valid @RequestBody ApplicationRequest request){return service.update(id,request);}
  @DeleteMapping("/{id}") void delete(@PathVariable UUID id){service.delete(id);}
  @GetMapping(value="/export",produces="text/csv") @ResponseBody String export(){return service.csv();}
  @GetMapping("/{id}/history") List<HistoryResponse> history(@PathVariable UUID id){return service.timeline(id);}
}

@RestController @RequestMapping("/api/v1/dashboard")
class DashboardController { private final DashboardService service; DashboardController(DashboardService s){service=s;} @GetMapping DashboardResponse get(){return service.get();} }

@RestController @RequestMapping("/api/v1/follow-ups")
class FollowUpController {
  private final FollowUpRepository follows; private final UserRepository users; private final ApplicationRepository apps;
  FollowUpController(FollowUpRepository f,UserRepository u,ApplicationRepository a){follows=f;users=u;apps=a;}
  @GetMapping List<FollowUpView> list(){return follows.findByUserIdOrderByDueDateAsc(CurrentUser.id()).stream().map(FollowUpView::from).toList();}
  @PostMapping FollowUpView create(@Valid @RequestBody FollowUpRequest r){var f=new FollowUp();f.id=UUID.randomUUID();f.user=users.getReferenceById(CurrentUser.id());f.dueDate=r.dueDate();f.type=r.type();f.notes=r.notes();f.createdAt=Instant.now();if(r.applicationId()!=null)f.application=apps.findByIdAndUserId(r.applicationId(),CurrentUser.id()).orElseThrow(()->new NotFoundException("APPLICATION_NOT_FOUND","Application not found"));return FollowUpView.from(follows.save(f));}
  @PatchMapping("/{id}/complete") FollowUpView complete(@PathVariable UUID id){var f=follows.findByIdAndUserId(id,CurrentUser.id()).orElseThrow(()->new NotFoundException("FOLLOW_UP_NOT_FOUND","Follow-up not found"));f.completed=true;return FollowUpView.from(follows.save(f));}
}
record FollowUpView(UUID id,UUID applicationId,LocalDate dueDate,FollowUpType type,String notes,boolean completed){static FollowUpView from(FollowUp f){return new FollowUpView(f.id,f.application==null?null:f.application.id,f.dueDate,f.type,f.notes,f.completed);}}

@RestController @RequestMapping("/api/v1/interviews")
class InterviewController {
  private final InterviewRepository interviews; private final UserRepository users; private final ApplicationRepository apps;
  InterviewController(InterviewRepository i,UserRepository u,ApplicationRepository a){interviews=i;users=u;apps=a;}
  @GetMapping List<InterviewView> upcoming(){return interviews.findByUserIdAndScheduledAtAfterAndCancelledFalseOrderByScheduledAtAsc(CurrentUser.id(),Instant.now()).stream().map(InterviewView::from).toList();}
  @PostMapping InterviewView create(@Valid @RequestBody InterviewRequest r){var i=new Interview();i.id=UUID.randomUUID();i.user=users.getReferenceById(CurrentUser.id());i.scheduledAt=r.scheduledAt();i.roundName=r.roundName();i.type=r.type();i.meetingLink=r.meetingLink();i.notes=r.notes();i.createdAt=Instant.now();if(r.applicationId()!=null)i.application=apps.findByIdAndUserId(r.applicationId(),CurrentUser.id()).orElseThrow(()->new NotFoundException("APPLICATION_NOT_FOUND","Application not found"));return InterviewView.from(interviews.save(i));}
  @PatchMapping("/{id}/cancel") InterviewView cancel(@PathVariable UUID id){var i=interviews.findByIdAndUserId(id,CurrentUser.id()).orElseThrow(()->new NotFoundException("INTERVIEW_NOT_FOUND","Interview not found"));i.cancelled=true;return InterviewView.from(interviews.save(i));}
}
record InterviewView(UUID id,UUID applicationId,Instant scheduledAt,String roundName,InterviewType type,String meetingLink,String notes,boolean cancelled){static InterviewView from(Interview i){return new InterviewView(i.id,i.application==null?null:i.application.id,i.scheduledAt,i.roundName,i.type,i.meetingLink,i.notes,i.cancelled);}}

@RestController @RequestMapping("/api/v1/notifications")
class NotificationController {
  private final NotificationRepository notifications;
  NotificationController(NotificationRepository n){notifications=n;}
  @GetMapping List<NotificationView> list(){return notifications.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(CurrentUser.id()).stream().map(NotificationView::from).toList();}
  @PatchMapping("/{id}/read") NotificationView read(@PathVariable UUID id){
    var notification=notifications.findByIdAndUserId(id,CurrentUser.id()).orElseThrow(()->new NotFoundException("NOTIFICATION_NOT_FOUND","Notification not found"));
    notification.readAt=Instant.now();
    return NotificationView.from(notifications.save(notification));
  }
}
record NotificationView(UUID id,String message,Instant createdAt,Instant readAt){
  static NotificationView from(Notification n){return new NotificationView(n.id,n.message,n.createdAt,n.readAt);}
}
