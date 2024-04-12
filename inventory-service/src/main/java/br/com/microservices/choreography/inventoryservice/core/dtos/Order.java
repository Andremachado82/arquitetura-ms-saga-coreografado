package br.com.microservices.choreography.inventoryservice.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private String id;

    private List<OrderProducts> products;

    private LocalDateTime createdAt;

    private String transactionId;

    private double totalAmount;

    private Integer totalItems;
}
