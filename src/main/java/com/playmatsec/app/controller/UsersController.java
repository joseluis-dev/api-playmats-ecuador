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
import com.playmatsec.app.repository.model.User;

@RestController
@RequiredArgsConstructor
public class UsersController {
  @GetMapping("/users")
  public ResponseEntity<List<User>> getUsers() {
    return ResponseEntity.ok(List.of(new User()));
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(new User());
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<Boolean> deleteUserById(@PathVariable Long id) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/users")
  public ResponseEntity<User> createUser(@RequestBody User user) {
    return ResponseEntity.ok(new User());
  }

  @PatchMapping("/users/{id}")
  public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
    return ResponseEntity.ok(new User());
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<User> replaceUser(@PathVariable Long id, @RequestBody User user) {
    return ResponseEntity.ok(new User());
  }
}
