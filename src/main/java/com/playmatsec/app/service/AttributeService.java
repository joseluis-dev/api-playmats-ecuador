package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;

import com.playmatsec.app.repository.model.Attribute;

public interface AttributeService {
  List<Attribute> getAttributes(
    String name,
    String description,
    String color,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String sortBy,
    Integer page,
    Integer size
  );
}
