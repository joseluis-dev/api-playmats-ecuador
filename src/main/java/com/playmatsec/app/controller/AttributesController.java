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

import com.playmatsec.app.controller.model.AttributeDTO;
import com.playmatsec.app.repository.model.Attribute;
import com.playmatsec.app.service.AttributeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AttributesController {
  private final AttributeService attributeService;

  @GetMapping("/attributes")
  public ResponseEntity<List<Attribute>> getAttributes(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String description,
    @RequestParam(required = false) String color,
    @RequestParam(required = false) String createdAt,
    @RequestParam(required = false) String updatedAt
  ) {
    log.info("headers: {}", headers);
    List<Attribute> attributes = attributeService.getAttributes(
      name,
      description,
      color,
      createdAt,
      updatedAt
    );
    return attributes != null
        ? ResponseEntity.ok(attributes)
        : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/attributes/{id}")
  public ResponseEntity<Attribute> getAttributeById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Attribute attribute = attributeService.getAttributeById(id);
    return attribute != null ? ResponseEntity.ok(attribute) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/attributes/{id}")
  public ResponseEntity<Boolean> deleteAttributeById(@PathVariable Long id) {
    // This method should delete a specific attribute by its ID.
    // For now, we will return a placeholder string.
    return ResponseEntity.ok(true);
  }

  @PostMapping("/attributes")
  public ResponseEntity<Attribute> createAttribute(@RequestHeader Map<String, String> headers, @RequestBody AttributeDTO attribute) {
    log.info("headers: {}", headers);
    Attribute createdAttribute = attributeService.createAttribute(attribute);
    return createdAttribute != null
        ? ResponseEntity.ok(createdAttribute)
        : ResponseEntity.badRequest().build();
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
