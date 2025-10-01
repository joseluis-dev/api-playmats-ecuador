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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.controller.model.ProductDTO;
import com.playmatsec.app.controller.model.CategoryIdsDTO;
import com.playmatsec.app.config.Authorized;
import com.playmatsec.app.controller.model.AttributeIdsDTO;
import com.playmatsec.app.controller.model.ResourceUploadDTO;
import com.playmatsec.app.controller.model.ResourceIdsDTO;
import com.playmatsec.app.repository.model.Attribute;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.Product.ResourceWithBanner;
import com.playmatsec.app.repository.utils.Consts.ResourceType;
import com.playmatsec.app.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductsController {
  private final ProductService productService;

  @GetMapping("/products")
  public ResponseEntity<List<Product>> getProducts(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String description,
    @RequestParam(required = false) Double price,
    @RequestParam(required = false) Boolean isCustomizable,
    @RequestParam(name = "resource", required = false) String resourceFilter,
    HttpServletRequest request
  ) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Product> products = productService.getProducts(name, description, price, isCustomizable, resourceFilter);
    return products != null ? ResponseEntity.ok(products) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/products/{id}")
  public ResponseEntity<Product> getProductById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product product = productService.getProductById(id);
    return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
  }

  @Authorized
  @DeleteMapping("/products/{id}")
  public ResponseEntity<Boolean> deleteProductById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = productService.deleteProduct(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PostMapping("/products")
  public ResponseEntity<Product> createProduct(@RequestHeader Map<String, String> headers, @RequestBody ProductDTO product, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product createdProduct = productService.createProduct(product);
    return createdProduct != null ? ResponseEntity.ok(createdProduct) : ResponseEntity.badRequest().build();
  }

  @Authorized
  @PatchMapping("/products/{id}")
  public ResponseEntity<Product> updateProduct(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.updateProduct(id, patchBody);
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PutMapping("/products/{id}")
  public ResponseEntity<Product> replaceProduct(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody ProductDTO product, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product replacedProduct = productService.updateProduct(id, product);
    return replacedProduct != null ? ResponseEntity.ok(replacedProduct) : ResponseEntity.notFound().build();
  }

  // Categories endpoints
  @GetMapping("/products/{id}/categories")
  public ResponseEntity<List<Category>> getProductCategories(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Category> categories = productService.getProductCategories(id);
    return categories != null ? ResponseEntity.ok(categories) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PostMapping("/products/{id}/categories")
  public ResponseEntity<Product> addProductCategories(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody CategoryIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.addCategoriesToProduct(id, requestBody.getCategoryIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PutMapping("/products/{id}/categories")
  public ResponseEntity<Product> replaceProductCategories(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody CategoryIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.replaceProductCategories(id, requestBody.getCategoryIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  // Attributes endpoints
  @GetMapping("/products/{id}/attributes")
  public ResponseEntity<List<Attribute>> getProductAttributes(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Attribute> attributes = productService.getProductAttributes(id);
    return attributes != null ? ResponseEntity.ok(attributes) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PostMapping("/products/{id}/attributes")
  public ResponseEntity<Product> addProductAttributes(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody AttributeIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.addAttributesToProduct(id, requestBody.getAttributeIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PutMapping("/products/{id}/attributes")
  public ResponseEntity<Product> replaceProductAttributes(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody AttributeIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.replaceProductAttributes(id, requestBody.getAttributeIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  // Resources endpoints
  @GetMapping("/products/{id}/resources")
  public ResponseEntity<List<Product.ResourceWithBanner>> getProductResources(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Product.ResourceWithBanner> resources = productService.getProductResources(id);
    return resources != null ? ResponseEntity.ok(resources) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PostMapping("/products/{id}/resources")
  public ResponseEntity<Product.ResourceWithBanner> addProductResource(
    @RequestHeader Map<String, String> headers,
    @PathVariable String id,
    @RequestParam(value = "file", required = true) MultipartFile file,
    @RequestParam(value = "type", required = false) ResourceType type,
    @RequestParam(value = "isBanner", required = false) Boolean isBanner,
    HttpServletRequest request
  ) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    ResourceUploadDTO uploadDTO = new ResourceUploadDTO();
    uploadDTO.setType(type);
    uploadDTO.setIsBanner(isBanner);
    ResourceWithBanner created = productService.addResourceToProductAndReturnResource(id, file, uploadDTO);
    return created != null ? ResponseEntity.status(201).body(created) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PostMapping("/products/{id}/resources/bulk")
  public ResponseEntity<Product> addProductResources(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody ResourceIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.addResourcesToProduct(id, requestBody.getResourceIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  @Authorized
  @PutMapping("/products/{id}/resources")
  public ResponseEntity<Product> replaceProductResources(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody ResourceIdsDTO requestBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Product updatedProduct = productService.replaceProductResources(id, requestBody.getResourceIds());
    return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
  }

  @Authorized
  @DeleteMapping("/products/{id}/resources/{resourceId}")
  public ResponseEntity<Map<String, Object>> deleteProductResource(
    @RequestHeader Map<String, String> headers, 
    @PathVariable String id, 
    @PathVariable String resourceId,
    HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = productService.deleteResourceFromProduct(id, resourceId);
    
    if (deleted) {
      Map<String, Object> response = Map.of(
        "success", true,
        "message", "Recurso eliminado correctamente del producto"
      );
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
