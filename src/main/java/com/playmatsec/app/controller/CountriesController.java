package com.playmatsec.app.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.playmatsec.app.config.Authorized;
import com.playmatsec.app.controller.model.CountryDTO;
import com.playmatsec.app.repository.model.Country;
import com.playmatsec.app.service.CountryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Authorized
public class CountriesController {
  private final CountryService countryService;

  @GetMapping("/countries")
  public ResponseEntity<List<Country>> getCountries(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String name,
    HttpServletRequest request
  ) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<Country> countries = countryService.getCountries(name);
    return countries != null ? ResponseEntity.ok(countries) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/countries/{id}")
  public ResponseEntity<Country> getCountryById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Country country = countryService.getCountryById(id);
    return country != null ? ResponseEntity.ok(country) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/countries/{id}")
  public ResponseEntity<Boolean> deleteCountryById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = countryService.deleteCountry(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/countries")
  public ResponseEntity<Country> createCountry(@RequestHeader Map<String, String> headers, @RequestBody CountryDTO country, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Country createdCountry = countryService.createCountry(country);
    return createdCountry != null ? ResponseEntity.ok(createdCountry) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/countries/{id}")
  public ResponseEntity<Country> updateCountry(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Country updatedCountry = countryService.updateCountry(id, patchBody);
    return updatedCountry != null ? ResponseEntity.ok(updatedCountry) : ResponseEntity.notFound().build();
  }

  @PutMapping("/countries/{id}")
  public ResponseEntity<Country> replaceCountry(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody CountryDTO country, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    Country replacedCountry = countryService.updateCountry(id, country);
    return replacedCountry != null ? ResponseEntity.ok(replacedCountry) : ResponseEntity.notFound().build();
  }
}
