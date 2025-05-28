package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.ShippingAddress;

public interface ShippingAddressJpaRepository extends JpaRepository<ShippingAddress, Integer>, JpaSpecificationExecutor<ShippingAddress> {
    // Custom query methods if needed
}
