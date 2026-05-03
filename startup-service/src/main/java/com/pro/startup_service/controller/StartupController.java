package com.pro.startup_service.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.startup_service.entity.Startup;
import com.pro.startup_service.service.StartupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/startups")
@RequiredArgsConstructor
public class StartupController {

    private final StartupService service;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER','ADMIN')")
    public Startup create(@RequestBody Startup s) {
        return service.create(s);
    }

    // GET ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public List<Startup> getAll() {
        return service.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public Startup getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','ADMIN')")
    public Startup update(@PathVariable Long id, @RequestBody Startup s) {
        return service.update(id, s);
    }

    // DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}