package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.OrderProduct;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Integer>, JpaSpecificationExecutor<OrderProduct> {
    // Custom query methods if needed
}
