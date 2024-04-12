package br.com.microservices.choreography.orderservice.core.repositories;

import br.com.microservices.choreography.orderservice.core.documents.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findAllByOrderByCreatedAtDesc();

    Optional<Event> findTop1ByOrderIdOrderByCreatedAtDesc(String orderId);
    Optional<Event> findTop1ByTransactionIdOrderByCreatedAtDesc(String transactionId);
}
