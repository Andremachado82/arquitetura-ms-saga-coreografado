package br.com.microservices.choreography.orderservice.core.controllers;

import br.com.microservices.choreography.orderservice.core.documents.Order;
import br.com.microservices.choreography.orderservice.core.dtos.OrderRequest;
import br.com.microservices.choreography.orderservice.core.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest orderRequest) {
         return orderService.createOrder(orderRequest);
    }
}
