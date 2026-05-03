package com.pro.user_service.dto;

import lombok.Data;

@Data
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private String skills;
    private String experience;
    private String portfolioLinks;
    private String location;

    public UserSummaryResponse(Long id, String name, String email, String bio) {
        this(id, name, email, bio, null, null, null, null);
    }

    public UserSummaryResponse(
            Long id,
            String name,
            String email,
            String bio,
            String skills,
            String experience,
            String portfolioLinks,
            String location) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.skills = skills;
        this.experience = experience;
        this.portfolioLinks = portfolioLinks;
        this.location = location;
    }
}
