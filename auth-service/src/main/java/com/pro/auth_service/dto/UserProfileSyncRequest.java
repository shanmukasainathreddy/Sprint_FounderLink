package com.pro.auth_service.dto;

import lombok.Data;

@Data
public class UserProfileSyncRequest {
    private Long id;
    private String name;
    private String email;
    private String bio;
}
