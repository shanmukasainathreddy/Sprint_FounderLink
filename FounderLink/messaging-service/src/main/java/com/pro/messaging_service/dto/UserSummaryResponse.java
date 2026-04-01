package com.pro.messaging_service.dto;

import lombok.Data;

@Data
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
}
