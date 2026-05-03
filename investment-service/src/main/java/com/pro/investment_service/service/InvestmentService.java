package com.pro.investment_service.service;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pro.investment_service.client.StartupServiceClient;
import com.pro.investment_service.client.UserServiceClient;
import com.pro.investment_service.dto.NotificationEvent;
import com.pro.investment_service.dto.StartupResponse;
import com.pro.investment_service.dto.UserSummaryResponse;
import com.pro.investment_service.entity.Investment;
import com.pro.investment_service.producer.NotificationProducer;
import com.pro.investment_service.repository.InvestmentRepository;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentService {

    private final InvestmentRepository repo;
    private final StartupServiceClient startupServiceClient;
    private final UserServiceClient userServiceClient;
    private final NotificationProducer notificationProducer;

    public Investment create(Investment investment) {
        investment.setInvestorId(getAuthenticatedUserId());
        investment.setStatus("PENDING");
        Investment saved = repo.save(investment);
        notifyFounder(saved);
        return saved;
    }

    public Investment approve(Long id) {
        Investment inv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        inv.setStatus("APPROVED");
        return repo.save(inv);
    }

    public Investment updateStatus(Long id, String status) {
        Investment investment = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Investment not found for id " + id));
        investment.setStatus(status == null ? "PENDING" : status.trim().toUpperCase(Locale.ROOT));
        return repo.save(investment);
    }

    public List<Investment> getByStartup(Long startupId) {
        return repo.findByStartupId(startupId);
    }

    public List<Investment> getByInvestor(Long investorId) {
        return repo.findByInvestorId(investorId);
    }

    public List<Investment> getAll() {
        return repo.findAll();
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid authenticated user id");
        }
    }

    private void notifyFounder(Investment investment) {
        try {
            StartupResponse startup = startupServiceClient.getStartupById(investment.getStartupId());
            if (startup == null || startup.getUserId() == null) {
                return;
            }
            UserSummaryResponse founder = userServiceClient.getUserById(startup.getUserId());
            if (founder == null || founder.getEmail() == null || founder.getEmail().isBlank()) {
                return;
            }
            String startupName = startup.getTitle() == null ? "your startup" : startup.getTitle();
            String message = String.format(
                    Locale.ROOT,
                    "New investment request of INR %,.0f for %s. Log in to FounderLink to review and approve it.",
                    investment.getAmount() == null ? 0.0 : investment.getAmount(),
                    startupName);
            notificationProducer.sendNotification(new NotificationEvent(founder.getEmail(), message));
        } catch (RuntimeException ex) {
            // Investment creation should not fail when email delivery infrastructure is temporarily unavailable.
            log.warn("Could not publish founder investment notification for investment={}", investment.getId(), ex);
        }
    }
}
