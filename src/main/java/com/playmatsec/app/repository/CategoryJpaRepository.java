package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Category;

public interface CategoryJpaRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {
    // Custom query methods if needed
}
