package com.pro.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileSyncRequest {

    @NotNull(message = "Id is required")
    private Long id;

    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
