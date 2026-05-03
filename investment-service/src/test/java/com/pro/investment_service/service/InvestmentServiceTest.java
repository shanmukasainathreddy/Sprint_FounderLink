package com.pro.investment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pro.investment_service.client.StartupServiceClient;
import com.pro.investment_service.client.UserServiceClient;
import com.pro.investment_service.dto.NotificationEvent;
import com.pro.investment_service.dto.StartupResponse;
import com.pro.investment_service.dto.UserSummaryResponse;
import com.pro.investment_service.entity.Investment;
import com.pro.investment_service.producer.NotificationProducer;
import com.pro.investment_service.repository.InvestmentRepository;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository repo;

    @Mock
    private StartupServiceClient startupServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private InvestmentService service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreate() {
        authenticateAsUser(20L);
        Investment investment = new Investment();
        investment.setStartupId(10L);
        investment.setAmount(10000.0);

        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);
        StartupResponse startup = new StartupResponse();
        startup.setId(10L);
        startup.setTitle("Reddy's Lab");
        startup.setUserId(1L);
        UserSummaryResponse founder = new UserSummaryResponse();
        founder.setId(1L);
        founder.setEmail("founder@test.com");
        when(startupServiceClient.getStartupById(10L)).thenReturn(startup);
        when(userServiceClient.getUserById(1L)).thenReturn(founder);

        Investment result = service.create(investment);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(10000.0, result.getAmount());
        assertEquals(20L, result.getInvestorId());
        verify(repo, times(1)).save(investment);
        verify(notificationProducer, times(1)).sendNotification(any(NotificationEvent.class));
    }

    @Test
    void testApprove() {
        Investment existing = new Investment();
        existing.setId(1L);
        existing.setStatus("PENDING");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);

        Investment result = service.approve(1L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(existing);
    }

    @Test
    void testApproveNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.approve(1L));
        verify(repo, times(1)).findById(1L);
    }

    @Test
    void testGetByStartup() {
        Investment inv1 = new Investment();
        inv1.setStartupId(10L);

        when(repo.findByStartupId(10L)).thenReturn(Arrays.asList(inv1));

        List<Investment> result = service.getByStartup(10L);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getStartupId());

        verify(repo, times(1)).findByStartupId(10L);
    }

    @Test
    void testGetByInvestor() {
        Investment inv1 = new Investment();
        inv1.setInvestorId(20L);

        when(repo.findByInvestorId(20L)).thenReturn(Arrays.asList(inv1));

        List<Investment> result = service.getByInvestor(20L);
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getInvestorId());

        verify(repo, times(1)).findByInvestorId(20L);
    }

    @Test
    void testGetAll() {
        Investment inv1 = new Investment();
        Investment inv2 = new Investment();

        when(repo.findAll()).thenReturn(Arrays.asList(inv1, inv2));

        List<Investment> result = service.getAll();

        assertEquals(2, result.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void testUpdateStatusDefaultsNullStatusToPending() {
        Investment existing = new Investment();
        existing.setId(1L);
        existing.setStatus("APPROVED");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);

        Investment result = service.updateStatus(1L, null);

        assertEquals("PENDING", result.getStatus());
        verify(repo, times(1)).save(existing);
    }

    @Test
    void testUpdateStatusNormalizesStatus() {
        Investment existing = new Investment();
        existing.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);

        Investment result = service.updateStatus(1L, " approved ");

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void testUpdateStatusNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.updateStatus(99L, "APPROVED"));
    }

    @Test
    void testCreateRequiresAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, () -> service.create(new Investment()));
    }

    @Test
    void testCreateContinuesWhenStartupLookupFails() {
        authenticateAsUser(20L);
        Investment investment = new Investment();
        investment.setStartupId(10L);
        investment.setAmount(5000.0);

        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(startupServiceClient.getStartupById(10L)).thenThrow(new RuntimeException("service down"));

        Investment result = service.create(investment);

        assertEquals("PENDING", result.getStatus());
        assertEquals(20L, result.getInvestorId());
        verify(notificationProducer, times(0)).sendNotification(any(NotificationEvent.class));
    }

    private void authenticateAsUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(String.valueOf(userId), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
