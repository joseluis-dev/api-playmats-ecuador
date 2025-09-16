package com.playmatsec.app.repository.utils.SearchCriteria;

import java.util.LinkedList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchOperation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Join;
import com.playmatsec.app.repository.model.Resource;
import java.util.Collection;

public class ResourceSearchCriteria implements Specification<Resource> {
    private final List<SearchStatement> list = new LinkedList<>();
    public void add(SearchStatement criteria) { list.add(criteria); }

    @SuppressWarnings("null")
    @Override
    public Predicate toPredicate(Root<Resource> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new LinkedList<>();
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
                predicates.add(builder.like(builder.lower(path.as(String.class)), "%" + criteria.getValue().toString().toLowerCase() + "%"));
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
                // Requiere que el recurso tenga TODOS los elementos de la colección en la relación indicada.
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
                        Join<Resource, ?> join = root.join(relation);
                        // where join.relField IN (:values)
                        jakarta.persistence.criteria.CriteriaBuilder.In<Object> inClause = builder.in(join.get(relField));
                        for (Object v : values) inClause.value(v);
                        predicates.add(inClause);
                        // group by resource id y having count(distinct relField) = values.size()
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
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
