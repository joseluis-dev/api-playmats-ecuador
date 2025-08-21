package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.ResourceProduct;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceProductJpaRepository extends JpaRepository<ResourceProduct, Integer> {
    List<ResourceProduct> findByProductId(UUID productId);
    List<ResourceProduct> findByResourceId(Integer resourceId);
    ResourceProduct findByResourceIdAndProductId(Integer resourceId, UUID productId);
    void deleteByResourceIdAndProductId(Integer resourceId, UUID productId);
    List<ResourceProduct> findByIsBanner(Boolean isBanner);
}
