package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Product;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    // Custom query methods if needed
}
