package com.playmatsec.app.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonParser;
import com.playmatsec.app.config.Authorized;
import com.playmatsec.app.controller.model.OrderDTO;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.OrderProduct;
import com.playmatsec.app.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Authorized
public class OrdersController {
  private final OrderService orderService;
  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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
    @RequestParam(required = false) String payment,
    HttpServletRequest request
  ) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Order> orders = orderService.getOrders(user, createdAt, updatedAt, status, totalAmount, shippingAddress, billingAddress, payment);
    return orders != null ? ResponseEntity.ok(orders) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/orders/{id}")
  public ResponseEntity<Order> getOrderById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Order order = orderService.getOrderById(id);
    return order != null ? ResponseEntity.ok(order) : ResponseEntity.notFound().build();
  }

  @GetMapping("/orders/{id}/products")
  public ResponseEntity<List<Product>> getOrderProductsByOrderId(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Product> products = orderService.getProductsByOrderId(id);
    return products != null ? ResponseEntity.ok(products) : ResponseEntity.notFound().build();
  }

  // Nuevo endpoint que retorna los OrderProducts con datos de cantidad, unitPrice, subtotal y el producto anidado
  @GetMapping("/orders/{id}/order-products")
  public ResponseEntity<List<OrderProduct>> getOrderProductsDetails(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<OrderProduct> orderProducts = orderService.getOrderProductsByOrderId(id);
    return orderProducts != null ? ResponseEntity.ok(orderProducts) : ResponseEntity.ok(Collections.emptyList());
  }

  @DeleteMapping("/orders/{id}")
  public ResponseEntity<Boolean> deleteOrderById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = orderService.deleteOrder(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  // @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
  // public ResponseEntity<Order> createOrder(@RequestHeader Map<String, String> headers, @RequestBody OrderDTO order) {
  //   log.info("headers: {}", headers);
  //   Order createdOrder = orderService.createOrder(order);
  //   return createdOrder != null ? ResponseEntity.ok(createdOrder) : ResponseEntity.badRequest().build();
  // }

  @PostMapping(value = "/orders", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Order> createOrderWithImage(
      @RequestHeader Map<String, String> headers,
      @RequestPart("order") String orderJson,
      @RequestPart(value = "paymentImage", required = false) MultipartFile paymentImage,
      HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    try {
      OrderDTO order = objectMapper.reader()
          .with(JsonParser.Feature.ALLOW_COMMENTS)
          .readValue(orderJson, OrderDTO.class);
      Order createdOrder = orderService.createOrder(order, paymentImage);
      return createdOrder != null ? ResponseEntity.ok(createdOrder) : ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error parsing order JSON", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @PatchMapping("/orders/{id}")
  public ResponseEntity<Order> updateOrder(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Order updatedOrder = orderService.updateOrder(id, patchBody);
    return updatedOrder != null ? ResponseEntity.ok(updatedOrder) : ResponseEntity.notFound().build();
  }

  @PutMapping("/orders/{id}")
  public ResponseEntity<Order> replaceOrder(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody OrderDTO order, HttpServletRequest request) {
    // log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Order replacedOrder = orderService.updateOrder(id, order);
    return replacedOrder != null ? ResponseEntity.ok(replacedOrder) : ResponseEntity.notFound().build();
  }
}
