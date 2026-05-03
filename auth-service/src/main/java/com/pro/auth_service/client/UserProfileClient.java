package com.pro.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.pro.auth_service.dto.UserProfileSyncRequest;

@FeignClient(name = "user-service", path = "/users/internal")
public interface UserProfileClient {

    @PostMapping("/sync")
    void syncProfile(@RequestBody UserProfileSyncRequest request);
}
