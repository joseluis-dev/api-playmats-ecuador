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

import com.playmatsec.app.controller.model.PaymentDTO;
import com.playmatsec.app.repository.model.Payment;
import com.playmatsec.app.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {
  private final PaymentService paymentService;

  @GetMapping("/payments")
  public ResponseEntity<List<Payment>> getPayments(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String order,
    @RequestParam(required = false) String amount,
    @RequestParam(required = false) String providerPaymentId,
    @RequestParam(required = false) String method,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String imageUrl,
    @RequestParam(required = false) String paidAt,
    @RequestParam(required = false) String createdAt
  ) {
    log.info("headers: {}", headers);
    List<Payment> payments = paymentService.getPayments(order, amount, providerPaymentId, method, status, imageUrl, paidAt, createdAt);
    return payments != null ? ResponseEntity.ok(payments) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/payments/{id}")
  public ResponseEntity<Payment> getPaymentById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Payment payment = paymentService.getPaymentById(id);
    return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/payments/{id}")
  public ResponseEntity<Boolean> deletePaymentById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = paymentService.deletePayment(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/payments")
  public ResponseEntity<Payment> createPayment(@RequestHeader Map<String, String> headers, @RequestBody PaymentDTO payment) {
    log.info("headers: {}", headers);
    Payment createdPayment = paymentService.createPayment(payment);
    return createdPayment != null ? ResponseEntity.ok(createdPayment) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/payments/{id}")
  public ResponseEntity<Payment> updatePayment(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    Payment updatedPayment = paymentService.updatePayment(id, patchBody);
    return updatedPayment != null ? ResponseEntity.ok(updatedPayment) : ResponseEntity.notFound().build();
  }

  @PutMapping("/payments/{id}")
  public ResponseEntity<Payment> replacePayment(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody PaymentDTO payment) {
    log.info("headers: {}", headers);
    Payment replacedPayment = paymentService.updatePayment(id, payment);
    return replacedPayment != null ? ResponseEntity.ok(replacedPayment) : ResponseEntity.notFound().build();
  }
}
