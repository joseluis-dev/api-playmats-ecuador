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

import com.playmatsec.app.controller.model.CategoryDTO;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoriesController {
  private final CategoryService categoryService;

  @GetMapping("/categories")
  public ResponseEntity<List<Category>> getCategories(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String description,
    @RequestParam(required = false) String color
  ) {
    log.info("headers: {}", headers);
    List<Category> categories = categoryService.getCategories(name, description, color);
    return categories != null ? ResponseEntity.ok(categories) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/categories/{id}")
  public ResponseEntity<Category> getCategoryById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Category category = categoryService.getCategoryById(id);
    return category != null ? ResponseEntity.ok(category) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/categories/{id}")
  public ResponseEntity<Boolean> deleteCategoryById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = categoryService.deleteCategory(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/categories")
  public ResponseEntity<Category> createCategory(@RequestHeader Map<String, String> headers, @RequestBody CategoryDTO category) {
    log.info("headers: {}", headers);
    Category createdCategory = categoryService.createCategory(category);
    return createdCategory != null ? ResponseEntity.ok(createdCategory) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/categories/{id}")
  public ResponseEntity<Category> updateCategory(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    Category updatedCategory = categoryService.updateCategory(id, patchBody);
    return updatedCategory != null ? ResponseEntity.ok(updatedCategory) : ResponseEntity.notFound().build();
  }

  @PutMapping("/categories/{id}")
  public ResponseEntity<Category> replaceCategory(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody CategoryDTO category) {
    log.info("headers: {}", headers);
    Category replacedCategory = categoryService.updateCategory(id, category);
    return replacedCategory != null ? ResponseEntity.ok(replacedCategory) : ResponseEntity.notFound().build();
  }
}
