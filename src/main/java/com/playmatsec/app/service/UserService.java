package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.User;
import com.playmatsec.app.controller.model.UserDTO;

public interface UserService {
    List<User> getUsers(String provider, String providerId, String email, String name, String role);
    User getUserById(String id);
    User createUser(UserDTO user);
    User updateUser(String id, String updateRequest);
    User updateUser(String id, UserDTO user);
    Boolean deleteUser(String id);
}
