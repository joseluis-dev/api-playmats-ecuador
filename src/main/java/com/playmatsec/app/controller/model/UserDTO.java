package com.playmatsec.app.controller.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDTO {
    private String provider;
    private String providerId;
    private String email;
    private String name;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String role;
    private String status;
    private LocalDateTime updatedAt;
}
