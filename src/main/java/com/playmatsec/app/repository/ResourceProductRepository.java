package com.playmatsec.app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.ResourceProduct;

@Repository
@RequiredArgsConstructor
public class ResourceProductRepository {
    private final ResourceProductJpaRepository repository;

    public List<ResourceProduct> getResourceProducts() {
        return repository.findAll();
    }

    public ResourceProduct getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public ResourceProduct save(ResourceProduct resourceProduct) {
        return repository.save(resourceProduct);
    }

    public void delete(ResourceProduct resourceProduct) {
        repository.delete(resourceProduct);
    }

    public List<ResourceProduct> findByProductId(UUID productId) {
        return repository.findByProductId(productId);
    }

    public List<ResourceProduct> findByResourceId(Integer resourceId) {
        return repository.findByResourceId(resourceId);
    }

    public ResourceProduct findByResourceIdAndProductId(Integer resourceId, UUID productId) {
        return repository.findByResourceIdAndProductId(resourceId, productId);
    }

    public void deleteByResourceIdAndProductId(Integer resourceId, UUID productId) {
        repository.deleteByResourceIdAndProductId(resourceId, productId);
    }
    
    public List<ResourceProduct> findByIsBanner(Boolean isBanner) {
        return repository.findByIsBanner(isBanner);
    }
}
