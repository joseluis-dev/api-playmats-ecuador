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
import com.playmatsec.app.controller.model.CategoryDTO;
import com.playmatsec.app.repository.CategoryRepository;
import com.playmatsec.app.repository.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Category> getCategories(String name, String description, String color) {
        if (StringUtils.hasLength(name) || StringUtils.hasLength(description) || StringUtils.hasLength(color)) {
            return categoryRepository.search(name, description, color);
        }
        List<Category> categories = categoryRepository.getCategories();
        return categories.isEmpty() ? null : categories;
    }

    @Override
    public Category getCategoryById(String id) {
        try {
            Integer categoryId = Integer.parseInt(id);
            return categoryRepository.getById(categoryId);
        } catch (NumberFormatException e) {
            log.error("Invalid category ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Category createCategory(CategoryDTO request) {
        if (request != null && StringUtils.hasLength(request.getName())) {
            Category category = objectMapper.convertValue(request, Category.class);
            category.setCreatedAt(java.time.LocalDateTime.now());
            return categoryRepository.save(category);
        }
        return null;
    }

    @Override
    public Category updateCategory(String id, String request) {
        Category category = getCategoryById(id);
        if (category != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(category)));
                Category patched = objectMapper.treeToValue(target, Category.class);
                patched.setUpdatedAt(LocalDateTime.now());
                categoryRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating category {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Category updateCategory(String id, CategoryDTO request) {
        Category category = getCategoryById(id);
        if (category != null) {
            category.update(request);
            categoryRepository.save(category);
            return category;
        }
        return null;
    }

    @Override
    public Boolean deleteCategory(String id) {
        try {
            Integer categoryId = Integer.parseInt(id);
            Category category = categoryRepository.getById(categoryId);
            if (category != null) {
                categoryRepository.delete(category);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid category ID format: {}", id, e);
        }
        return false;
    }
}
