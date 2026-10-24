package com.applyflow.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class ApplicationServiceTest {
  private final ApplicationRepository applications = mock(ApplicationRepository.class);
  private final UserRepository users = mock(UserRepository.class);
  private final StatusHistoryRepository history = mock(StatusHistoryRepository.class);
  private final ApplicationService service = new ApplicationService(applications, users, history);
  private UUID userId;

  @BeforeEach
  void authenticate() {
    userId = UUID.randomUUID();
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, null));
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void updateRejectsApplicationOwnedByAnotherUser() {
    UUID applicationId = UUID.randomUUID();
    when(applications.findByIdAndUserId(applicationId, userId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.update(applicationId, request(ApplicationStatus.INTERVIEW)));
    verify(history, never()).save(any());
  }

  @Test
  void updateRecordsStatusTransition() {
    var user = new User("Test User", "test@example.com", "hash");
    var application = new JobApplication(user, "Acme", "Engineer", ApplicationSource.LINKEDIN, JobType.FULL_TIME,
        WorkMode.REMOTE, LocalDate.now(), Priority.MEDIUM);
    when(applications.findByIdAndUserId(application.id, userId)).thenReturn(Optional.of(application));
    when(applications.save(application)).thenReturn(application);

    service.update(application.id, request(ApplicationStatus.INTERVIEW));

    verify(history).save(argThat(item -> item.previousStatus == ApplicationStatus.APPLIED
        && item.newStatus == ApplicationStatus.INTERVIEW));
  }

  private ApplicationRequest request(ApplicationStatus status) {
    return new ApplicationRequest("Acme", "Engineer", null, ApplicationSource.LINKEDIN, JobType.FULL_TIME,
        WorkMode.REMOTE, null, null, null, LocalDate.now(), Priority.MEDIUM, null, "changed", status);
  }
}
