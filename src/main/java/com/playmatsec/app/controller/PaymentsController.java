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
import com.playmatsec.app.repository.model.Payment;

@RestController
@RequiredArgsConstructor
public class PaymentsController {
  @GetMapping("/payments")
  public ResponseEntity<List<Payment>> getPayments() {
    return ResponseEntity.ok(List.of(new Payment()));
  }

  @GetMapping("/payments/{id}")
  public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
    return ResponseEntity.ok(new Payment());
  }

  @DeleteMapping("/payments/{id}")
  public ResponseEntity<Boolean> deletePaymentById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/payments")
  public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
    return ResponseEntity.ok(new Payment());
  }

  @PatchMapping("/payments/{id}")
  public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
    return ResponseEntity.ok(new Payment());
  }

  @PutMapping("/payments/{id}")
  public ResponseEntity<Payment> replacePayment(@PathVariable Long id, @RequestBody Payment payment) {
    return ResponseEntity.ok(new Payment());
  }
}
