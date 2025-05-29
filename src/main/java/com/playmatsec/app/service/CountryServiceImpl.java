package com.playmatsec.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.CountryDTO;
import com.playmatsec.app.repository.CountryRepository;
import com.playmatsec.app.repository.model.Country;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {
    private final CountryRepository countryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Country> getCountries(String nombre) {
        if (StringUtils.hasLength(nombre)) {
            return countryRepository.search(nombre);
        }
        List<Country> countries = countryRepository.getCountries();
        return countries.isEmpty() ? null : countries;
    }

    @Override
    public Country getCountryById(String id) {
        try {
            Integer countryId = Integer.parseInt(id);
            return countryRepository.getById(countryId);
        } catch (NumberFormatException e) {
            log.error("Invalid country ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Country createCountry(CountryDTO request) {
        if (request != null && StringUtils.hasLength(request.getName())) {
            Country country = objectMapper.convertValue(request, Country.class);
            return countryRepository.save(country);
        }
        return null;
    }

    @Override
    public Country updateCountry(String id, String request) {
        Country country = getCountryById(id);
        if (country != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(country)));
                Country patched = objectMapper.treeToValue(target, Country.class);
                countryRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating country {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Country updateCountry(String id, CountryDTO request) {
        Country country = getCountryById(id);
        if (country != null) {
            // country.update(request); // Implementar si existe método update
            countryRepository.save(country);
            return country;
        }
        return null;
    }

    @Override
    public Boolean deleteCountry(String id) {
        try {
            Integer countryId = Integer.parseInt(id);
            Country country = countryRepository.getById(countryId);
            if (country != null) {
                countryRepository.delete(country);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid country ID format: {}", id, e);
        }
        return false;
    }
}
