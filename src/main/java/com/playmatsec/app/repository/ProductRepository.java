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

    public List<Product> search(String name, String description, Double price, Boolean isCustomizable, String resourceFilter) {
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
        // Filtro unificado por recurso asociado (cualquiera de sus campos principales)
        if (StringUtils.isNotBlank(resourceFilter)) {
            String trimmed = resourceFilter.trim();
            boolean handled = false;
            // 1. Intentar ID numérico del recurso
            try {
                Integer rid = Integer.valueOf(trimmed);
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_ID, rid, SearchOperation.EQUAL));
                handled = true;
            } catch (NumberFormatException ignored) { }

            // 2. Detectar si parece publicId (empieza por "resources/")
            if (!handled && trimmed.startsWith("resources/")) {
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_PUBLIC_ID, trimmed, SearchOperation.MATCH));
                handled = true;
            }

            // 3. Detectar si es URL (http/https)
            if (!handled && (trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
                // Aplicar sobre url, thumbnail y watermark (OR se gestionará en criteria)
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_URL, trimmed, SearchOperation.MATCH));
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_THUMBNAIL, trimmed, SearchOperation.MATCH));
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_WATERMARK, trimmed, SearchOperation.MATCH));
                handled = true;
            }

            // 4. Si no se manejó aún, aplicar como texto sobre name y hosting
            if (!handled) {
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_NAME, trimmed, SearchOperation.MATCH));
                spec.add(new SearchStatement(ProductConsts.RESOURCE_PRODUCTS_RESOURCE_HOSTING, trimmed, SearchOperation.MATCH));
            }
        }
        return repository.findAll(spec);
    }
}
