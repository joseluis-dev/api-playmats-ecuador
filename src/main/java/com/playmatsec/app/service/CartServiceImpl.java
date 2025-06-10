package com.playmatsec.app.service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.CartDTO;
import com.playmatsec.app.repository.CartRepository;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.UserRepository;
import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Cart> getCarts(String userId, Integer quantity, String price, String subtotal, String createdAt, String updatedAt) {
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
        if (StringUtils.hasLength(userId)
            || createdAtParsed != null
            || updatedAtParsed != null
            || quantity != null
            || StringUtils.hasLength(price)
            || StringUtils.hasLength(subtotal)) {
            return cartRepository.search(userId, quantity, price, subtotal, createdAtParsed, updatedAtParsed);
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
        if (request != null && request.getUser() != null && request.getQuantity() != null && request.getPrice() != null && request.getSubtotal() != null) {
            Cart cart = objectMapper.convertValue(request, Cart.class);
            cart.setId(UUID.randomUUID());
            cart.setCreatedAt(LocalDateTime.now());
            if (request.getUser().getId() != null) {
                User user = userRepository.getById(request.getUser().getId());
                cart.setUser(user);
            }
            if (request.getProducts() != null && !request.getProducts().isEmpty()) {
                List<Product> products = request.getProducts().stream()
                    .map(p -> productRepository.getById(p.getId()))
                    .collect(Collectors.toList());
                cart.setProducts(products);
            }
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
                // Si el patch no incluye user o products, reasignar los originales
                if (patched.getUser() != null && patched.getUser().getId() != null) {
                    User user = userRepository.getById(patched.getUser().getId());
                    patched.setUser(user);
                } else {
                    patched.setUser(cart.getUser());
                }
                if (patched.getProducts() != null && !patched.getProducts().isEmpty()) {
                    List<Product> products = patched.getProducts().stream()
                        .map(p -> productRepository.getById(p.getId()))
                        .collect(Collectors.toList());
                    patched.setProducts(products);
                } else {
                    patched.setProducts(cart.getProducts());
                }
                patched.setId(cart.getId());
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
            if (request.getUser() != null && request.getUser().getId() != null) {
                User user = userRepository.getById(request.getUser().getId());
                cart.setUser(user);
            }
            if (request.getProducts() != null && !request.getProducts().isEmpty()) {
                List<Product> products = request.getProducts().stream()
                    .map(p -> productRepository.getById(p.getId()))
                    .collect(Collectors.toList());
                cart.setProducts(products);
            }
            cart.update(request);
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
