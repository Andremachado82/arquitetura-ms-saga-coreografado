package br.com.microservices.choreography.inventoryservice.core.repositories;

import br.com.microservices.choreography.inventoryservice.core.models.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Integer> {
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
