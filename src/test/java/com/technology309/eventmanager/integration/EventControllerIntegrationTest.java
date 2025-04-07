package com.technology309.eventmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technology309.eventmanager.model.Event;
import com.technology309.eventmanager.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setStartDate(LocalDateTime.now());
        testEvent.setEndDate(LocalDateTime.now().plusHours(2));
        testEvent.setLocation("Madrid");
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testEvent.getTitle()))
                .andExpect(jsonPath("$.description").value(testEvent.getDescription()))
                .andExpect(jsonPath("$.location").value(testEvent.getLocation()))
                .andExpect(jsonPath("$.weatherData").exists());
    }

    @Test
    void createEvent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Event invalidEvent = new Event();
        invalidEvent.setTitle(""); // Invalid: empty title

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void getAllEvents_ShouldReturnListOfEvents() throws Exception {
        // Create an event first
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedEvent.getId()))
                .andExpect(jsonPath("$[0].title").value(savedEvent.getTitle()));
    }

    @Test
    void getEventById_WhenEventExists_ShouldReturnEvent() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(get("/api/events/" + savedEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedEvent.getId()))
                .andExpect(jsonPath("$.title").value(savedEvent.getTitle()));
    }

    @Test
    void getEventById_WhenEventDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_WhenEventExists_ShouldUpdateAndReturnEvent() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);
        savedEvent.setTitle("Updated Title");

        mockMvc.perform(put("/api/events/" + savedEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateEvent_WhenEventDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put("/api/events/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_WhenEventExists_ShouldDeleteEvent() throws Exception {
        Event savedEvent = eventRepository.save(testEvent);

        mockMvc.perform(delete("/api/events/" + savedEvent.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/events/" + savedEvent.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_WhenEventDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/events/999"))
                .andExpect(status().isNotFound());
    }
} 