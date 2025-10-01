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
import com.playmatsec.app.controller.model.StateDTO;
import com.playmatsec.app.repository.model.State;
import com.playmatsec.app.service.StateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Authorized
public class StatesController {
  private final StateService stateService;

  @GetMapping("/states")
  public ResponseEntity<List<State>> getStates(
    @RequestHeader Map<String, String> headers,
    @RequestParam(required = false) String nombre,
    @RequestParam(required = false) Integer countryId,
    HttpServletRequest request
  ) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    List<State> states = stateService.getStates(nombre, countryId);
    return states != null ? ResponseEntity.ok(states) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/states/{id}")
  public ResponseEntity<State> getStateById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    State state = stateService.getStateById(id);
    return state != null ? ResponseEntity.ok(state) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/states/{id}")
  public ResponseEntity<Boolean> deleteStateById(@RequestHeader Map<String, String> headers, @PathVariable String id, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    boolean deleted = stateService.deleteState(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/states")
  public ResponseEntity<State> createState(@RequestHeader Map<String, String> headers, @RequestBody StateDTO state, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    State createdState = stateService.createState(state);
    return createdState != null ? ResponseEntity.ok(createdState) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/states/{id}")
  public ResponseEntity<State> updateState(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    State updatedState = stateService.updateState(id, patchBody);
    return updatedState != null ? ResponseEntity.ok(updatedState) : ResponseEntity.notFound().build();
  }

  @PutMapping("/states/{id}")
  public ResponseEntity<State> replaceState(@RequestHeader Map<String, String> headers, @PathVariable String id, @RequestBody StateDTO state, HttpServletRequest request) {
    log.info("[{} {}] headers: {}", request.getMethod(), request.getRequestURI(), headers);
    State replacedState = stateService.updateState(id, state);
    return replacedState != null ? ResponseEntity.ok(replacedState) : ResponseEntity.notFound().build();
  }
}
