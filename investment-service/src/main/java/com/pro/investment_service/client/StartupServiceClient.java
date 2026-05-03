package com.pro.investment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.pro.investment_service.dto.StartupResponse;

@FeignClient(name = "startup-service", path = "/startups")
public interface StartupServiceClient {

    @GetMapping("/{id}")
    StartupResponse getStartupById(@PathVariable("id") Long id);
}
