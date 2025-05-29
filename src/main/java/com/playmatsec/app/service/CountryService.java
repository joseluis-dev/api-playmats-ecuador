package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Country;
import com.playmatsec.app.controller.model.CountryDTO;

public interface CountryService {
    List<Country> getCountries(String nombre);
    Country getCountryById(String id);
    Country createCountry(CountryDTO country);
    Country updateCountry(String id, String updateRequest);
    Country updateCountry(String id, CountryDTO country);
    Boolean deleteCountry(String id);
}
