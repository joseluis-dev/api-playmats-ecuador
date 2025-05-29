package com.playmatsec.app.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.CartDTO;
import com.playmatsec.app.repository.CartRepository;
import com.playmatsec.app.repository.model.Cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Cart> getCarts(String userId, String createdAt, String updatedAt) {
        Date createdAtParsed = null;
        Date updatedAtParsed = null;
        if (createdAt != null) {
            try {
                createdAtParsed = Date.valueOf(createdAt);
            } catch (Exception e) {
                log.warn("createdAt no es una fecha válida: {}", createdAt);
            }
        }
        if (updatedAt != null) {
            try {
                updatedAtParsed = Date.valueOf(updatedAt);
            } catch (Exception e) {
                log.warn("updatedAt no es una fecha válida: {}", updatedAt);
            }
        }
        if (StringUtils.hasLength(userId) || createdAtParsed != null || updatedAtParsed != null) {
            return cartRepository.search(userId, createdAtParsed, updatedAtParsed);
        }
        List<Cart> carts = cartRepository.getCarts();
        return carts.isEmpty() ? null : carts;
    }

    @Override
    public Cart getCartById(String id) {
        try {
            UUID cartId = UUID.fromString(id);
            return cartRepository.getById(cartId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid cart ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Cart createCart(CartDTO request) {
        if (request != null && request.getUser() != null && request.getQuantity() != null) {
            Cart cart = objectMapper.convertValue(request, Cart.class);
            cart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        }
        return null;
    }

    @Override
    public Cart updateCart(String id, String request) {
        Cart cart = getCartById(id);
        if (cart != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(cart)));
                Cart patched = objectMapper.treeToValue(target, Cart.class);
                patched.setUpdatedAt(LocalDateTime.now());
                cartRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating cart {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Cart updateCart(String id, CartDTO request) {
        Cart cart = getCartById(id);
        if (cart != null) {
            request.setUpdatedAt(LocalDateTime.now());
            // Aquí deberías tener un método update en Cart para aplicar los cambios del DTO
            // cart.update(request);
            cartRepository.save(cart);
            return cart;
        }
        return null;
    }

    @Override
    public Boolean deleteCart(String id) {
        try {
            UUID cartId = UUID.fromString(id);
            Cart cart = cartRepository.getById(cartId);
            if (cart != null) {
                cartRepository.delete(cart);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid cart ID format: {}", id, e);
        }
        return false;
    }
}
