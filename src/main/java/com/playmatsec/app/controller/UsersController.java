package com.playmatsec.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import com.playmatsec.app.controller.model.UserDTO;
import com.playmatsec.app.repository.model.User;
import com.playmatsec.app.service.UserService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UsersController {
  private final UserService userService;

  @GetMapping("/users")
  public ResponseEntity<List<User>> getUsers(
    @RequestHeader java.util.Map<String, String> headers,
    @RequestParam(required = false) String provider,
    @RequestParam(required = false) String providerId,
    @RequestParam(required = false) String email,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String role
  ) {
    log.info("headers: {}", headers);
    List<User> users = userService.getUsers(provider, providerId, email, name, role);
    return users != null ? ResponseEntity.ok(users) : ResponseEntity.ok(Collections.emptyList());
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<User> getUserById(@RequestHeader java.util.Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    User user = userService.getUserById(id);
    return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<Boolean> deleteUserById(@RequestHeader java.util.Map<String, String> headers, @PathVariable String id) {
    log.info("headers: {}", headers);
    boolean deleted = userService.deleteUser(id);
    return deleted ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
  }

  @PostMapping("/users")
  public ResponseEntity<User> createUser(@RequestHeader java.util.Map<String, String> headers, @RequestBody UserDTO user) {
    log.info("headers: {}", headers);
    User createdUser = userService.createUser(user);
    return createdUser != null ? ResponseEntity.ok(createdUser) : ResponseEntity.badRequest().build();
  }

  @PatchMapping("/users/{id}")
  public ResponseEntity<User> updateUser(@RequestHeader java.util.Map<String, String> headers, @PathVariable String id, @RequestBody String patchBody) {
    log.info("headers: {}", headers);
    User updatedUser = userService.updateUser(id, patchBody);
    return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<User> replaceUser(@RequestHeader java.util.Map<String, String> headers, @PathVariable String id, @RequestBody UserDTO user) {
    log.info("headers: {}", headers);
    User replacedUser = userService.updateUser(id, user);
    return replacedUser != null ? ResponseEntity.ok(replacedUser) : ResponseEntity.notFound().build();
  }
}
