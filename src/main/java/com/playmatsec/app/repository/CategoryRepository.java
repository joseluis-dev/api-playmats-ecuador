package com.playmatsec.app.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.Category;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.CategorySearchCriteria;
import com.playmatsec.app.repository.utils.Consts.CategoryConsts;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {
    private final CategoryJpaRepository repository;

    public List<Category> getCategories() {
        return repository.findAll();
    }

    public Category getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public Category save(Category category) {
        return repository.save(category);
    }

    public void delete(Category category) {
        repository.delete(category);
    }

    public List<Category> search(String name, String description, String color) {
        CategorySearchCriteria spec = new CategorySearchCriteria();
        if (StringUtils.isNotBlank(name)) {
            spec.add(new SearchStatement(CategoryConsts.NAME, name, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(description)) {
            spec.add(new SearchStatement(CategoryConsts.DESCRIPTION, description, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(color)) {
            spec.add(new SearchStatement(CategoryConsts.COLOR, color, SearchOperation.MATCH));
        }
        return repository.findAll(spec);
    }
}
