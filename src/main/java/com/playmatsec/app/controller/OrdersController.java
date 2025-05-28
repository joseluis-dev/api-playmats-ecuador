package com.playmatsec.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import com.playmatsec.app.repository.model.Order;

@RestController
@RequiredArgsConstructor
public class OrdersController {
  @GetMapping("/orders")
  public ResponseEntity<List<Order>> getOrders() {
    return ResponseEntity.ok(List.of(new Order()));
  }

  @GetMapping("/orders/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(new Order());
  }

  @DeleteMapping("/orders/{id}")
  public ResponseEntity<Boolean> deleteOrderById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/orders")
  public ResponseEntity<Order> createOrder(@RequestBody Order order) {
    return ResponseEntity.ok(new Order());
  }

  @PatchMapping("/orders/{id}")
  public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
    return ResponseEntity.ok(new Order());
  }

  @PutMapping("/orders/{id}")
  public ResponseEntity<Order> replaceOrder(@PathVariable Long id, @RequestBody Order order) {
    return ResponseEntity.ok(new Order());
  }
}
