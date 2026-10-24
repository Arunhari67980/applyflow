package com.applyflow.api;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
class DashboardServiceTest {
  @Test void zeroApplicationsProduceZeroRates(){
    var apps=mock(ApplicationRepository.class);var interviews=mock(InterviewRepository.class);var follows=mock(FollowUpRepository.class);
    var id=UUID.randomUUID();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(id,null));
    when(apps.countByUserId(id)).thenReturn(0L);for(var s:ApplicationStatus.values())when(apps.countByUserIdAndStatus(id,s)).thenReturn(0L);when(apps.countByUserIdAndAppliedDateAfter(any(),any())).thenReturn(0L);when(interviews.findByUserIdAndScheduledAtAfterAndCancelledFalseOrderByScheduledAtAsc(any(),any())).thenReturn(List.of());when(follows.findByUserIdOrderByDueDateAsc(id)).thenReturn(List.of());
    var result=new DashboardService(apps,interviews,follows).get();assertEquals(0,result.interviewRate());assertEquals(0,result.offerRate());
  }
}
