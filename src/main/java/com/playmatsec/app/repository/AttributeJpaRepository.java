package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Attribute;

public interface AttributeJpaRepository extends JpaRepository<Attribute, Long>, JpaSpecificationExecutor<Attribute> {

    // Custom query methods can be defined here if needed
    // For example:
    // List<Attribute> findByName(String name);
    
    // You can also use Spring Data JPA's derived query methods
    // or define custom queries using @Query annotation.
  
}
