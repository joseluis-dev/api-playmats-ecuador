package com.playmatsec.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;
import com.playmatsec.app.repository.model.Country;

@RestController
@RequiredArgsConstructor
public class CountriesController {
  @GetMapping("/countries")
  public ResponseEntity<List<Country>> getCountries() {
    return ResponseEntity.ok(List.of(new Country()));
  }

  @GetMapping("/countries/{id}")
  public ResponseEntity<Country> getCountryById(@PathVariable Long id) {
    return ResponseEntity.ok(new Country());
  }

  @DeleteMapping("/countries/{id}")
  public ResponseEntity<Boolean> deleteCountryById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/countries")
  public ResponseEntity<Country> createCountry(@RequestBody Country country) {
    return ResponseEntity.ok(new Country());
  }

  @PatchMapping("/countries/{id}")
  public ResponseEntity<Country> updateCountry(@PathVariable Long id, @RequestBody Country country) {
    return ResponseEntity.ok(new Country());
  }

  @PutMapping("/countries/{id}")
  public ResponseEntity<Country> replaceCountry(@PathVariable Long id, @RequestBody Country country) {
    return ResponseEntity.ok(new Country());
  }
}
