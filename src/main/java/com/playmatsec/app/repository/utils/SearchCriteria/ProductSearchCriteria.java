package com.playmatsec.app.repository.utils.SearchCriteria;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.Consts.ProductConsts;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Join;
import com.playmatsec.app.repository.model.Product;

public class ProductSearchCriteria implements Specification<Product> {
    private final List<SearchStatement> list = new LinkedList<>();
    public void add(SearchStatement criteria) { list.add(criteria); }

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new LinkedList<>();
        List<Predicate> pendingResourceUrlLike = new LinkedList<>(); // agrupar url/thumbnail/watermark
        List<Predicate> pendingResourceNameHosting = new LinkedList<>(); // agrupar name/hosting
        // Para agrupar OR entre categories.name y categories.description cuando vienen del mismo texto
        List<Predicate> pendingCategoryText = new ArrayList<>();

        for (SearchStatement criteria : list) {
            String key = criteria.getKey();
            Path<?> path = root;
            if (key.contains(".")) {
                for (String part : key.split("\\.")) {
                    path = path.get(part);
                }
            } else {
                path = root.get(key);
            }
            if (criteria.getOperation().equals(SearchOperation.GREATER_THAN)) {
                predicates.add(builder.greaterThan(path.as(String.class), criteria.getValue().toString()));
            } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN)) {
                predicates.add(builder.lessThan(path.as(String.class), criteria.getValue().toString()));
            } else if (criteria.getOperation().equals(SearchOperation.GREATER_THAN_EQUAL)) {
                predicates.add(builder.greaterThanOrEqualTo(path.as(String.class), criteria.getValue().toString()));
            } else if (criteria.getOperation().equals(SearchOperation.LESS_THAN_EQUAL)) {
                predicates.add(builder.lessThanOrEqualTo(path.as(String.class), criteria.getValue().toString()));
            } else if (criteria.getOperation().equals(SearchOperation.NOT_EQUAL)) {
                predicates.add(builder.notEqual(path, criteria.getValue()));
            } else if (criteria.getOperation().equals(SearchOperation.EQUAL)) {
                predicates.add(builder.equal(path, criteria.getValue()));
            } else if (criteria.getOperation().equals(SearchOperation.MATCH)) {
                String likeVal = "%" + criteria.getValue().toString().toLowerCase() + "%";
                Predicate p = builder.like(builder.lower(path.as(String.class)), likeVal);
                String keyLower = key.toLowerCase();
                
                // Primero verificar si es un campo de categoría (más específico)
                boolean isCategoryName = key.equals(ProductConsts.CATEGORIES_NAME);
                boolean isCategoryDescription = key.equals(ProductConsts.CATEGORIES_DESCRIPTION);
                if (isCategoryName || isCategoryDescription) {
                    pendingCategoryText.add(p);
                }
                // Luego verificar campos de recursos URL
                else if (keyLower.endsWith(".url") || keyLower.endsWith(".thumbnail") || keyLower.endsWith(".watermark")) {
                    pendingResourceUrlLike.add(p);
                } 
                // Luego verificar campos de recursos name/hosting (excluyendo categories.name)
                else if ((keyLower.endsWith(".name") && !key.equals(ProductConsts.CATEGORIES_NAME)) || keyLower.endsWith(".hosting")) {
                    pendingResourceNameHosting.add(p);
                } 
                // Todo lo demás va a predicates generales
                else {
                    predicates.add(p);
                }
            } else if (criteria.getOperation().equals(SearchOperation.MATCH_END)) {
                predicates.add(builder.like(builder.lower(path.as(String.class)), criteria.getValue().toString().toLowerCase() + "%"));
            } else if (criteria.getOperation().equals(SearchOperation.IN)) {
                // Espera un Collection<?> como value
                Object val = criteria.getValue();
                if (val instanceof Collection<?>) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> values = (Collection<Object>) val;
                    jakarta.persistence.criteria.CriteriaBuilder.In<Object> inClause = builder.in(path);
                    for (Object v : values) {
                        inClause.value(v);
                    }
                    predicates.add(inClause);
                }
            } else if (criteria.getOperation().equals(SearchOperation.IN_ALL)) {
                // Requiere que el producto tenga TODOS los elementos de la colección en la relación indicada.
                // Caso soportado: categories.id
                Object val = criteria.getValue();
                if (val instanceof Collection<?>) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> values = (Collection<Object>) val;
                    // Detectar join por nombre de propiedad antes del primer punto
                    if (key.contains(".")) {
                        String[] parts = key.split("\\.");
                        String relation = parts[0];
                        String relField = parts[1];
                        Join<Product, ?> join = root.join(relation);
                        // where join.relField IN (:values)
                        jakarta.persistence.criteria.CriteriaBuilder.In<Object> inClause = builder.in(join.get(relField));
                        for (Object v : values) inClause.value(v);
                        predicates.add(inClause);
                        // group by product id y having count(distinct relField) = values.size()
                        query.groupBy(root.get("id"));
                        query.having(
                            builder.equal(
                                builder.countDistinct(join.get(relField)),
                                (long) values.size()
                            )
                        );
                    }
                }
            }
        }
        if (!pendingResourceUrlLike.isEmpty()) {
            predicates.add(builder.or(pendingResourceUrlLike.toArray(new Predicate[0])));
        }
        if (!pendingResourceNameHosting.isEmpty()) {
            predicates.add(builder.or(pendingResourceNameHosting.toArray(new Predicate[0])));
        }
        // Si se acumularon predicates para categories name/description, combinarlos con OR
        if (!pendingCategoryText.isEmpty()) {
            predicates.add(builder.or(pendingCategoryText.toArray(new Predicate[0])));
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
