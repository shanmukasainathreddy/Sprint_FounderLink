package com.pro.startup_service.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.pro.startup_service.client.UserServiceClient;
import com.pro.startup_service.dto.NotificationEvent;
import com.pro.startup_service.dto.UserSummaryResponse;
import com.pro.startup_service.entity.Startup;
import com.pro.startup_service.producer.NotificationProducer;
import com.pro.startup_service.repository.StartupRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StartupService {

    private final StartupRepository repository;
    private final UserServiceClient userServiceClient;
    private final NotificationProducer notificationProducer;

    public Startup create(Startup startup) {
        // Prevent detached-entity merge when clients send an id for create.
        startup.setId(null);
        Startup savedStartup = repository.save(startup);
        notifyStartupOwner(savedStartup, "Startup '" + savedStartup.getTitle() + "' has been created with status "
                + savedStartup.getStatus() + ".");
        log.info("Created startup with id={}", savedStartup.getId());
        return savedStartup;
    }

    public List<Startup> getAll() {
        return repository.findAll();
    }

    public Startup getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Startup not found for id " + id));
    }

    public Startup update(Long id, Startup updated) {
        Startup existing = getById(id);
        String previousStatus = existing.getStatus();
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDomain(updated.getDomain());
        existing.setStatus(updated.getStatus() == null || updated.getStatus().isBlank() ? "PENDING" : updated.getStatus());
        Startup savedStartup = repository.save(existing);
        notifyIfStatusChanged(savedStartup, previousStatus);
        log.info("Updated startup with id={}", id);
        return savedStartup;
    }

    public void delete(Long id) {
        getById(id);
        repository.deleteById(id);
        log.info("Deleted startup with id={}", id);
    }

    public List<Startup> search(String keyword) {
        return repository.findByTitleContainingIgnoreCase(keyword);
    }

    private void notifyIfStatusChanged(Startup startup, String previousStatus) {
        String currentStatus = startup.getStatus();
        if (currentStatus == null) {
            return;
        }
        if (previousStatus != null && previousStatus.equalsIgnoreCase(currentStatus)) {
            return;
        }

        if ("ACCEPTED".equalsIgnoreCase(currentStatus)) {
            notifyStartupOwner(startup,
                    "Congratulations! Your startup '" + startup.getTitle() + "' has been accepted.");
        } else if ("REJECTED".equalsIgnoreCase(currentStatus)) {
            notifyStartupOwner(startup,
                    "Update on your startup '" + startup.getTitle() + "': it was rejected.");
        }
    }

    private void notifyStartupOwner(Startup startup, String message) {
        if (startup.getUserId() == null) {
            return;
        }
        UserSummaryResponse owner = userServiceClient.getUserById(startup.getUserId());
        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {
            return;
        }

        notificationProducer.sendNotification(new NotificationEvent(owner.getEmail(), message));
    }
}
