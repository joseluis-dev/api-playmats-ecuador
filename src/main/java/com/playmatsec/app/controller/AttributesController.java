package com.playmatsec.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.repository.model.Attribute;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AttributesController {
  @GetMapping("/attributes")
  public ResponseEntity<List<Attribute>> getAttributes() {
    // This method should return a list of attributes.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(List.of(new Attribute()));
  }

  @GetMapping("/attributes/{id}")
  public ResponseEntity<Attribute> getAttributeById(@PathVariable Long id) {
    // This method should return a specific attribute by its ID.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(new Attribute());
  }

  @DeleteMapping("/attributes/{id}")
  public ResponseEntity<Boolean> deleteAttributeById(@PathVariable Long id) {
    // This method should delete a specific attribute by its ID.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(true);
  }

  @PostMapping("/attributes")
  public ResponseEntity<Attribute> createAttribute(@RequestBody Attribute attribute) {
    // This method should create a new attribute.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(new Attribute());
  }

  @PatchMapping("/attributes/{id}")
  public ResponseEntity<Attribute> updateAttribute(@PathVariable Long id, @RequestBody Attribute attribute) {
    // This method should update an existing attribute by its ID.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(new Attribute());
  }

  @PutMapping("/attributes/{id}")
  public ResponseEntity<Attribute> replaceAttribute(@PathVariable Long id, @RequestBody Attribute attribute) {
    // This method should replace an existing attribute by its ID.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(new Attribute());
  }

}
