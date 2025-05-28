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
import com.playmatsec.app.repository.model.Cart;

@RestController
@RequiredArgsConstructor
public class CartController {
  @GetMapping("/carts")
  public ResponseEntity<List<Cart>> getCarts() {
    return ResponseEntity.ok(List.of(new Cart()));
  }

  @GetMapping("/carts/{id}")
  public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
    return ResponseEntity.ok(new Cart());
  }

  @DeleteMapping("/carts/{id}")
  public ResponseEntity<Boolean> deleteCartById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/carts")
  public ResponseEntity<Cart> createCart(@RequestBody Cart cart) {
    return ResponseEntity.ok(new Cart());
  }

  @PatchMapping("/carts/{id}")
  public ResponseEntity<Cart> updateCart(@PathVariable Long id, @RequestBody Cart cart) {
    return ResponseEntity.ok(new Cart());
  }

  @PutMapping("/carts/{id}")
  public ResponseEntity<Cart> replaceCart(@PathVariable Long id, @RequestBody Cart cart) {
    return ResponseEntity.ok(new Cart());
  }
}
