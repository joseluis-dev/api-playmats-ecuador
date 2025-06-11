package com.playmatsec.app.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.Consts.CartConsts;
import com.playmatsec.app.repository.utils.SearchCriteria.CartSearchCriteria;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepository {
    private final CartJpaRepository repository;

    public List<Cart> getCarts() {
        return repository.findAll();
    }

    public Cart getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Cart save(Cart cart) {
        return repository.save(cart);
    }

    public void delete(Cart cart) {
        repository.delete(cart);
    }

    public List<Cart> search(String userId, Integer quantity, BigDecimal price, BigDecimal subtotal, Date createdAt, Date updatedAt) {
        CartSearchCriteria spec = new CartSearchCriteria();
        if (StringUtils.isNotBlank(userId)) {
            spec.add(new SearchStatement(CartConsts.USER, userId, SearchOperation.EQUAL));
        }
        if (createdAt != null) {
            spec.add(new SearchStatement(CartConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
        }
        if (updatedAt != null) {
            spec.add(new SearchStatement(CartConsts.UPDATED_AT, updatedAt, SearchOperation.EQUAL));
        }
        if (quantity != null) {
            spec.add(new SearchStatement(CartConsts.QUANTITY, quantity, SearchOperation.EQUAL));
        }
        if (price != null) {
            spec.add(new SearchStatement(CartConsts.PRICE, price, SearchOperation.EQUAL));
        }
        if (subtotal != null) {
            spec.add(new SearchStatement(CartConsts.SUBTOTAL, subtotal, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
