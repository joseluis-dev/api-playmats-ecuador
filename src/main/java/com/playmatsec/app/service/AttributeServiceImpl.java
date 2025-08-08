package com.playmatsec.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.AttributeDTO;
import com.playmatsec.app.repository.AttributeRepository;
import com.playmatsec.app.repository.model.Attribute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeServiceImpl implements AttributeService {
  private final AttributeRepository attributeRepository;
  private final ObjectMapper objectMapper;
  
  @Override
  public List<Attribute> getAttributes(
      String name,
      String value,
      String color,
      String createdAt,
      String updatedAt
  ) {
    if (StringUtils.hasLength(name)
        || StringUtils.hasLength(value)
        || StringUtils.hasLength(color)
        || createdAt != null
        || updatedAt != null) {

      LocalDateTime createdAtParsed = null;
      if (createdAt != null) {
        try {
          createdAtParsed = LocalDateTime.parse(createdAt);
        } catch (Exception e) {
          log.warn("createdAt no es una fecha válida: {}", createdAt);
        }
      }
      LocalDateTime updatedAtParsed = null;
      if (updatedAt != null) {
        try {
          updatedAtParsed = LocalDateTime.parse(updatedAt);
        } catch (Exception e) {
          log.warn("updatedAt no es una fecha válida: {}", updatedAt);
        }
      }
      return attributeRepository.search(name, value, color, createdAtParsed, updatedAtParsed);
    }
    List<Attribute> attributes = attributeRepository.getAttributes();
    return attributes.isEmpty() ? null : attributes;
  }

  @Override
  public Attribute getAttributeById(String id) {
    try {
      Long attributeId = Long.parseLong(id);
      return attributeRepository.getById(attributeId);
    } catch (NumberFormatException e) {
      log.error("Invalid attribute ID format: {}", id, e);
      return null;
    }
  }

  @Override
  public Attribute createAttribute(AttributeDTO request) {
    if (request != null
        && StringUtils.hasLength(request.getName().trim())
        && StringUtils.hasLength(request.getValue().trim())
        && StringUtils.hasLength(request.getColor().trim())) {
      Attribute attribute = objectMapper.convertValue(request, Attribute.class);
      attribute.setCreatedAt(LocalDateTime.now());
      return attributeRepository.save(attribute);
    }
    return null;
  }

  @Override
  public Attribute updateAttribute(String id, String request) {
    //PATCH se implementa en este caso mediante Merge Patch: https://datatracker.ietf.org/doc/html/rfc7386
		Attribute attribute = attributeRepository.getById(Long.valueOf(id));
		if (attribute != null) {
			try {
				JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
				JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(attribute)));
				Attribute patched = objectMapper.treeToValue(target, Attribute.class);
        patched.setUpdatedAt(LocalDateTime.now());
				attributeRepository.save(patched);
				return patched;
			} catch (JsonProcessingException | JsonPatchException e) {
				log.error("Error updating attribute {}", id, e);
                return null;
            }
        } else {
			return null;
		}
  }

  @Override
  public Attribute updateAttribute(String id, AttributeDTO request) {
    Attribute attribute = attributeRepository.getById(Long.valueOf(id));
    if (attribute != null) {
      request.setUpdatedAt(LocalDateTime.now());
      attribute.update(request);
      attributeRepository.save(attribute);
      return attribute;
    }
    return null;
  }

  @Override
  public Boolean deleteAttribute(String id) {
    try {
      Long attributeId = Long.parseLong(id);
      Attribute attribute = attributeRepository.getById(attributeId);
      if (attribute != null) {
        attributeRepository.delete(attribute);
        return true;
      }
    } catch (NumberFormatException e) {
      log.error("Invalid attribute ID format: {}", id, e);
    }
    return false;
  }
}
