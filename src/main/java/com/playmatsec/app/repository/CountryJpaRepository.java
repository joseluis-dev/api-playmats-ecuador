package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.Country;

public interface CountryJpaRepository extends JpaRepository<Country, Integer>, JpaSpecificationExecutor<Country> {
    // Custom query methods if needed
}
