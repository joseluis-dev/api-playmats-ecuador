package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Order;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    // Custom query methods if needed
}
