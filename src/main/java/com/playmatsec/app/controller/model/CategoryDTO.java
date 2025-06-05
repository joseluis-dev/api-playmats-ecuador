package com.playmatsec.app.controller.model;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CategoryDTO {
    private UUID id;
    private String name;
    private String description;
    private String color;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
