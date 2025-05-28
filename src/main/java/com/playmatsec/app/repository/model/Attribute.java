package com.playmatsec.app.repository.model;

import java.time.LocalDateTime;

import com.playmatsec.app.controller.model.AttributeDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void update(AttributeDTO updateRequest) {
        this.name = updateRequest.getName();
        this.description = updateRequest.getDescription();
        this.color = updateRequest.getColor();
        this.createdAt = updateRequest.getCreatedAt();
        this.updatedAt = updateRequest.getUpdatedAt();
    }
}
