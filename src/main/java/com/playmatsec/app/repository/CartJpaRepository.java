package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Cart;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<Cart, UUID>, JpaSpecificationExecutor<Cart> {
    // Custom query methods if needed
}
