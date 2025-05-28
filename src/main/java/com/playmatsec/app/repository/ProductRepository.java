package com.playmatsec.app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.ProductSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.ProductConsts;
import io.micrometer.common.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ProductRepository {
    private final ProductJpaRepository repository;

    public List<Product> getProducts() {
        return repository.findAll();
    }

    public Product getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Product save(Product product) {
        return repository.save(product);
    }

    public void delete(Product product) {
        repository.delete(product);
    }

    public List<Product> search(String name, String description, Double price, Boolean isCustomizable) {
        ProductSearchCriteria spec = new ProductSearchCriteria();
        if (StringUtils.isNotBlank(name)) {
            spec.add(new SearchStatement(ProductConsts.NAME, name, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(description)) {
            spec.add(new SearchStatement(ProductConsts.DESCRIPTION, description, SearchOperation.MATCH));
        }
        if (price != null) {
            spec.add(new SearchStatement(ProductConsts.PRICE, price, SearchOperation.EQUAL));
        }
        if (isCustomizable != null) {
            spec.add(new SearchStatement(ProductConsts.IS_CUSTOMIZABLE, isCustomizable, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
