package com.applyflow.api;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.time.*;
import java.util.*;

interface UserRepository extends JpaRepository<User,UUID> { Optional<User> findByEmailIgnoreCase(String email); }
interface ApplicationRepository extends JpaRepository<JobApplication,UUID> {
  Page<JobApplication> findByUserId(UUID userId, Pageable pageable);
  List<JobApplication> findByUserIdOrderByAppliedDateDesc(UUID userId);
  Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);
  long countByUserId(UUID userId);
  long countByUserIdAndStatus(UUID userId, ApplicationStatus status);
  long countByUserIdAndAppliedDateAfter(UUID userId, LocalDate date);
}
interface StatusHistoryRepository extends JpaRepository<StatusHistory,UUID> { List<StatusHistory> findByApplicationIdOrderByChangedAtAsc(UUID applicationId); }
interface FollowUpRepository extends JpaRepository<FollowUp,UUID> { List<FollowUp> findByUserIdOrderByDueDateAsc(UUID userId); List<FollowUp> findByDueDateLessThanEqualAndCompletedFalse(LocalDate date); Optional<FollowUp> findByIdAndUserId(UUID id,UUID userId); }
interface InterviewRepository extends JpaRepository<Interview,UUID> { List<Interview> findByUserIdAndScheduledAtAfterAndCancelledFalseOrderByScheduledAtAsc(UUID userId,Instant from); Optional<Interview> findByIdAndUserId(UUID id,UUID userId); }
interface NotificationRepository extends JpaRepository<Notification,UUID> {
  List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID id);
  Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
  boolean existsByFollowUpId(UUID followUpId);
}
