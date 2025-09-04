package com.playmatsec.app.controller;

import java.math.BigDecimal;
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

import com.playmatsec.app.controller.model.CartDTO;
import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CartController {
  private final CartService cartService;

  @GetMapping("/carts")
  public ResponseEntity<List<Cart>> getCarts(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String user,
  @RequestParam(required = false) BigDecimal total,
    @RequestParam(required = false) String createdAt,
    @RequestParam(required = false) String updatedAt
  ) {
    log.info("headers: {}", headers);
  List<Cart> carts = cartService.getCarts(user, total, createdAt, updatedAt);
    return carts != null ? ResponseEntity.ok(carts) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/carts/{id}")
  public ResponseEntity<Cart> getCartById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Cart cart = cartService.getCartById(id);
    return cart != null ? ResponseEntity.ok(cart) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/carts/{id}")
  public ResponseEntity<Boolean> deleteCartById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = cartService.deleteCart(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/carts")
  public ResponseEntity<Cart> createCart(@RequestHeader Map<String, String> headers, @RequestBody CartDTO cart) {
    log.info("headers: {}", headers);
    Cart createdCart = cartService.createCart(cart);
    return createdCart != null ? ResponseEntity.ok(createdCart) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/carts/{id}")
  public ResponseEntity<Cart> updateCart(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    Cart updatedCart = cartService.updateCart(id, patchBody);
    return updatedCart != null ? ResponseEntity.ok(updatedCart) : ResponseEntity.notFound().build();
  }

  @PutMapping("/carts/{id}")
  public ResponseEntity<Cart> replaceCart(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody CartDTO cart) {
    log.info("headers: {}", headers);
    Cart replacedCart = cartService.updateCart(id, cart);
    return replacedCart != null ? ResponseEntity.ok(replacedCart) : ResponseEntity.notFound().build();
  }
}
