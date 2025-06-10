package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.UserDTO;
import com.playmatsec.app.repository.UserRepository;
import com.playmatsec.app.repository.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<User> getUsers(String provider, String providerId, String email, String name, String role, String status) {
        if (StringUtils.hasLength(provider) || StringUtils.hasLength(providerId) || StringUtils.hasLength(email) || StringUtils.hasLength(name) || StringUtils.hasLength(role) || StringUtils.hasLength(status)) {
            List<User> users = userRepository.search(provider, providerId, email, name, role, status);
            return users.isEmpty() ? null : users;
        }
        List<User> users = userRepository.getUsers();
        return users.isEmpty() ? null : users;
    }

    @Override
    public User getUserById(String id) {
        try {
            UUID userId = UUID.fromString(id);
            return userRepository.getById(userId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public User createUser(UserDTO request) {
        if (request != null && StringUtils.hasLength(request.getProvider()) && 
            StringUtils.hasLength(request.getProviderId()) && 
            StringUtils.hasLength(request.getEmail()) && 
            StringUtils.hasLength(request.getName()) &&
            StringUtils.hasLength(request.getAvatarUrl()) &&
            StringUtils.hasLength(request.getRole()) &&
            request.getStatus() != null
        ) {
            User user = objectMapper.convertValue(request, User.class);
            if (userRepository.getByEmail(request.getEmail()) != null) {
                log.warn("User with email {} already exists", request.getEmail());
                return null;
            }
            user.setId(UUID.randomUUID());
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }

    @Override
    public User updateUser(String id, String request) {
        User user = getUserById(id);
        if (user != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(user)));
                User patched = objectMapper.treeToValue(target, User.class);
                patched.setUpdatedAt(LocalDateTime.now());
                userRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating user {}", id, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public User updateUser(String id, UserDTO request) {
        User user = getUserById(id);
        if (user != null) {
            request.setUpdatedAt(LocalDateTime.now());
            user.update(request);
            userRepository.save(user);
            return user;
        }
        return null;
    }

    @Override
    public Boolean deleteUser(String id) {
        try {
            UUID userId = UUID.fromString(id);
            User user = userRepository.getById(userId);
            if (user != null) {
                userRepository.delete(user);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", id, e);
        }
        return false;
    }
}
