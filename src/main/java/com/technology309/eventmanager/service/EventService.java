package com.technology309.eventmanager.service;

import com.technology309.eventmanager.model.Event;
import com.technology309.eventmanager.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final WeatherService weatherService;

    @Cacheable(value = "events", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Cacheable(value = "events", key = "'search-' + #title + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> searchEventsByTitle(String title, Pageable pageable) {
        return eventRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Cacheable(value = "events", key = "'location-' + #location + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> searchEventsByLocation(String location, Pageable pageable) {
        return eventRepository.findByLocationContainingIgnoreCase(location, pageable);
    }

    @Cacheable(value = "events", key = "'date-' + #start + '-' + #end + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Event> searchEventsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return eventRepository.findByStartDateBetween(start, end, pageable);
    }

    @Cacheable(value = "events", key = "'id-' + #id")
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event createEvent(Event event) {
        // Fetch weather data for the event location
        String weatherData = weatherService.getWeatherData(event.getLocation());
        event.setWeatherData(weatherData);
        return eventRepository.save(event);
    }

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);
        
        // Update weather data if location changed
        if (!event.getLocation().equals(eventDetails.getLocation())) {
            String weatherData = weatherService.getWeatherData(eventDetails.getLocation());
            event.setWeatherData(weatherData);
        }
        
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setLocation(eventDetails.getLocation());
        
        return eventRepository.save(event);
    }

    @CacheEvict(value = "events", allEntries = true)
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }
} 