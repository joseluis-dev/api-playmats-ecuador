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
import com.playmatsec.app.repository.model.Resource;

@RestController
@RequiredArgsConstructor
public class ResourcesController {
  @GetMapping("/resources")
  public ResponseEntity<List<Resource>> getResources() {
    return ResponseEntity.ok(List.of(new Resource()));
  }

  @GetMapping("/resources/{id}")
  public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
    return ResponseEntity.ok(new Resource());
  }

  @DeleteMapping("/resources/{id}")
  public ResponseEntity<Boolean> deleteResourceById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/resources")
  public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
    return ResponseEntity.ok(new Resource());
  }

  @PatchMapping("/resources/{id}")
  public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
    return ResponseEntity.ok(new Resource());
  }

  @PutMapping("/resources/{id}")
  public ResponseEntity<Resource> replaceResource(@PathVariable Long id, @RequestBody Resource resource) {
    return ResponseEntity.ok(new Resource());
  }
}
