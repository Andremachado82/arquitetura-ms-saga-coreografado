package br.com.microservices.choreography.orderservice.core.controllers;

import br.com.microservices.choreography.orderservice.core.documents.Event;
import br.com.microservices.choreography.orderservice.core.dtos.EventFilters;
import br.com.microservices.choreography.orderservice.core.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/event")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public Event findByFilters(EventFilters eventFilters) {
        return eventService.findByFilters(eventFilters);
    }

    @GetMapping("all")
    public List<Event> findAll() {
        return eventService.findAll();
    }
}
