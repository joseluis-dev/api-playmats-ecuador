package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.playmatsec.app.repository.ResourceProductRepository;
import com.playmatsec.app.repository.ResourceRepository;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.model.ResourceProduct;
import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.controller.model.ResourceProductDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceProductServiceImpl implements ResourceProductService {
    private final ResourceProductRepository resourceProductRepository;
    private final ResourceRepository resourceRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ResourceProduct> getResourceProducts() {
        List<ResourceProduct> resourceProducts = resourceProductRepository.getResourceProducts();
        return resourceProducts.isEmpty() ? null : resourceProducts;
    }

    @Override
    public ResourceProduct getResourceProductById(String id) {
        try {
            Integer resourceProductId = Integer.parseInt(id);
            return resourceProductRepository.getById(resourceProductId);
        } catch (NumberFormatException e) {
            log.error("Invalid resourceProduct ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public ResourceProduct createResourceProduct(ResourceProductDTO request) {
        if (request != null && request.getResourceId() != null && request.getProductId() != null) {
            try {
                Integer resourceId = Integer.parseInt(request.getResourceId());
                UUID productId = UUID.fromString(request.getProductId());
                
                Resource resource = resourceRepository.getById(resourceId);
                Product product = productRepository.getById(productId);
                
                if (resource != null && product != null) {
                    // Verificar si ya existe una relación
                    ResourceProduct existing = resourceProductRepository.findByResourceIdAndProductId(resourceId, productId);
                    if (existing != null) {
                        // Actualizar la relación existente
                        existing.setIsBanner(request.getIsBanner());
                        existing.setUpdatedAt(LocalDateTime.now());
                        return resourceProductRepository.save(existing);
                    } else {
                        // Crear nueva relación
                        ResourceProduct resourceProduct = new ResourceProduct();
                        resourceProduct.setResource(resource);
                        resourceProduct.setProduct(product);
                        resourceProduct.setIsBanner(request.getIsBanner());
                        resourceProduct.setCreatedAt(LocalDateTime.now());
                        resourceProduct.setUpdatedAt(LocalDateTime.now());
                        return resourceProductRepository.save(resourceProduct);
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("Error in ID format: resourceId={}, productId={}", request.getResourceId(), request.getProductId(), e);
            }
        }
        return null;
    }

    @Override
    public ResourceProduct updateResourceProduct(String id, ResourceProductDTO request) {
        ResourceProduct resourceProduct = getResourceProductById(id);
        if (resourceProduct != null && request != null) {
            if (request.getIsBanner() != null) {
                resourceProduct.setIsBanner(request.getIsBanner());
            }
            resourceProduct.setUpdatedAt(LocalDateTime.now());
            return resourceProductRepository.save(resourceProduct);
        }
        return null;
    }

    @Override
    public Boolean deleteResourceProduct(String id) {
        try {
            Integer resourceProductId = Integer.parseInt(id);
            ResourceProduct resourceProduct = resourceProductRepository.getById(resourceProductId);
            if (resourceProduct != null) {
                resourceProductRepository.delete(resourceProduct);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid resourceProduct ID format: {}", id, e);
        }
        return false;
    }

    @Override
    public List<ResourceProduct> getResourceProductsByProductId(String productId) {
        try {
            UUID pid = UUID.fromString(productId);
            return resourceProductRepository.findByProductId(pid);
        } catch (IllegalArgumentException e) {
            log.error("Invalid product ID format: {}", productId, e);
            return null;
        }
    }

    @Override
    public List<ResourceProduct> getResourceProductsByResourceId(String resourceId) {
        try {
            Integer rid = Integer.parseInt(resourceId);
            return resourceProductRepository.findByResourceId(rid);
        } catch (NumberFormatException e) {
            log.error("Invalid resource ID format: {}", resourceId, e);
            return null;
        }
    }

    @Override
    public ResourceProduct getResourceProductByResourceIdAndProductId(String resourceId, String productId) {
        try {
            Integer rid = Integer.parseInt(resourceId);
            UUID pid = UUID.fromString(productId);
            return resourceProductRepository.findByResourceIdAndProductId(rid, pid);
        } catch (IllegalArgumentException e) {
            log.error("Invalid ID format: resourceId={}, productId={}", resourceId, productId, e);
            return null;
        }
    }

    @Override
    public Boolean deleteResourceProductByResourceIdAndProductId(String resourceId, String productId) {
        try {
            Integer rid = Integer.parseInt(resourceId);
            UUID pid = UUID.fromString(productId);
            ResourceProduct resourceProduct = resourceProductRepository.findByResourceIdAndProductId(rid, pid);
            if (resourceProduct != null) {
                resourceProductRepository.delete(resourceProduct);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid ID format: resourceId={}, productId={}", resourceId, productId, e);
        }
        return false;
    }

    @Override
    public List<ResourceProduct> getBannersByProductId(String productId) {
        try {
            UUID pid = UUID.fromString(productId);
            List<ResourceProduct> resources = resourceProductRepository.findByProductId(pid);
            
            // Filtramos solo los banners
            return resources.stream()
                .filter(rp -> rp.getIsBanner() != null && rp.getIsBanner())
                .toList();
        } catch (IllegalArgumentException e) {
            log.error("Invalid product ID format: {}", productId, e);
            return null;
        }
    }
}
