package com.playmatsec.app.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.State;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.StateSearchCriteria;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StateRepository {
    private final StateJpaRepository repository;

    public List<State> getStates() {
        return repository.findAll();
    }

    public State getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public State save(State state) {
        return repository.save(state);
    }

    public void delete(State state) {
        repository.delete(state);
    }

    public List<State> search(String nombre, Integer countryId) {
        StateSearchCriteria spec = new StateSearchCriteria();
        if (StringUtils.isNotBlank(nombre)) {
            spec.add(new SearchStatement("nombre", nombre, SearchOperation.MATCH));
        }
        if (countryId != null) {
            spec.add(new SearchStatement("country.id", countryId, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
