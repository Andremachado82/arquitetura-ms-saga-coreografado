package br.com.microservices.choreography.orderservice.core.services;

import br.com.microservices.choreography.orderservice.core.documents.Event;
import br.com.microservices.choreography.orderservice.core.repositories.EventRepository;
import br.com.microservices.choreography.orderservice.configs.exceptions.ValidationException;
import br.com.microservices.choreography.orderservice.core.dtos.EventFilters;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    public Event findByFilters(EventFilters eventFilters) {
        validateEmptyFilters(eventFilters);
        if (isEmpty(eventFilters.getOrderId())) {
            return findByTransactionId(eventFilters.getTransactionId());
        } else {
            return findByOrderId(eventFilters.getOrderId());
        }
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    private Event findByOrderId(String orderId) {
        return eventRepository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by OrderID."));
    }

    private Event findByTransactionId(String transactionId) {
        return eventRepository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by TransactionID."));
    }

    private void validateEmptyFilters(EventFilters eventFilters) {
        if (isEmpty(eventFilters.getOrderId()) && isEmpty(eventFilters.getTransactionId())) {
            throw new ValidationException("OrderID or TransactionID must be informed");
        }
    }
}
