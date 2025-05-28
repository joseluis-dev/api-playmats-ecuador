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
import com.playmatsec.app.repository.model.State;

@RestController
@RequiredArgsConstructor
public class StatesController {
  @GetMapping("/states")
  public ResponseEntity<List<State>> getStates() {
    return ResponseEntity.ok(List.of(new State()));
  }

  @GetMapping("/states/{id}")
  public ResponseEntity<State> getStateById(@PathVariable Long id) {
    return ResponseEntity.ok(new State());
  }

  @DeleteMapping("/states/{id}")
  public ResponseEntity<Boolean> deleteStateById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/states")
  public ResponseEntity<State> createState(@RequestBody State state) {
    return ResponseEntity.ok(new State());
  }

  @PatchMapping("/states/{id}")
  public ResponseEntity<State> updateState(@PathVariable Long id, @RequestBody State state) {
    return ResponseEntity.ok(new State());
  }

  @PutMapping("/states/{id}")
  public ResponseEntity<State> replaceState(@PathVariable Long id, @RequestBody State state) {
    return ResponseEntity.ok(new State());
  }
}
