package com.pro.startup_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.startup_service.client.UserServiceClient;
import com.pro.startup_service.entity.Startup;
import com.pro.startup_service.producer.NotificationProducer;
import com.pro.startup_service.repository.StartupRepository;

@ExtendWith(MockitoExtension.class)
class StartupServiceTest {

    @Mock
    private StartupRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private StartupService service;

    @Test
    void testCreate() {
        Startup startup = new Startup();
        startup.setTitle("New Startup");
        
        when(repository.save(any(Startup.class))).thenReturn(startup);
        
        Startup result = service.create(startup);
        assertNotNull(result);
        assertEquals("New Startup", result.getTitle());
        verify(repository, times(1)).save(startup);
    }

    @Test
    void testGetAll() {
        Startup startup = new Startup();
        when(repository.findAll()).thenReturn(Arrays.asList(startup));
        
        List<Startup> result = service.getAll();
        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetById_success() {
        Startup startup = new Startup();
        startup.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(startup));
        
        Startup result = service.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testGetById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getById(1L));
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void testUpdate() {
        Startup existing = new Startup();
        existing.setId(1L);
        existing.setTitle("Old Title");

        Startup updatedInput = new Startup();
        updatedInput.setTitle("New Title");
        updatedInput.setDescription("Desc");
        updatedInput.setDomain("AI");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Startup.class))).thenAnswer(i -> i.getArguments()[0]);

        Startup result = service.update(1L, updatedInput);
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("Desc", result.getDescription());
        assertEquals("AI", result.getDomain());
        
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(existing);
    }

    @Test
    void testDelete() {
        Startup existing = new Startup();
        existing.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testSearch() {
        Startup startup = new Startup();
        startup.setTitle("AI App");
        
        when(repository.findByTitleContainingIgnoreCase("ai")).thenReturn(Arrays.asList(startup));
        
        List<Startup> result = service.search("ai");
        assertEquals(1, result.size());
        assertEquals("AI App", result.get(0).getTitle());
        verify(repository, times(1)).findByTitleContainingIgnoreCase("ai");
    }
}
