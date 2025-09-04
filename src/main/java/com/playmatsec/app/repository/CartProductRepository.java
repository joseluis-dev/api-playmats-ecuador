package com.playmatsec.app.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.CartProduct;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.CartProductSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.CartProductConsts;

@Repository
@RequiredArgsConstructor
public class CartProductRepository {
    private final CartProductJpaRepository repository;

    public List<CartProduct> getCartProducts() {
        return repository.findAll();
    }

    public CartProduct getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public CartProduct save(CartProduct cartProduct) {
        return repository.save(cartProduct);
    }

    public void delete(CartProduct cartProduct) {
        repository.delete(cartProduct);
    }

    public List<CartProduct> search(String cart, String product, Integer quantity, BigDecimal price, BigDecimal subtotal, LocalDateTime createdAt) {
        CartProductSearchCriteria spec = new CartProductSearchCriteria();
        if (cart != null) {
            spec.add(new SearchStatement(CartProductConsts.CART + ".id", UUID.fromString(cart), SearchOperation.EQUAL));
        }
        if (product != null) {
            spec.add(new SearchStatement(CartProductConsts.PRODUCT, product, SearchOperation.EQUAL));
        }
        if (quantity != null) {
            spec.add(new SearchStatement(CartProductConsts.QUANTITY, quantity, SearchOperation.EQUAL));
        }
        if (price != null) {
            spec.add(new SearchStatement(CartProductConsts.PRICE, price, SearchOperation.EQUAL));
        }
        if (subtotal != null) {
            spec.add(new SearchStatement(CartProductConsts.SUBTOTAL, subtotal, SearchOperation.EQUAL));
        }
        if (createdAt != null) {
            spec.add(new SearchStatement(CartProductConsts.CREATED_AT, createdAt, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
