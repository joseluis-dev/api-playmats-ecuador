package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.controller.model.CategoryDTO;

public interface CategoryService {
    List<Category> getCategories(String name, String description, String color);
    Category getCategoryById(String id);
    Category createCategory(CategoryDTO category);
    Category updateCategory(String id, String updateRequest);
    Category updateCategory(String id, CategoryDTO category);
    Boolean deleteCategory(String id);
}
