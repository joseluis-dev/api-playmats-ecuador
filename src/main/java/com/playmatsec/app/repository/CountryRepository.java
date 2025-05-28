package com.playmatsec.app.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.Country;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.CountrySearchCriteria;
import com.playmatsec.app.repository.utils.Consts.CountryConsts;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CountryRepository {
    private final CountryJpaRepository repository;

    public List<Country> getCountries() {
        return repository.findAll();
    }

    public Country getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public Country save(Country country) {
        return repository.save(country);
    }

    public void delete(Country country) {
        repository.delete(country);
    }

    public List<Country> search(String nombre) {
        CountrySearchCriteria spec = new CountrySearchCriteria();
        if (StringUtils.isNotBlank(nombre)) {
            spec.add(new SearchStatement(CountryConsts.NOMBRE, nombre, SearchOperation.MATCH));
        }
        return repository.findAll(spec);
    }
}
