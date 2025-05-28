package com.playmatsec.app.repository;

import java.util.List;

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

    public List<OrderProduct> search(Integer orderId, Integer productId, Integer quantity) {
        OrderProductSearchCriteria spec = new OrderProductSearchCriteria();
        if (orderId != null) {
            spec.add(new SearchStatement(OrderProductConsts.ORDER, orderId, SearchOperation.EQUAL));
        }
        if (productId != null) {
            spec.add(new SearchStatement(OrderProductConsts.PRODUCT, productId, SearchOperation.EQUAL));
        }
        if (quantity != null) {
            spec.add(new SearchStatement(OrderProductConsts.QUANTITY, quantity, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
