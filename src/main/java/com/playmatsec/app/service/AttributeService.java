package com.playmatsec.app.service;

import java.util.List;

import com.playmatsec.app.controller.model.AttributeDTO;
import com.playmatsec.app.repository.model.Attribute;

public interface AttributeService {
  List<Attribute> getAttributes(
    String name,
    String description,
    String color,
    String createdAt,
    String updatedAt
  );
  Attribute getAttributeById(String id);
  Attribute createAttribute(AttributeDTO attribute);
  Attribute updateAttribute(String id, String updateRequest);
  Attribute updateAttribute(String id, AttributeDTO attribute);
  Boolean deleteAttribute(String id);
}
