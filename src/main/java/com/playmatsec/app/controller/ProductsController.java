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
import com.playmatsec.app.repository.model.Product;

@RestController
@RequiredArgsConstructor
public class ProductsController {
  @GetMapping("/products")
  public ResponseEntity<List<Product>> getProducts() {
    return ResponseEntity.ok(List.of(new Product()));
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(new Product());
  }

  @DeleteMapping("/products/{id}")
  public ResponseEntity<Boolean> deleteProductById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/products")
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    return ResponseEntity.ok(new Product());
  }

  @PatchMapping("/products/{id}")
  public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
    return ResponseEntity.ok(new Product());
  }

  @PutMapping("/products/{id}")
  public ResponseEntity<Product> replaceProduct(@PathVariable Long id, @RequestBody Product product) {
    return ResponseEntity.ok(new Product());
  }
}
