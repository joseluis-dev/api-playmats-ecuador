package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Resource;

public interface ResourceJpaRepository extends JpaRepository<Resource, Integer>, JpaSpecificationExecutor<Resource> {
    // Custom query methods if needed
}
