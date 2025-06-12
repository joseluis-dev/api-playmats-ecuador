package com.playmatsec.app.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.controller.model.OrderDTO;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrdersController {
  private final OrderService orderService;

  @GetMapping("/orders")
  public ResponseEntity<List<Order>> getOrders(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String user,
    @RequestParam(required = false) String createdAt,
    @RequestParam(required = false) String updatedAt,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String totalAmount,
    @RequestParam(required = false) String shippingAddress,
    @RequestParam(required = false) String billingAddress,
    @RequestParam(required = false) String payment
  ) {
    log.info("headers: {}", headers);
    List<Order> orders = orderService.getOrders(user, createdAt, updatedAt, status, totalAmount, shippingAddress, billingAddress, payment);
    return orders != null ? ResponseEntity.ok(orders) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/orders/{id}")
  public ResponseEntity<Order> getOrderById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Order order = orderService.getOrderById(id);
    return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/orders/{id}")
  public ResponseEntity<Boolean> deleteOrderById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = orderService.deleteOrder(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/orders")
  public ResponseEntity<Order> createOrder(@RequestHeader Map<String, String> headers, @RequestBody OrderDTO order) {
    log.info("headers: {}", headers);
    Order createdOrder = orderService.createOrder(order);
    return createdOrder != null ? ResponseEntity.ok(createdOrder) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/orders/{id}")
  public ResponseEntity<Order> updateOrder(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    Order updatedOrder = orderService.updateOrder(id, patchBody);
    return updatedOrder != null ? ResponseEntity.ok(updatedOrder) : ResponseEntity.notFound().build();
  }

  @PutMapping("/orders/{id}")
  public ResponseEntity<Order> replaceOrder(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody OrderDTO order) {
    log.info("headers: {}", headers);
    Order replacedOrder = orderService.updateOrder(id, order);
    return replacedOrder != null ? ResponseEntity.ok(replacedOrder) : ResponseEntity.notFound().build();
  }
}
