package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.CartProduct;

public interface CartProductJpaRepository extends JpaRepository<CartProduct, Integer>, JpaSpecificationExecutor<CartProduct> {
}
