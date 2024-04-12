package br.com.microservices.choreography.paymentservice.core.repositories;

import br.com.microservices.choreography.paymentservice.core.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);

}
