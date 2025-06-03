package com.playmatsec.app.repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.OrderSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.OrderConsts;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final OrderJpaRepository repository;

    public List<Order> getOrders() {
        return repository.findAll();
    }

    public Order getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Order save(Order order) {
        return repository.save(order);
    }

    public void delete(Order order) {
        repository.delete(order);
    }

    public List<Order> search(String userId, Date createdAt, Date updatedAt, String status) {
        OrderSearchCriteria spec = new OrderSearchCriteria();
        if (StringUtils.isNotBlank(userId)) {
            spec.add(new SearchStatement(OrderConsts.USER, userId, SearchOperation.EQUAL));
        }
        if (createdAt != null) {
            spec.add(new SearchStatement(OrderConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
        }
        if (updatedAt != null) {
            spec.add(new SearchStatement(OrderConsts.UPDATED_AT, updatedAt, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(status)) {
            spec.add(new SearchStatement(OrderConsts.STATUS, status, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
