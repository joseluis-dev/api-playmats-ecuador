package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Payment;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    // Custom query methods if needed
}
