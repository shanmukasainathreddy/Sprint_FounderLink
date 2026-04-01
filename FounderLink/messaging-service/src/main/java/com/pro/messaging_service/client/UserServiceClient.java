package com.pro.messaging_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.pro.messaging_service.dto.UserSummaryResponse;

@FeignClient(name = "user-service", path = "/users/internal")
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserSummaryResponse getUserById(@PathVariable("id") Long id);
}
