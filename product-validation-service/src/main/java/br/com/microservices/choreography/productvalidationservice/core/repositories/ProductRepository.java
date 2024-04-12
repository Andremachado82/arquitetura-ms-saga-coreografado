package br.com.microservices.choreography.productvalidationservice.core.repositories;

import br.com.microservices.choreography.productvalidationservice.core.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Boolean existsByCode(String code);
}
