package com.playmatsec.app.repository;

import java.util.ArrayList;
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

    public List<Product> search(String name, String description, Double price, Boolean isCustomizable, String resourceFilter, String categoryFilter) {
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

        // categoryFilter soporta:
        //  - Lista de IDs: "1,2,3" (AND entre todas)
        //  - Color HEX: #abc / #a1b2c3 (igual exacto contra categories.color)
        //  - Texto: búsqueda parcial (ILIKE) en categories.name OR categories.description
        if (StringUtils.isNotBlank(categoryFilter)) {
            String filter = categoryFilter.trim();
            boolean processed = false;
            // Intentar como lista de IDs (AND semantics)
            String[] idParts = filter.split(",");
            boolean allInts = true;
            List<Integer> idList = new ArrayList<>();
            for (String p : idParts) {
                String t = p.trim();
                if (t.isEmpty()) continue;
                try {
                    idList.add(Integer.valueOf(t));
                } catch (NumberFormatException ex) {
                    allInts = false;
                    break;
                }
            }
            if (allInts && !idList.isEmpty()) {
                spec.add(new SearchStatement(ProductConsts.CATEGORIES_ID, idList, SearchOperation.IN_ALL));
                processed = true;
            }
            // Intentar como color HEX (#rgb o #rrggbb)
            if (!processed && filter.matches("(?i)^#?([0-9a-f]{3}|[0-9a-f]{6})$")) {
                String normalized = filter.startsWith("#") ? filter.toLowerCase() : "#" + filter.toLowerCase();
                spec.add(new SearchStatement(ProductConsts.CATEGORIES_COLOR, normalized, SearchOperation.EQUAL));
                processed = true;
            }
            // Texto libre => name OR description LIKE
            if (!processed) {
                String lower = filter.toLowerCase();
                // Guardamos el texto a buscar empaquetado en un SearchStatement especial
                // Usaremos una clave sintética para que el Specification construya el OR (lo añadiremos si no existe ya la lógica)
                spec.add(new SearchStatement(ProductConsts.CATEGORIES_NAME, lower, SearchOperation.MATCH));
                spec.add(new SearchStatement(ProductConsts.CATEGORIES_DESCRIPTION, lower, SearchOperation.MATCH));
                processed = true;
            }
        }
        return repository.findAll(spec);
    }
}
