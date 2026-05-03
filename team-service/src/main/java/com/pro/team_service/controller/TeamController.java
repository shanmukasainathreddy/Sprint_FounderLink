package com.pro.team_service.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.team_service.dto.TeamRequest;
import com.pro.team_service.entity.Team;
import com.pro.team_service.service.TeamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService service;

    // ✅ INVITE CO-FOUNDER
    // Only Founder or Admin can invite
    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('FOUNDER','ADMIN')")
    public Team invite(@RequestBody TeamRequest req) {
        return service.invite(req);
    }

    // ✅ JOIN TEAM
    // Invited users can join their own invite, admins can join on behalf of others
    @PostMapping("/join")
    @PreAuthorize("isAuthenticated()")
    public Team join(@RequestBody TeamRequest req) {
        return service.join(req);
    }

    // ✅ GET TEAM MEMBERS OF A STARTUP
    // Founder, Co-founder, Investor, Admin can view
    @GetMapping("/startup/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','COFOUNDER','INVESTOR','ADMIN')")
    public List<Team> getTeam(@PathVariable Long id) {
        return service.getByStartup(id);
    }

    // ✅ OPTIONAL: GET MY TEAM (BEST PRACTICE)
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('FOUNDER','COFOUNDER')")
    public List<Team> getMyTeams() {
        return service.getMyTeams(); // implement using JWT user
    }
}
