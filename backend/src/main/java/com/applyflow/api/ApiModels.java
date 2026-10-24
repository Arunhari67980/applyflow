package com.applyflow.api;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

record RegisterRequest(@NotBlank @Size(max=120) String name,@NotBlank @Email String email,@NotBlank @Size(min=8,max=72) String password) {}
record LoginRequest(@NotBlank @Email String email,@NotBlank String password) {}
record AuthResponse(String token, UUID userId, String name, String email) {}
record ApplicationRequest(@NotBlank @Size(max=160) String companyName,@NotBlank @Size(max=200) String jobTitle,
  String description,@NotNull ApplicationSource source,@NotNull JobType jobType,@NotNull WorkMode workMode,String location,
  @Size(max=500) String postingUrl,String salaryRange,@NotNull LocalDate appliedDate,@NotNull Priority priority,
  LocalDate nextFollowUpDate,String notes,ApplicationStatus status) {}
record ApplicationResponse(UUID id,String companyName,String jobTitle,String description,ApplicationSource source,JobType jobType,
  WorkMode workMode,String location,String postingUrl,String salaryRange,LocalDate appliedDate,ApplicationStatus status,Priority priority,
  LocalDate nextFollowUpDate,String notes,Instant createdAt,Instant updatedAt) {
  static ApplicationResponse from(JobApplication a){return new ApplicationResponse(a.id,a.companyName,a.jobTitle,a.description,a.source,a.jobType,a.workMode,a.location,a.postingUrl,a.salaryRange,a.appliedDate,a.status,a.priority,a.nextFollowUpDate,a.notes,a.createdAt,a.updatedAt);}
}
record HistoryResponse(UUID id,ApplicationStatus previousStatus,ApplicationStatus newStatus,String note,Instant changedAt) {}
record FollowUpRequest(@NotNull LocalDate dueDate,@NotNull FollowUpType type,String notes,UUID applicationId) {}
record InterviewRequest(@NotNull Instant scheduledAt,@NotBlank String roundName,@NotNull InterviewType type,String meetingLink,String notes,UUID applicationId) {}
record DashboardResponse(long totalApplications,long last7Days,long last30Days,Map<ApplicationStatus,Long> byStatus,long upcomingInterviews,long pendingFollowUps,long overdueFollowUps,double interviewRate,double offerRate) {}
record ApiError(String code,String message,Map<String,String> fields,Instant timestamp) {}
