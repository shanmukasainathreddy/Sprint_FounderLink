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
    private String description;
    private String domain;
    private String status = "PENDING";
    
    @Column(name = "user_id")
    private Long userId; // creator
}
