package com.applyflow.api;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

enum ApplicationStatus { SAVED, APPLIED, ASSESSMENT, INTERVIEW, OFFER, REJECTED, WITHDRAWN }
enum ApplicationSource { LINKEDIN, NAUKRI, COMPANY_WEBSITE, REFERRAL, OTHER }
enum JobType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }
enum WorkMode { REMOTE, HYBRID, ONSITE }
enum Priority { LOW, MEDIUM, HIGH }
enum FollowUpType { EMAIL, CALL, LINKEDIN_MESSAGE, OTHER }
enum InterviewType { PHONE, VIDEO, ONSITE, OTHER }

@Entity @Table(name="app_user")
class User {
  @Id UUID id; @Column(nullable=false) String name; @Column(nullable=false, unique=true) String email;
  @Column(name="password_hash", nullable=false) String passwordHash; @Column(name="created_at", nullable=false) Instant createdAt;
  protected User() {}
  User(String name,String email,String passwordHash){this.id=UUID.randomUUID();this.name=name;this.email=email;this.passwordHash=passwordHash;this.createdAt=Instant.now();}
}

@Entity @Table(name="job_application")
class JobApplication {
  @Id UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
  @Column(name="company_name",nullable=false) String companyName; @Column(name="job_title",nullable=false) String jobTitle;
  @Column(columnDefinition="TEXT") String description; @Enumerated(EnumType.STRING) ApplicationSource source;
  @Enumerated(EnumType.STRING) JobType jobType; @Enumerated(EnumType.STRING) WorkMode workMode; String location;
  @Column(name="posting_url") String postingUrl; @Column(name="salary_range") String salaryRange;
  @Column(name="applied_date") LocalDate appliedDate; @Enumerated(EnumType.STRING) ApplicationStatus status;
  @Enumerated(EnumType.STRING) Priority priority; LocalDate nextFollowUpDate; @Column(columnDefinition="TEXT") String notes;
  Instant createdAt; Instant updatedAt;
  protected JobApplication() {}
  JobApplication(User u, String company, String title, ApplicationSource source, JobType type, WorkMode mode, LocalDate applied, Priority priority) {
    id=UUID.randomUUID(); user=u; companyName=company; jobTitle=title; this.source=source; jobType=type; workMode=mode; appliedDate=applied; status=ApplicationStatus.APPLIED; this.priority=priority; createdAt=Instant.now(); updatedAt=createdAt;
  }
}

@Entity @Table(name="status_history")
class StatusHistory {
  @Id UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="application_id",nullable=false) JobApplication application;
  @Enumerated(EnumType.STRING) ApplicationStatus previousStatus; @Enumerated(EnumType.STRING) ApplicationStatus newStatus; String note; Instant changedAt;
  protected StatusHistory() {}
  StatusHistory(JobApplication a, ApplicationStatus previous, ApplicationStatus next, String note){id=UUID.randomUUID();application=a;previousStatus=previous;newStatus=next;this.note=note;changedAt=Instant.now();}
}

@Entity @Table(name="follow_up")
class FollowUp {
  @Id UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="application_id") JobApplication application;
  LocalDate dueDate; @Enumerated(EnumType.STRING) FollowUpType type; String notes; boolean completed; Instant createdAt;
  protected FollowUp() {}
}

@Entity @Table(name="interview")
class Interview {
  @Id UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="application_id") JobApplication application;
  Instant scheduledAt; String roundName; @Enumerated(EnumType.STRING) InterviewType type; String meetingLink; String notes; boolean cancelled; Instant createdAt;
  protected Interview() {}
}

@Entity @Table(name="notification")
class Notification {
  @Id UUID id; @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) User user;
  @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="follow_up_id",unique=true) FollowUp followUp; String message; Instant readAt; Instant createdAt;
  protected Notification() {}
}
