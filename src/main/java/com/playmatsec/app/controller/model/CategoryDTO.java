package com.playmatsec.app.controller.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CategoryDTO {
    private String name;
    private String description;
    private String color;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
