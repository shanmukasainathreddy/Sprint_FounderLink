package com.pro.team_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.pro.team_service.client.UserServiceClient;
import com.pro.team_service.dto.TeamRequest;
import com.pro.team_service.dto.UserSummaryResponse;
import com.pro.team_service.entity.Team;
import com.pro.team_service.producer.NotificationProducer;
import com.pro.team_service.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private TeamService teamService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testInvite() {
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);

        UserSummaryResponse user = new UserSummaryResponse();
        user.setEmail("member@example.com");

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.empty());
        when(repository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userServiceClient.getUserById(200L)).thenReturn(user);

        Team result = teamService.invite(request);

        assertNotNull(result);
        assertEquals(100L, result.getStartupId());
        assertEquals(200L, result.getUserId());
        assertEquals("COFOUNDER", result.getRole());
        assertEquals("PENDING", result.getStatus());

        verify(repository, times(1)).save(any(Team.class));
        verify(notificationProducer, times(1)).sendNotification(any());
    }

    @Test
    void testInviteStillSucceedsWhenUserLookupFails() {
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.empty());
        when(repository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);
        doThrow(new RuntimeException("User profile not found"))
                .when(userServiceClient).getUserById(200L);

        Team result = teamService.invite(request);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(repository, times(1)).save(any(Team.class));
        verify(notificationProducer, never()).sendNotification(any());
    }

    @Test
    void testGetByStartup() {
        Team team = new Team();
        team.setStartupId(100L);
        team.setStatus("ACTIVE");

        when(repository.findByStartupId(100L)).thenReturn(Arrays.asList(team));

        List<Team> result = teamService.getByStartup(100L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getStartupId());

        verify(repository, times(1)).findByStartupId(100L);
    }

    @Test
    void testGetMyTeams() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("500");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedContext = mockStatic(SecurityContextHolder.class)) {
            mockedContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            Team team = new Team();
            team.setUserId(500L);
            team.setStatus("ACTIVE");
            when(repository.findByUserIdAndStatus(500L, "ACTIVE")).thenReturn(Arrays.asList(team));

            List<Team> result = teamService.getMyTeams();
            assertEquals(1, result.size());
            assertEquals(500L, result.get(0).getUserId());

            verify(repository, times(1)).findByUserIdAndStatus(500L, "ACTIVE");
        }
    }

    @Test
    void testJoinActivatesPendingInvitationForOwner() {
        authenticateAsUser(200L);
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);

        Team pending = new Team();
        pending.setStartupId(100L);
        pending.setUserId(200L);
        pending.setRole("DESIGNER");
        pending.setStatus("PENDING");

        UserSummaryResponse user = new UserSummaryResponse();
        user.setEmail("member@example.com");

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.of(pending));
        when(repository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userServiceClient.getUserById(200L)).thenReturn(user);

        Team result = teamService.join(request);

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("DESIGNER", result.getRole());
        verify(notificationProducer, times(1)).sendNotification(any());
    }

    @Test
    void testJoinAllowsAdminToAcceptForAnotherUser() {
        authenticateAsAdmin(1L);
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);
        request.setRole("backend");

        Team pending = new Team();
        pending.setStartupId(100L);
        pending.setUserId(200L);
        pending.setStatus("PENDING");

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.of(pending));
        when(repository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);

        Team result = teamService.join(request);

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("BACKEND", result.getRole());
    }

    @Test
    void testJoinRejectsDifferentNonAdminUser() {
        authenticateAsUser(201L);
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> teamService.join(request));

        assertEquals("You can only accept your own team invitation", ex.getMessage());
    }

    @Test
    void testJoinRejectsMissingInvitation() {
        authenticateAsUser(200L);
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> teamService.join(request));
    }

    @Test
    void testInviteReusesExistingInvitationAndUppercasesRole() {
        TeamRequest request = new TeamRequest();
        request.setStartupId(100L);
        request.setUserId(200L);
        request.setRole("designer");

        Team existing = new Team();
        existing.setStartupId(100L);
        existing.setUserId(200L);
        existing.setStatus("ACTIVE");

        when(repository.findByStartupIdAndUserId(100L, 200L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Team.class))).thenAnswer(i -> i.getArguments()[0]);

        Team result = teamService.invite(request);

        assertEquals("PENDING", result.getStatus());
        assertEquals("DESIGNER", result.getRole());
    }

    @Test
    void testGetMyTeamsRejectsInvalidAuthenticatedUserId() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("not-a-number", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(AccessDeniedException.class, () -> teamService.getMyTeams());
    }

    private void authenticateAsUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(String.valueOf(userId), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateAsAdmin(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                String.valueOf(userId),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
