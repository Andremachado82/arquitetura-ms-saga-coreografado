package br.com.microservices.choreography.orderservice.core.services;

import br.com.microservices.choreography.orderservice.core.documents.Event;
import br.com.microservices.choreography.orderservice.core.documents.History;
import br.com.microservices.choreography.orderservice.core.documents.Order;
import br.com.microservices.choreography.orderservice.core.repositories.EventRepository;
import br.com.microservices.choreography.orderservice.configs.exceptions.ValidationException;
import br.com.microservices.choreography.orderservice.core.dtos.EventFilters;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.microservices.choreography.orderservice.core.enums.ESagaStatus.SUCCESS;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private static final String CURRENT_SERVICE = "ORDER_SERVICE";
    private final EventRepository eventRepository;

    public void notifyEnding(Event event) {
        event.setSource(CURRENT_SERVICE);
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        setEndingHistory(event);
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    private void setEndingHistory(Event event) {
        if (SUCCESS.equals(event.getStatus())) {
            log.info("#### SAGA FINISHED SUCCESSFULLY FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished successfully!");
        } else {
            log.info("#### SAGA FINISHED WITH ERRORS FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished with errors!");
        }
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

    public Event createEvent(Order order) {
        var event = Event.builder()
                .source(CURRENT_SERVICE)
                .status(SUCCESS)
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        addHistory(event, "Saga Started!");
        return save(event);
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

    private void addHistory(Event event, String message) {
        var history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }
}
