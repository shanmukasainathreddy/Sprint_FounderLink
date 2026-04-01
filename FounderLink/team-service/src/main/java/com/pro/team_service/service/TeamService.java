package com.pro.team_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pro.team_service.client.UserServiceClient;
import com.pro.team_service.dto.NotificationEvent;
import com.pro.team_service.dto.TeamRequest;
import com.pro.team_service.dto.UserSummaryResponse;
import com.pro.team_service.entity.Team;
import com.pro.team_service.producer.NotificationProducer;
import com.pro.team_service.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String DEFAULT_TEAM_ROLE = "COFOUNDER";

    private final TeamRepository repository;
    private final UserServiceClient userServiceClient;
    private final NotificationProducer notificationProducer;

    // Invite Cofounder
    public Team invite(TeamRequest request) {
        Team team = repository.findByStartupIdAndUserId(request.getStartupId(), request.getUserId())
                .orElseGet(Team::new);
        team.setStartupId(request.getStartupId());
        team.setUserId(request.getUserId());
        team.setRole(resolveRequestedRole(request));
        team.setStatus(STATUS_PENDING);

        Team savedTeam = repository.save(team);
        notifyMember(savedTeam, "You have been invited to join startup " + savedTeam.getStartupId()
                + " as " + savedTeam.getRole() + ".");
        return savedTeam;
    }

    // Join Team
    public Team join(TeamRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication is required to join a team");
        }

        Long requesterUserId = parseUserId(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin && !requesterUserId.equals(request.getUserId())) {
            throw new AccessDeniedException("You can only accept your own team invitation");
        }

        Team team = repository.findByStartupIdAndUserId(request.getStartupId(), request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No invitation found for this startup and user"));

        if (!STATUS_PENDING.equalsIgnoreCase(team.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation is not pending");
        }

        team.setStatus(STATUS_ACTIVE);
        team.setRole(team.getRole() == null || team.getRole().isBlank() ? resolveRequestedRole(request) : team.getRole());

        Team savedTeam = repository.save(team);
        notifyMember(savedTeam, "You have joined startup " + savedTeam.getStartupId()
                + " as " + savedTeam.getRole() + ".");
        return savedTeam;
    }

    // ✅ ADD THIS BACK
    public List<Team> getByStartup(Long startupId) {
        return repository.findByStartupIdAndStatus(startupId, STATUS_ACTIVE);
    }

    // Get teams of logged-in user
    public List<Team> getMyTeams() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = parseUserId(auth.getName());
        return repository.findByUserIdAndStatus(userId, STATUS_ACTIVE);
    }

    private void notifyMember(Team team, String message) {
        if (team.getUserId() == null) {
            return;
        }

        try {
            UserSummaryResponse user = userServiceClient.getUserById(team.getUserId());
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                return;
            }

            notificationProducer.sendNotification(new NotificationEvent(user.getEmail(), message));
        } catch (RuntimeException ex) {
            log.warn("Skipping notification for startupId={} userId={} because member lookup or dispatch failed: {}",
                    team.getStartupId(), team.getUserId(), ex.getMessage());
        }
    }

    private Long parseUserId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid authenticated user id");
        }
    }

    private String resolveRequestedRole(TeamRequest request) {
        if (request.getRole() == null || request.getRole().isBlank()) {
            return DEFAULT_TEAM_ROLE;
        }
        return request.getRole().trim().toUpperCase();
    }
}
