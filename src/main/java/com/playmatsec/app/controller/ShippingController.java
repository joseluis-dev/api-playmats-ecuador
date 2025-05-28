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
import com.playmatsec.app.repository.model.ShippingAddress;

@RestController
@RequiredArgsConstructor
public class ShippingController {
  @GetMapping("/shipping")
  public ResponseEntity<List<ShippingAddress>> getShippingAddresses() {
    return ResponseEntity.ok(List.of(new ShippingAddress()));
  }

  @GetMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> getShippingAddressById(@PathVariable Long id) {
    return ResponseEntity.ok(new ShippingAddress());
  }

  @DeleteMapping("/shipping/{id}")
  public ResponseEntity<Boolean> deleteShippingAddressById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/shipping")
  public ResponseEntity<ShippingAddress> createShippingAddress(@RequestBody ShippingAddress shippingAddress) {
    return ResponseEntity.ok(new ShippingAddress());
  }

  @PatchMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> updateShippingAddress(@PathVariable Long id, @RequestBody ShippingAddress shippingAddress) {
    return ResponseEntity.ok(new ShippingAddress());
  }

  @PutMapping("/shipping/{id}")
  public ResponseEntity<ShippingAddress> replaceShippingAddress(@PathVariable Long id, @RequestBody ShippingAddress shippingAddress) {
    return ResponseEntity.ok(new ShippingAddress());
  }
}
