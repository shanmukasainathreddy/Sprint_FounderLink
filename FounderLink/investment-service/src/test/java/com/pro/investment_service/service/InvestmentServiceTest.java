package com.pro.investment_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.investment_service.entity.Investment;
import com.pro.investment_service.repository.InvestmentRepository;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository repo;

    @InjectMocks
    private InvestmentService service;

    @Test
    void testCreate() {
        Investment investment = new Investment();
        investment.setAmount(10000.0);

        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);

        Investment result = service.create(investment);
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(10000.0, result.getAmount());
        verify(repo, times(1)).save(investment);
    }

    @Test
    void testApprove() {
        Investment existing = new Investment();
        existing.setId(1L);
        existing.setStatus("PENDING");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Investment.class))).thenAnswer(i -> i.getArguments()[0]);

        Investment result = service.approve(1L);
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());

        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(existing);
    }

    @Test
    void testApproveNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.approve(1L));
        verify(repo, times(1)).findById(1L);
    }

    @Test
    void testGetByStartup() {
        Investment inv1 = new Investment();
        inv1.setStartupId(10L);

        when(repo.findByStartupId(10L)).thenReturn(Arrays.asList(inv1));

        List<Investment> result = service.getByStartup(10L);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getStartupId());

        verify(repo, times(1)).findByStartupId(10L);
    }

    @Test
    void testGetByInvestor() {
        Investment inv1 = new Investment();
        inv1.setInvestorId(20L);

        when(repo.findByInvestorId(20L)).thenReturn(Arrays.asList(inv1));

        List<Investment> result = service.getByInvestor(20L);
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getInvestorId());

        verify(repo, times(1)).findByInvestorId(20L);
    }
}
