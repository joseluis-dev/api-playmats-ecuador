package com.playmatsec.app.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.OrderProduct;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.OrderProductSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.OrderProductConsts;

@Repository
@RequiredArgsConstructor
public class OrderProductRepository {
    private final OrderProductJpaRepository repository;

    public List<OrderProduct> getOrderProducts() {
        return repository.findAll();
    }

    public OrderProduct getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public OrderProduct save(OrderProduct orderProduct) {
        return repository.save(orderProduct);
    }

    public void delete(OrderProduct orderProduct) {
        repository.delete(orderProduct);
    }

    public List<OrderProduct> search(String order, String product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal, LocalDateTime createdAt) {
        OrderProductSearchCriteria spec = new OrderProductSearchCriteria();
        if (order != null) {
            // Buscar por el id de la orden (UUID)
            spec.add(new SearchStatement(OrderProductConsts.ORDER + ".id", UUID.fromString(order), SearchOperation.EQUAL));
        }
        if (product != null) {
            spec.add(new SearchStatement(OrderProductConsts.PRODUCT, product, SearchOperation.EQUAL));
        }
        if (quantity != null) {
            spec.add(new SearchStatement(OrderProductConsts.QUANTITY, quantity, SearchOperation.EQUAL));
        }
        if (unitPrice != null) {
            spec.add(new SearchStatement(OrderProductConsts.UNIT_PRICE, unitPrice, SearchOperation.EQUAL));
        }
        if (subtotal != null) {
            spec.add(new SearchStatement(OrderProductConsts.SUBTOTAL, subtotal, SearchOperation.EQUAL));
        }
        if (createdAt != null) {
            spec.add(new SearchStatement(OrderProductConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
