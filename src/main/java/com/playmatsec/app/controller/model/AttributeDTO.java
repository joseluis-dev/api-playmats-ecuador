package com.playmatsec.app.controller.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AttributeDTO {
  private String name;
  private String description;
  private String color;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
