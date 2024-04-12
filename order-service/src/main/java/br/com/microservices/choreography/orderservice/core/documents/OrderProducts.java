package br.com.microservices.choreography.orderservice.core.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProducts {

    private Product product;

    private Integer quantity;
}
