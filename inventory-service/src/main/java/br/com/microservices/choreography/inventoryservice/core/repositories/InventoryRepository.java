package br.com.microservices.choreography.inventoryservice.core.repositories;

import br.com.microservices.choreography.inventoryservice.core.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductCode(String productCode);
}
