package com.playmatsec.app.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

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
import org.springframework.web.multipart.MultipartFile;

import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.controller.model.CategoryIdsDTO;
import com.playmatsec.app.controller.model.AttributeIdsDTO;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Attribute;
import com.playmatsec.app.service.ResourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResourcesController {
  private final ResourceService resourceService;

  /**
   * Lista recursos con filtros opcionales.
   * Para múltiples categorías use:
   * - Repetir parámetro: ?category=1&category=2
   * - O separado por comas: ?category=1,2,3
   */
  @GetMapping("/resources")
  public ResponseEntity<List<Resource>> getResources(
      @RequestHeader Map<String, String> headers,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String url,
      @RequestParam(required = false) String thumbnail,
      @RequestParam(required = false) String watermark,
      @RequestParam(required = false) String hosting,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean isBanner,
      @RequestParam(required = false) String product,
      @RequestParam(name = "category", required = false) String categoryFilter,
      HttpServletRequest request){
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    // category = "1,2" (IDs AND), "#ff00aa" (color), "Azul" (name), "algo con espacios" (description)
    List<Resource> resources = resourceService.getResources(name, url, hosting, thumbnail, watermark, type, isBanner, product, categoryFilter);
    return resources != null ? ResponseEntity.ok(resources) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/resources/{id}")
  public ResponseEntity<Resource> getResourceById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource resource = resourceService.getResourceById(id);
    return resource != null ? ResponseEntity.ok(resource) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/resources/{id}")
  public ResponseEntity<Boolean> deleteResourceById(@RequestHeader Map<String, String> headers,
      @PathVariable String id,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = resourceService.deleteResource(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  /**
   * Crea un nuevo recurso subiendo un archivo a Cloudinary.
   * El tipo de archivo se detecta automáticamente basado en el contenido.
   */
  @PostMapping("/resources")
  public ResponseEntity<Resource> createResource(@RequestHeader Map<String, String> headers,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "isBanner", required = false) Boolean isBanner,
      @RequestParam(value = "name") String name,
      @RequestParam(value = "productId", required = false) String productId,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ResourceUploadDTO uploadDTO = new ResourceUploadDTO();
    // El tipo será detectado automáticamente basado en el archivo
    uploadDTO.setIsBanner(isBanner);
    uploadDTO.setName(name);
    uploadDTO.setProductId(productId);
    Resource createdResource = resourceService.createResource(file, uploadDTO);
    return createdResource != null ? ResponseEntity.ok(createdResource) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/resources/{id}")
  public ResponseEntity<Resource> updateResource(@RequestHeader Map<String, String> headers, @PathVariable String id,
      @RequestBody String patchBody,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource updatedResource = resourceService.updateResource(id, patchBody);
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  // @PutMapping("/resources/{id}")
  // public ResponseEntity<Resource> replaceResource(@RequestHeader Map<String, String> headers, @PathVariable String id,
  //     @RequestBody ResourceDTO resource) {
  //   log.info("headers: {}", headers);
  //   Resource replacedResource = resourceService.updateResource(id, resource);
  //   return replacedResource != null ? ResponseEntity.ok(replacedResource) : ResponseEntity.notFound().build();
  // }
  
  /**
   * Actualiza un recurso existente con un nuevo archivo.
   * Este endpoint primero carga el nuevo archivo a Cloudinary y luego,
   * si la carga es exitosa, elimina el archivo anterior y actualiza los datos del recurso.
   * El tipo de archivo se detecta automáticamente basado en el contenido.
   * 
   * @param headers Cabeceras de la petición
   * @param id ID del recurso a actualizar
   * @param file Nuevo archivo para el recurso
   * @param isBanner Indica si el recurso es un banner
   * @param name Nombre del recurso (opcional)
   * @return El recurso actualizado
   */
  @PutMapping(value = "/resources/{id}", consumes = "multipart/form-data")
  public ResponseEntity<Resource> updateResourceWithFile(
      @RequestHeader Map<String, String> headers,
      @PathVariable String id,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "isBanner", required = false) Boolean isBanner,
      @RequestParam(value = "name") String name,
      @RequestParam(value = "productId", required = false) String productId,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    
    ResourceUploadDTO uploadDTO = new ResourceUploadDTO();
    // El tipo será detectado automáticamente basado en el archivo
    uploadDTO.setIsBanner(isBanner);
    uploadDTO.setName(name);
    uploadDTO.setProductId(productId);
    
    Resource updatedResource = resourceService.updateResourceWithFile(id, file, uploadDTO);
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  // Categories endpoints
  @GetMapping("/resources/{id}/categories")
  public ResponseEntity<List<Category>> getResourceCategories(@RequestHeader Map<String, String> headers,
      @PathVariable String id,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Category> categories = resourceService.getResourceCategories(id);
    return categories != null ? ResponseEntity.ok(categories) : ResponseEntity.notFound().build();
  }

  @PostMapping("/resources/{id}/categories")
  public ResponseEntity<Resource> addResourceCategories(@RequestHeader Map<String, String> headers,
      @PathVariable String id, @RequestBody CategoryIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource updatedResource = resourceService.addCategoriesToResource(id, requestBody.getCategoryIds());
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  @PutMapping("/resources/{id}/categories")
  public ResponseEntity<Resource> replaceResourceCategories(@RequestHeader Map<String, String> headers,
      @PathVariable String id, @RequestBody CategoryIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource updatedResource = resourceService.replaceResourceCategories(id, requestBody.getCategoryIds());
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  // Attributes endpoints
  @GetMapping("/resources/{id}/attributes")
  public ResponseEntity<List<Attribute>> getResourceAttributes(@RequestHeader Map<String, String> headers,
      @PathVariable String id,
      HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Attribute> attributes = resourceService.getResourceAttributes(id);
    return attributes != null ? ResponseEntity.ok(attributes) : ResponseEntity.notFound().build();
  }

  @PostMapping("/resources/{id}/attributes")
  public ResponseEntity<Resource> addResourceAttributes(@RequestHeader Map<String, String> headers,
      @PathVariable String id, @RequestBody AttributeIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource updatedResource = resourceService.addAttributesToResource(id, requestBody.getAttributeIds());
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }

  @PutMapping("/resources/{id}/attributes")
  public ResponseEntity<Resource> replaceResourceAttributes(@RequestHeader Map<String, String> headers,
      @PathVariable String id, @RequestBody AttributeIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Resource updatedResource = resourceService.replaceResourceAttributes(id, requestBody.getAttributeIds());
    return updatedResource != null ? ResponseEntity.ok(updatedResource) : ResponseEntity.notFound().build();
  }
}
