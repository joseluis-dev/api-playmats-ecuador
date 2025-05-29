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

import com.playmatsec.app.controller.model.ResourceDTO;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.service.ResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResourcesController {
  private final ResourceService resourceService;

  @GetMapping("/resources")
  public ResponseEntity<List<Resource>> getResources(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String url,
    @RequestParam(required = false) String hosting
  ) {
    log.info("headers: {}", headers);
    List<Resource> resources = resourceService.getResources(name, url, hosting);
    return resources != null ? ResponseEntity.ok(resources) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/resources/{id}")
  public ResponseEntity<Resource> getResourceById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    Resource resource = resourceService.getResourceById(id);
    return resource != null ? ResponseEntity.ok(resource) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/resources/{id}")
  public ResponseEntity<Boolean> deleteResourceById(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = resourceService.deleteResource(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/resources")
  public ResponseEntity<Resource> createResource(@RequestHeader Map<String, String> headers, @RequestBody ResourceDTO resource) {
    log.info("headers: {}", headers);
    Resource createdResource = resourceService.createResource(resource);
    return createdResource != null ? ResponseEntity.ok(createdResource) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/resources/{id}")
  public ResponseEntity<Resource> updateResource(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    Resource updatedResource = resourceService.updateResource(id, patchBody);
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  @PutMapping("/resources/{id}")
  public ResponseEntity<Resource> replaceResource(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody ResourceDTO resource) {
    log.info("headers: {}", headers);
    Resource replacedResource = resourceService.updateResource(id, resource);
    return replacedResource != null ? ResponseEntity.ok(replacedResource) : ResponseEntity.notFound().build();
  }
}
