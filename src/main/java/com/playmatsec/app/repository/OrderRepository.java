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

    public List<Order> search(String user, Date createdAt, Date updatedAt, String status, String totalAmount, String shippingAddress, String billingAddress, String payment) {
        OrderSearchCriteria spec = new OrderSearchCriteria();
        if (StringUtils.isNotBlank(user)) {
            spec.add(new SearchStatement(OrderConsts.USER + ".id", UUID.fromString(user), SearchOperation.EQUAL));
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
        if (StringUtils.isNotBlank(totalAmount)) {
            spec.add(new SearchStatement(OrderConsts.TOTAL_AMOUNT, totalAmount, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(shippingAddress)) {
            spec.add(new SearchStatement(OrderConsts.SHIPPING_ADDRESS + ".id", shippingAddress, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(billingAddress)) {
            spec.add(new SearchStatement(OrderConsts.BILLING_ADDRESS, billingAddress, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(payment)) {
            spec.add(new SearchStatement(OrderConsts.PAYMENT + ".id", payment, SearchOperation.MATCH));
        }
        return repository.findAll(spec);
    }
}
