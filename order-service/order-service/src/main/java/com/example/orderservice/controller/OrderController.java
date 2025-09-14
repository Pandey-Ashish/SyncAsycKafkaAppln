package com.example.orderservice.controller;

import com.example.orderservice.model.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final WebClient webClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderController(WebClient.Builder webClientBuilder, KafkaTemplate<String, Object> kafkaTemplate) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build(); // inventory
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public Mono<ResponseEntity<String>> createOrder(@RequestBody OrderRequest request) {
        // 1) Call inventory service synchronously using WebClient to reserve stock
        return webClient.post()
                .uri("/inventory/reserve")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(invResp -> {
                    // 2) Publish to Kafka asynchronously for notifications
                    kafkaTemplate.send("orders-topic", request.getOrderId(), request);
                    return ResponseEntity.ok("Order placed and inventory reserved: " + invResp);
                })
                .onErrorResume(ex -> {
                    // send notification of failure as well
                    kafkaTemplate.send("orders-topic", request.getOrderId(), "FAILED: " + request);
                    return Mono.just(ResponseEntity.status(500).body("Failed to reserve inventory: " + ex.getMessage()));
                });
    }
}
