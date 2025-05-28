package com.playmatsec.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.playmatsec.app.repository.model.State;

public interface StateJpaRepository extends JpaRepository<State, Integer>, JpaSpecificationExecutor<State> {
    // Custom query methods if needed
}
