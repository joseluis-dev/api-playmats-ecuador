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
import com.playmatsec.app.repository.model.Category;

@RestController
@RequiredArgsConstructor
public class CategoriesController {
  @GetMapping("/categories")
  public ResponseEntity<List<Category>> getCategories() {
    return ResponseEntity.ok(List.of(new Category()));
  }

  @GetMapping("/categories/{id}")
  public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
    return ResponseEntity.ok(new Category());
  }

  @DeleteMapping("/categories/{id}")
  public ResponseEntity<Boolean> deleteCategoryById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/categories")
  public ResponseEntity<Category> createCategory(@RequestBody Category category) {
    return ResponseEntity.ok(new Category());
  }

  @PatchMapping("/categories/{id}")
  public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
    return ResponseEntity.ok(new Category());
  }

  @PutMapping("/categories/{id}")
  public ResponseEntity<Category> replaceCategory(@PathVariable Long id, @RequestBody Category category) {
    return ResponseEntity.ok(new Category());
  }
}
