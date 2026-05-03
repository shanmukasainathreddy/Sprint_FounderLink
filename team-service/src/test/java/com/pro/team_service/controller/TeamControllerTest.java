package com.pro.team_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.team_service.dto.TeamRequest;
import com.pro.team_service.entity.Team;
import com.pro.team_service.service.TeamService;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private TeamService service;

    @InjectMocks
    private TeamController controller;

    @Test
    void testInvite() {
        TeamRequest req = new TeamRequest();
        req.setStartupId(1L);

        Team team = new Team();
        team.setRole("INVITED");
        when(service.invite(any(TeamRequest.class))).thenReturn(team);

        Team result = controller.invite(req);
        assertNotNull(result);
        assertEquals("INVITED", result.getRole());
        verify(service, times(1)).invite(req);
    }

    @Test
    void testJoin() {
        TeamRequest req = new TeamRequest();
        req.setStartupId(1L);

        Team team = new Team();
        team.setRole("MEMBER");
        when(service.join(any(TeamRequest.class))).thenReturn(team);

        Team result = controller.join(req);
        assertNotNull(result);
        assertEquals("MEMBER", result.getRole());
        verify(service, times(1)).join(req);
    }

    @Test
    void testGetTeamByStartup() {
        Team team = new Team();
        when(service.getByStartup(1L)).thenReturn(Arrays.asList(team));

        List<Team> result = controller.getTeam(1L);
        assertEquals(1, result.size());
        verify(service, times(1)).getByStartup(1L);
    }

    @Test
    void testGetMyTeams() {
        Team team = new Team();
        when(service.getMyTeams()).thenReturn(Arrays.asList(team));

        List<Team> result = controller.getMyTeams();
        assertEquals(1, result.size());
        verify(service, times(1)).getMyTeams();
    }
}
