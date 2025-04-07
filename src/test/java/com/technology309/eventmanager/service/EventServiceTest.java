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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartDate(LocalDateTime.now().plusDays(1));
        testEvent.setEndDate(LocalDateTime.now().plusDays(2));
        testEvent.setLocation("Test Location");
        testEvent.setWeatherData("Test Weather Data");

        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "startDate"));
    }

    @Test
    void getAllEvents_ShouldReturnPageOfEvents() {
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        when(eventRepository.findAll(pageRequest)).thenReturn(page);

        Page<Event> result = eventService.getAllEvents(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testEvent, result.getContent().get(0));
        verify(eventRepository).findAll(pageRequest);
    }

    @Test
    void searchEventsByTitle_ShouldReturnMatchingEvents() {
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        when(eventRepository.findByTitleContainingIgnoreCase("Test", pageRequest)).thenReturn(page);

        Page<Event> result = eventService.searchEventsByTitle("Test", pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testEvent, result.getContent().get(0));
        verify(eventRepository).findByTitleContainingIgnoreCase("Test", pageRequest);
    }

    @Test
    void searchEventsByLocation_ShouldReturnMatchingEvents() {
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        when(eventRepository.findByLocationContainingIgnoreCase("Test", pageRequest)).thenReturn(page);

        Page<Event> result = eventService.searchEventsByLocation("Test", pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testEvent, result.getContent().get(0));
        verify(eventRepository).findByLocationContainingIgnoreCase("Test", pageRequest);
    }

    @Test
    void searchEventsByDateRange_ShouldReturnMatchingEvents() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> page = new PageImpl<>(events, pageRequest, events.size());
        when(eventRepository.findByStartDateBetween(startDate, endDate, pageRequest)).thenReturn(page);

        Page<Event> result = eventService.searchEventsByDateRange(startDate, endDate, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testEvent, result.getContent().get(0));
        verify(eventRepository).findByStartDateBetween(startDate, endDate, pageRequest);
    }

    @Test
    void getEventById_ShouldReturnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(testEvent, result);
        verify(eventRepository).findById(1L);
    }

    @Test
    void getEventById_WhenEventNotFound_ShouldThrowException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.getEventById(1L));
        verify(eventRepository).findById(1L);
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        when(weatherService.getWeatherData(anyString())).thenReturn("Test Weather Data");
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(testEvent);

        assertNotNull(result);
        assertEquals(testEvent, result);
        verify(weatherService).getWeatherData(testEvent.getLocation());
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEvent_WhenEventExists_ShouldReturnUpdatedEvent() {
        // Create original event
        Event originalEvent = new Event();
        originalEvent.setId(1L);
        originalEvent.setTitle("Original Event");
        originalEvent.setDescription("Original Description");
        originalEvent.setStartDate(LocalDateTime.now().plusDays(1));
        originalEvent.setEndDate(LocalDateTime.now().plusDays(2));
        originalEvent.setLocation("Original Location");
        originalEvent.setWeatherData("Original Weather Data");

        // Create updated event
        Event updatedEvent = new Event();
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setStartDate(LocalDateTime.now().plusDays(1));
        updatedEvent.setEndDate(LocalDateTime.now().plusDays(2));
        updatedEvent.setLocation("Updated Location");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(originalEvent));
        when(weatherService.getWeatherData("Updated Location")).thenReturn("Updated Weather Data");
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L);
            savedEvent.setWeatherData("Updated Weather Data");
            return savedEvent;
        });

        Event result = eventService.updateEvent(1L, updatedEvent);

        assertNotNull(result);
        assertEquals("Updated Event", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Updated Location", result.getLocation());
        assertEquals("Updated Weather Data", result.getWeatherData());
        verify(eventRepository).findById(1L);
        verify(weatherService).getWeatherData("Updated Location");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_WhenEventNotFound_ShouldThrowException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.updateEvent(1L, testEvent));
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
        verify(weatherService, never()).getWeatherData(anyString());
    }

    @Test
    void deleteEvent_WhenEventExists_ShouldDeleteEvent() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(1L);

        eventService.deleteEvent(1L);

        verify(eventRepository).existsById(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEvent_WhenEventNotFound_ShouldThrowException() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> eventService.deleteEvent(1L));
        verify(eventRepository).existsById(1L);
        verify(eventRepository, never()).deleteById(anyLong());
    }
}