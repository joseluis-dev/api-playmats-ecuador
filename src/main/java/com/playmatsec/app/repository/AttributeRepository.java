package com.playmatsec.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.Attribute;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.Consts.AttributeConsts;
import com.playmatsec.app.repository.utils.SearchCriteria.AttributeSearchCriteria;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AttributeRepository {
  private final AttributeJpaRespository repository;

  public List<Attribute> getAttributes() {
    return repository.findAll();
  }

  public Attribute getById(Long id) {
    return repository.findById(id).orElse(null);
  }

  public Attribute save(Attribute attribute) {
    return repository.save(attribute);
  }

  public void delete(Attribute attribute) {
    repository.delete(attribute);
  }

  public List<Attribute> search(String name, String description, String color, LocalDateTime createdAt, LocalDateTime updatedAt) {
    AttributeSearchCriteria<Attribute> spec = new AttributeSearchCriteria<>();

    if (StringUtils.isNotBlank(name)) {
      spec.add(new SearchStatement(AttributeConsts.NAME, name, SearchOperation.MATCH));
    }

    if (StringUtils.isNotBlank(description)) {
      spec.add(new SearchStatement(AttributeConsts.DESCRIPTION, description, SearchOperation.MATCH));
    }

    if (StringUtils.isNotBlank(color)) {
      spec.add(new SearchStatement(AttributeConsts.COLOR, color, SearchOperation.EQUAL));
    }

    if (createdAt != null) {
      spec.add(new SearchStatement(AttributeConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
    }

    if (updatedAt != null) {
      spec.add(new SearchStatement(AttributeConsts.UPDATED_AT, updatedAt, SearchOperation.EQUAL));
    }

    return repository.findAll(spec);
  }
}
