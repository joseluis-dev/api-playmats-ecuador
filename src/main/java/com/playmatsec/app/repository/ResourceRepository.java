package com.playmatsec.app.repository;

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
                                List<String> categories) {

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
        // categories: lista de Integer vía categories.id (OR semantics)
        if (categories != null && !categories.isEmpty()) {
            // soportar "1,2,3" en un único valor
            List<Integer> categoryIds = new java.util.ArrayList<>();
            for (String c : categories) {
                if (c == null || c.isBlank()) continue;
                String[] parts = c.split(",");
                for (String p : parts) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            categoryIds.add(Integer.valueOf(trimmed));
                        } catch (NumberFormatException ex) {
                            // ignorar valores inválidos
                        }
                    }
                }
            }
            if (!categoryIds.isEmpty()) {
                spec.add(new SearchStatement(ResourceConsts.CATEGORIES_ID, categoryIds, SearchOperation.IN_ALL));
            }
        }
        return repository.findAll(spec);
    }
}
