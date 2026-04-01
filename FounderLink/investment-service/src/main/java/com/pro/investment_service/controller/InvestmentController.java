package com.pro.investment_service.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.investment_service.entity.Investment;
import com.pro.investment_service.service.InvestmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService service;

    // INVEST
    @PostMapping
    @PreAuthorize("hasAnyRole('INVESTOR','ADMIN')")
    public Investment invest(@RequestBody Investment i) {
        return service.create(i);
    }

    // VIEW BY STARTUP
    @GetMapping("/startup/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','ADMIN')")
    public List<Investment> getByStartup(@PathVariable Long id) {
        return service.getByStartup(id);
    }

    // VIEW BY INVESTOR
    @GetMapping("/investor/{id}")
    @PreAuthorize("hasAnyRole('INVESTOR','ADMIN')")
    public List<Investment> getByInvestor(@PathVariable Long id) {
        return service.getByInvestor(id);
    }
}