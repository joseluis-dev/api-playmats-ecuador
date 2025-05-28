package com.playmatsec.app.controller.model;

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
  private java.time.LocalDateTime createdAt;
  private java.time.LocalDateTime updatedAt;
}
