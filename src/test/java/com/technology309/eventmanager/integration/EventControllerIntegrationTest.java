package com.technology309.eventmanager.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.technology309.eventmanager.model.Event;
import com.technology309.eventmanager.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventControllerIntegrationTest {

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
        objectMapper.registerModule(new JavaTimeModule());

        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setStartDate(LocalDateTime.now().plusDays(1));
        testEvent.setEndDate(LocalDateTime.now().plusDays(2));
        testEvent = eventRepository.save(testEvent);
    }

    private Page<Event> deserializePage(String json) throws Exception {
        Map<String, Object> pageData = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        List<Event> content = objectMapper.convertValue(pageData.get("content"),
                new TypeReference<List<Event>>() {
                });
        return new PageImpl<>(content,
                org.springframework.data.domain.PageRequest.of(
                        (Integer) pageData.get("number"),
                        (Integer) pageData.get("size")),
                (Integer) pageData.get("totalElements"));
    }

    @Test
    void getAllEventsWithPagination() throws Exception {
        // Create 14 events (plus the one created in setUp makes 15 total)
        for (int i = 0; i < 14; i++) {
            Event event = new Event();
            event.setTitle("Event " + i);
            event.setDescription("Description " + i);
            event.setLocation("Location " + i);
            event.setStartDate(LocalDateTime.now().plusDays(i));
            event.setEndDate(LocalDateTime.now().plusDays(i + 1));
            eventRepository.save(event);
        }

        MvcResult result = mockMvc.perform(get("/api/events")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Page<Event> page = deserializePage(content);
        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void searchEventsByTitle() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/search")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Page<Event> page = deserializePage(content);
        assertEquals(1, page.getContent().size());
        assertEquals("Test Event", page.getContent().get(0).getTitle());
    }

    @Test
    void searchEventsByLocation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/events/search")
                .param("location", "Test Location")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Page<Event> page = deserializePage(content);
        assertEquals(1, page.getContent().size());
        assertEquals("Test Location", page.getContent().get(0).getLocation());
    }

    @Test
    void searchEventsByDateRange() throws Exception {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(3);

        MvcResult result = mockMvc.perform(get("/api/events/search")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Page<Event> page = deserializePage(content);
        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent().get(0).getStartDate().isAfter(startDate));
        assertTrue(page.getContent().get(0).getEndDate().isBefore(endDate));
    }

    @Test
    void testCache() throws Exception {
        // First request
        MvcResult firstResult = mockMvc.perform(get("/api/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String firstContent = firstResult.getResponse().getContentAsString();
        Event firstEvent = objectMapper.readValue(firstContent, Event.class);

        // Second request (should be cached)
        MvcResult secondResult = mockMvc.perform(get("/api/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String secondContent = secondResult.getResponse().getContentAsString();
        Event secondEvent = objectMapper.readValue(secondContent, Event.class);

        // Verify both responses are identical
        assertEquals(firstContent, secondContent);
        assertEquals(firstEvent.getId(), secondEvent.getId());
        assertEquals(firstEvent.getTitle(), secondEvent.getTitle());
        assertEquals(firstEvent.getDescription(), secondEvent.getDescription());
        assertEquals(firstEvent.getLocation(), secondEvent.getLocation());
        assertEquals(firstEvent.getStartDate(), secondEvent.getStartDate());
        assertEquals(firstEvent.getEndDate(), secondEvent.getEndDate());
    }

    @Test
    void createEvent() throws Exception {
        Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setDescription("New Description");
        newEvent.setLocation("New Location");
        newEvent.setStartDate(LocalDateTime.now().plusDays(3));
        newEvent.setEndDate(LocalDateTime.now().plusDays(4));

        MvcResult result = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Event createdEvent = objectMapper.readValue(content, Event.class);
        assertNotNull(createdEvent.getId());
        assertEquals("New Event", createdEvent.getTitle());
        assertEquals("New Description", createdEvent.getDescription());
        assertEquals("New Location", createdEvent.getLocation());
    }

    @Test
    void updateEvent() throws Exception {
        Event updatedEvent = new Event();
        updatedEvent.setTitle("Updated Event");
        updatedEvent.setDescription("Updated Description");
        updatedEvent.setLocation("Updated Location");
        updatedEvent.setStartDate(LocalDateTime.now().plusDays(5));
        updatedEvent.setEndDate(LocalDateTime.now().plusDays(6));

        MvcResult result = mockMvc.perform(put("/api/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Event modifiedEvent = objectMapper.readValue(content, Event.class);
        assertEquals(testEvent.getId(), modifiedEvent.getId());
        assertEquals("Updated Event", modifiedEvent.getTitle());
        assertEquals("Updated Description", modifiedEvent.getDescription());
        assertEquals("Updated Location", modifiedEvent.getLocation());
    }

    @Test
    void deleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/" + testEvent.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/events/" + testEvent.getId()))
                .andExpect(status().isNotFound());
    }
}