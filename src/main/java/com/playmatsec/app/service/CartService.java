package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.controller.model.CartDTO;

public interface CartService {
    List<Cart> getCarts(String userId, String createdAt, String updatedAt);
    Cart getCartById(String id);
    Cart createCart(CartDTO cart);
    Cart updateCart(String id, String updateRequest);
    Cart updateCart(String id, CartDTO cart);
    Boolean deleteCart(String id);
}
