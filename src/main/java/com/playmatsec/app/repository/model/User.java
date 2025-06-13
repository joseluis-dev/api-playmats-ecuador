package com.playmatsec.app.repository.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.playmatsec.app.controller.model.UserDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private UUID id;

    private String provider;
    private String providerId;
    private String email;
    private String name;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "user")
    // @JsonManagedReference(value = "user-orders")
    @JsonIgnore
    private List<Order> orders;

    @OneToOne(mappedBy = "user")
    @JsonManagedReference(value = "user-cart")
    private Cart cart;

    @OneToMany(mappedBy = "user")
    private List<ShippingAddress> shippingAddresses;
    
    private LocalDateTime updatedAt;

    public void update (UserDTO updateRequest) {
        this.provider = updateRequest.getProvider();
        this.providerId = updateRequest.getProviderId();
        this.email = updateRequest.getEmail();
        this.name = updateRequest.getName();
        this.avatarUrl = updateRequest.getAvatarUrl();
        this.lastLogin = updateRequest.getLastLogin();
        this.role = updateRequest.getRole();
        this.status = Status.valueOf(updateRequest.getStatus());
        this.updatedAt = LocalDateTime.now();
    }
}

enum Status {
    ACTIVE, SUSPENDED
}
