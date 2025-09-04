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

    public List<Cart> search(String user, BigDecimal total, Date createdAt, Date updatedAt) {
        UUID userId = null;
        if (user != null) {
            userId = UUID.fromString(user);
        }
        CartSearchCriteria spec = new CartSearchCriteria();
        if (StringUtils.isNotBlank(user)) {
            spec.add(new SearchStatement(CartConsts.USER + ".id", userId, SearchOperation.EQUAL));
        }
        if (createdAt != null) {
            spec.add(new SearchStatement(CartConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
        }
        if (updatedAt != null) {
            spec.add(new SearchStatement(CartConsts.UPDATED_AT, updatedAt, SearchOperation.EQUAL));
        }
        if (total != null) {
            spec.add(new SearchStatement(CartConsts.TOTAL, total, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
