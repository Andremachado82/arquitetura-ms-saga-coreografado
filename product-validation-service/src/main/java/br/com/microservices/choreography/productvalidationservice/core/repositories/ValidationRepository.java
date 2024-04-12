package br.com.microservices.choreography.productvalidationservice.core.repositories;

import br.com.microservices.choreography.productvalidationservice.core.models.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    Optional<Validation> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
