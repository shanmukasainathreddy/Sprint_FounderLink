package com.pro.user_service.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    private String skills;

    @Size(max = 200, message = "Experience must not exceed 200 characters")
    private String experience;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 500, message = "Portfolio links must not exceed 500 characters")
    private String portfolioLinks;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;
}
