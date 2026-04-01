package com.pro.team_service.dto;

import lombok.Data;

@Data
public class TeamRequest {
    private Long startupId;
    private Long userId;
    private String role;
}