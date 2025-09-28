package com.playmatsec.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.Resource;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.ResourceSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.ResourceConsts;
import io.micrometer.common.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ResourceRepository {
    private final ResourceJpaRepository repository;

    public List<Resource> getResources() {
        return repository.findAll();
    }

    public Resource getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public Resource save(Resource resource) {
        return repository.save(resource);
    }

    public void delete(Resource resource) {
        repository.delete(resource);
    }

    public List<Resource> search(String name,
                                String url,
                                String hosting,
                                String thumbnail,
                                String watermark,
                                String type,
                                Boolean isBanner,
                                String product,
                                String categoryFilter) {

        ResourceSearchCriteria spec = new ResourceSearchCriteria();

        if (StringUtils.isNotBlank(name)) {
            spec.add(new SearchStatement(ResourceConsts.NAME, name, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(url)) {
            spec.add(new SearchStatement(ResourceConsts.URL, url, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(hosting)) {
            spec.add(new SearchStatement(ResourceConsts.HOSTING, hosting, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(thumbnail)) {
            spec.add(new SearchStatement(ResourceConsts.THUMBNAIL, thumbnail, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(watermark)) {
            spec.add(new SearchStatement(ResourceConsts.WATERMARK, watermark, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(type)) {
            spec.add(new SearchStatement(ResourceConsts.TYPE, type, SearchOperation.EQUAL));
        }
        // isBanner pertenece a ResourceProduct
        if (isBanner != null) {
            spec.add(new SearchStatement(ResourceConsts.RESOURCE_PRODUCTS_IS_BANNER, isBanner, SearchOperation.EQUAL));
        }
        // product: UUID vía resourceProducts.product.id
        if (StringUtils.isNotBlank(product)) {
            spec.add(new SearchStatement(ResourceConsts.RESOURCE_PRODUCTS_PRODUCT_ID, UUID.fromString(product), SearchOperation.EQUAL));
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
                spec.add(new SearchStatement(ResourceConsts.CATEGORIES_ID, idList, SearchOperation.IN_ALL));
                processed = true;
            }
            // Intentar como color HEX (#rgb o #rrggbb)
            if (!processed && filter.matches("(?i)^#?([0-9a-f]{3}|[0-9a-f]{6})$")) {
                String normalized = filter.startsWith("#") ? filter.toLowerCase() : "#" + filter.toLowerCase();
                spec.add(new SearchStatement(ResourceConsts.CATEGORIES_COLOR, normalized, SearchOperation.EQUAL));
                processed = true;
            }
            // Texto libre => name OR description LIKE
            if (!processed) {
                String lower = filter.toLowerCase();
                // Guardamos el texto a buscar empaquetado en un SearchStatement especial
                // Usaremos una clave sintética para que el Specification construya el OR (lo añadiremos si no existe ya la lógica)
                spec.add(new SearchStatement(ResourceConsts.CATEGORIES_NAME, lower, SearchOperation.MATCH));
                spec.add(new SearchStatement(ResourceConsts.CATEGORIES_DESCRIPTION, lower, SearchOperation.MATCH));
                processed = true;
            }
        }
        return repository.findAll(spec);
    }
}
