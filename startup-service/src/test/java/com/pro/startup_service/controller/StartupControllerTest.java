package com.pro.startup_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.pro.startup_service.entity.Startup;
import com.pro.startup_service.service.StartupService;

@ExtendWith(MockitoExtension.class)
class StartupControllerTest {

    @Mock
    private StartupService service;

    @InjectMocks
    private StartupController controller;

    @Test
    void testCreate() {
        Startup startup = new Startup();
        startup.setTitle("Test");
        
        when(service.create(any(Startup.class))).thenReturn(startup);
        
        Startup result = controller.create(startup);
        assertNotNull(result);
        assertEquals("Test", result.getTitle());
        verify(service, times(1)).create(startup);
    }

    @Test
    void testGetAll() {
        Startup startup = new Startup();
        when(service.getAll()).thenReturn(Arrays.asList(startup));
        
        List<Startup> result = controller.getAll();
        assertEquals(1, result.size());
        verify(service, times(1)).getAll();
    }

    @Test
    void testGetById() {
        Startup startup = new Startup();
        startup.setId(1L);
        when(service.getById(1L)).thenReturn(startup);
        
        Startup result = controller.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(service, times(1)).getById(1L);
    }

    @Test
    void testUpdate() {
        Startup startup = new Startup();
        startup.setId(1L);
        startup.setTitle("Updated");
        
        when(service.update(eq(1L), any(Startup.class))).thenReturn(startup);
        
        Startup result = controller.update(1L, startup);
        assertNotNull(result);
        assertEquals("Updated", result.getTitle());
        verify(service, times(1)).update(1L, startup);
    }

    @Test
    void testDelete() {
        controller.delete(1L);
        verify(service, times(1)).delete(1L);
    }
}
