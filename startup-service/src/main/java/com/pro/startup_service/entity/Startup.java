package com.pro.startup_service.entity;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "startups")
@Data
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;
    private String domain;
    private String status = "PENDING";
    @Column(length = 2000)
    private String problemStatement;
    @Column(length = 2000)
    private String solution;
    private Double fundingGoal;
    private String stage;
    private String location;
    @Column(length = 2000)
    private String pitch;
    private String teamRoles;
    
    @Column(name = "user_id")
    private Long userId; // creator
}
