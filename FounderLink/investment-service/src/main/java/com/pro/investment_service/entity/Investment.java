package com.pro.investment_service.entity;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "investments")
@Data
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long startupId;
    private Long investorId;

    private Double amount;

    private String status; // PENDING / APPROVED
}
