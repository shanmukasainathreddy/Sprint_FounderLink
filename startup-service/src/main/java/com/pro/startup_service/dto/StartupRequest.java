package com.pro.startup_service.dto;


import lombok.Data;

@Data
public class StartupRequest {
    private String title;
    private String description;
    private String domain;
    private Long userId;
}
