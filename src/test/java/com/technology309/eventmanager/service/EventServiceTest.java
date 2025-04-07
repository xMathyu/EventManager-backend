package com.technology309.eventmanager.service;

import com.technology309.eventmanager.model.Event;
import com.technology309.eventmanager.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartDate(LocalDateTime.now());
        testEvent.setEndDate(LocalDateTime.now().plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setWeatherData("Test Weather Data");
    }

    @Test
    void getAllEvents_ShouldReturnListOfEvents() {
        List<Event> expectedEvents = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(expectedEvents);

        List<Event> actualEvents = eventService.getAllEvents();

        assertEquals(expectedEvents, actualEvents);
        verify(eventRepository).findAll();
    }

    @Test
    void getEventById_WhenEventExists_ShouldReturnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Event actualEvent = eventService.getEventById(1L);

        assertEquals(testEvent, actualEvent);
        verify(eventRepository).findById(1L);
    }

    @Test
    void getEventById_WhenEventDoesNotExist_ShouldThrowException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.getEventById(1L));
        verify(eventRepository).findById(1L);
    }

    @Test
    void createEvent_ShouldSaveAndReturnEvent() {
        when(weatherService.getWeatherData(anyString())).thenReturn("Test Weather Data");
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event createdEvent = eventService.createEvent(testEvent);

        assertEquals(testEvent, createdEvent);
        verify(weatherService).getWeatherData(testEvent.getLocation());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEvent_WhenEventExists_ShouldUpdateAndReturnEvent() {
        // Setup
        Event updatedEvent = new Event();
        updatedEvent.setTitle("Updated Title");
        updatedEvent.setLocation("Updated Location");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setStartDate(LocalDateTime.now());
        updatedEvent.setEndDate(LocalDateTime.now().plusHours(2));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(weatherService.getWeatherData("Updated Location")).thenReturn("Updated Weather Data");
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            return savedEvent;
        });

        // Execute
        Event result = eventService.updateEvent(1L, updatedEvent);

        // Verify
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Location", result.getLocation());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Weather Data", result.getWeatherData());
        verify(eventRepository).findById(1L);
        verify(weatherService).getWeatherData("Updated Location");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_WhenEventDoesNotExist_ShouldThrowException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.updateEvent(1L, testEvent));
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(weatherService, never()).getWeatherData(anyString());
    }

    @Test
    void deleteEvent_WhenEventExists_ShouldDeleteEvent() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        eventService.deleteEvent(1L);

        verify(eventRepository).existsById(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEvent_WhenEventDoesNotExist_ShouldThrowException() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> eventService.deleteEvent(1L));
        verify(eventRepository).existsById(1L);
        verify(eventRepository, never()).deleteById(anyLong());
    }
} 