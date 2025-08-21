package com.playmatsec.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.controller.model.ResourceProductDTO;
import com.playmatsec.app.repository.model.ResourceProduct;
import com.playmatsec.app.service.ResourceProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResourceProductController {
    private final ResourceProductService resourceProductService;

    @GetMapping("/resource-products")
    public ResponseEntity<List<ResourceProduct>> getResourceProducts(@RequestHeader Map<String, String> headers) {
        log.info("headers: {}", headers);
        List<ResourceProduct> resourceProducts = resourceProductService.getResourceProducts();
        return ResponseEntity.ok(resourceProducts);
    }

    @GetMapping("/resource-products/{id}")
    public ResponseEntity<ResourceProduct> getResourceProductById(@RequestHeader Map<String, String> headers, 
                                                                 @PathVariable String id) {
        log.info("headers: {}", headers);
        ResourceProduct resourceProduct = resourceProductService.getResourceProductById(id);
        return resourceProduct != null ? ResponseEntity.ok(resourceProduct) : ResponseEntity.notFound().build();
    }

    @PostMapping("/resource-products")
    public ResponseEntity<ResourceProduct> createResourceProduct(@RequestHeader Map<String, String> headers, 
                                                                @RequestBody ResourceProductDTO resourceProduct) {
        log.info("headers: {}", headers);
        ResourceProduct createdResourceProduct = resourceProductService.createResourceProduct(resourceProduct);
        return createdResourceProduct != null ? ResponseEntity.ok(createdResourceProduct) : ResponseEntity.badRequest().build();
    }

    @PutMapping("/resource-products/{id}")
    public ResponseEntity<ResourceProduct> updateResourceProduct(@RequestHeader Map<String, String> headers, 
                                                                @PathVariable String id, 
                                                                @RequestBody ResourceProductDTO resourceProduct) {
        log.info("headers: {}", headers);
        ResourceProduct updatedResourceProduct = resourceProductService.updateResourceProduct(id, resourceProduct);
        return updatedResourceProduct != null ? ResponseEntity.ok(updatedResourceProduct) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/resource-products/{id}")
    public ResponseEntity<Boolean> deleteResourceProduct(@RequestHeader Map<String, String> headers, 
                                                        @PathVariable String id) {
        log.info("headers: {}", headers);
        boolean deleted = resourceProductService.deleteResourceProduct(id);
        return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @GetMapping("/products/{productId}/resource-products")
    public ResponseEntity<List<ResourceProduct>> getResourceProductsByProductId(@RequestHeader Map<String, String> headers, 
                                                                              @PathVariable String productId) {
        log.info("headers: {}", headers);
        List<ResourceProduct> resourceProducts = resourceProductService.getResourceProductsByProductId(productId);
        return resourceProducts != null ? ResponseEntity.ok(resourceProducts) : ResponseEntity.notFound().build();
    }

    @GetMapping("/products/{productId}/banners")
    public ResponseEntity<List<ResourceProduct>> getBannersByProductId(@RequestHeader Map<String, String> headers, 
                                                                     @PathVariable String productId) {
        log.info("headers: {}", headers);
        List<ResourceProduct> banners = resourceProductService.getBannersByProductId(productId);
        return banners != null ? ResponseEntity.ok(banners) : ResponseEntity.notFound().build();
    }

    @GetMapping("/resources/{resourceId}/resource-products")
    public ResponseEntity<List<ResourceProduct>> getResourceProductsByResourceId(@RequestHeader Map<String, String> headers, 
                                                                              @PathVariable String resourceId) {
        log.info("headers: {}", headers);
        List<ResourceProduct> resourceProducts = resourceProductService.getResourceProductsByResourceId(resourceId);
        return resourceProducts != null ? ResponseEntity.ok(resourceProducts) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/resources/{resourceId}/products/{productId}")
    public ResponseEntity<Boolean> deleteResourceProductByIds(@RequestHeader Map<String, String> headers, 
                                                             @PathVariable String resourceId, 
                                                             @PathVariable String productId) {
        log.info("headers: {}", headers);
        boolean deleted = resourceProductService.deleteResourceProductByResourceIdAndProductId(resourceId, productId);
        return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }
}
