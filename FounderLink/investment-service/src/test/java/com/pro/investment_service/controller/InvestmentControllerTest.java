package com.pro.investment_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.investment_service.entity.Investment;
import com.pro.investment_service.service.InvestmentService;

@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock
    private InvestmentService service;

    @InjectMocks
    private InvestmentController controller;

    @Test
    void testInvest() {
        Investment investment = new Investment();
        investment.setAmount(5000.0);

        when(service.create(any(Investment.class))).thenReturn(investment);

        Investment result = controller.invest(investment);
        assertNotNull(result);
        assertEquals(5000.0, result.getAmount());
        verify(service, times(1)).create(investment);
    }

    @Test
    void testGetByStartup() {
        Investment inv = new Investment();
        inv.setStartupId(10L);

        when(service.getByStartup(10L)).thenReturn(Arrays.asList(inv));

        List<Investment> result = controller.getByStartup(10L);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getStartupId());
        verify(service, times(1)).getByStartup(10L);
    }

    @Test
    void testGetByInvestor() {
        Investment inv = new Investment();
        inv.setInvestorId(20L);

        when(service.getByInvestor(20L)).thenReturn(Arrays.asList(inv));

        List<Investment> result = controller.getByInvestor(20L);
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getInvestorId());
        verify(service, times(1)).getByInvestor(20L);
    }
}
