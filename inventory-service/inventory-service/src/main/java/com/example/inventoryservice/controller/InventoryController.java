package com.example.inventoryservice.controller;

import com.example.inventoryservice.model.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    // in-memory store for stock levels
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // seed some stock
        stock.put("product-1", 10);
        stock.put("product-2", 5);
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestBody OrderRequest request) {
        if (request.getProductId() == null) {
            return ResponseEntity.badRequest().body("Product ID cannot be null");
        }

//        Integer available = stock.getOrDefault(request.getProductId(), 0);
        Integer available = 10;
        if (available >= request.getQuantity()) {
            stock.put(request.getProductId(), available - request.getQuantity());
            kafkaTemplate.send("inventory-topic", request.getOrderId(), request);
            return ResponseEntity.ok("reserved");
        } else {
            kafkaTemplate.send("inventory-topic", request.getOrderId(), "FAILED: insufficient stock for " + request.getProductId());
            return ResponseEntity.status(400).body("insufficient stock");
        }
    }


    @GetMapping("/stock/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable String productId) {
        return ResponseEntity.ok(stock.getOrDefault(productId, 0));
    }
}
