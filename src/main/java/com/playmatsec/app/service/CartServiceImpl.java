package com.playmatsec.app.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.playmatsec.app.repository.UserRepository;
import com.playmatsec.app.repository.model.Cart;
import com.playmatsec.app.repository.model.CartProduct;
import com.playmatsec.app.repository.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Cart> getCarts(String user, BigDecimal total, String createdAt, String updatedAt) {
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
        if (StringUtils.hasLength(user)
            || createdAtParsed != null
            || updatedAtParsed != null
            || total != null) {
            log.info("Searching carts with criteria - user: {}, total: {}, createdAt: {}, updatedAt: {}",
                user, total, createdAtParsed, updatedAtParsed);
            return cartRepository.search(user, total, createdAtParsed, updatedAtParsed);
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
        if (request != null && request.getUser() != null) {
            Cart cart = objectMapper.convertValue(request, Cart.class);
            cart.setId(UUID.randomUUID());
            LocalDateTime now = LocalDateTime.now();
            cart.setCreatedAt(now);
            // cart.setUpdatedAt(now);
            if (request.getUser().getId() != null) {
                User user = userRepository.getById(request.getUser().getId());
                cart.setUser(user);
            }
            // ensure timestamps/back-references and compute subtotal & total
            BigDecimal total = BigDecimal.ZERO;
            if (cart.getCartProducts() != null && !cart.getCartProducts().isEmpty()) {
                for (var cp : cart.getCartProducts()) {
                    cp.setCart(cart);
                    if (cp.getCreatedAt() == null) cp.setCreatedAt(now);
                    // cp.setUpdatedAt(now);
                    BigDecimal price = cp.getPrice() != null ? cp.getPrice() : BigDecimal.ZERO;
                    int qty = cp.getQuantity() != null ? cp.getQuantity() : 0;
                    BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
                    cp.setSubtotal(subtotal);
                    total = total.add(subtotal);
                }
            }
            cart.setTotal(total);
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
                if (patched.getUser() != null && patched.getUser().getId() != null) {
                    User user = userRepository.getById(patched.getUser().getId());
                    patched.setUser(user);
                } else {
                    patched.setUser(cart.getUser());
                }
                // Merge cartProducts by product: update existing, add new, remove missing
                LocalDateTime now = LocalDateTime.now();
                BigDecimal total = BigDecimal.ZERO;
                if (patched.getCartProducts() != null) {
                    // Index existing by productId
                    Map<UUID, CartProduct> existingByProduct = new HashMap<>();
                    if (cart.getCartProducts() != null) {
                        for (var existing : cart.getCartProducts()) {
                            if (existing.getProduct() != null && existing.getProduct().getId() != null) {
                                existingByProduct.put(existing.getProduct().getId(), existing);
                            }
                        }
                    }

                    // Track incoming product ids to support deletions
                    Set<UUID> incomingProductIds = new HashSet<>();

                    List<CartProduct> merged = new ArrayList<>();
                    for (var incoming : patched.getCartProducts()) {
                        UUID pid = (incoming.getProduct() != null) ? incoming.getProduct().getId() : null;
                        if (pid == null) { continue; }
                        incomingProductIds.add(pid);
                        CartProduct toUse = existingByProduct.get(pid);
                        if (toUse != null) {
                            // update existing
                            Integer qty = incoming.getQuantity() != null ? incoming.getQuantity() : 0;
                            BigDecimal price = incoming.getPrice() != null ? incoming.getPrice() : BigDecimal.ZERO;
                            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
                            toUse.setQuantity(qty);
                            toUse.setPrice(price);
                            toUse.setSubtotal(subtotal);
                            toUse.setUpdatedAt(now);
                            toUse.setCart(patched);
                            merged.add(toUse);
                            total = total.add(subtotal);
                        } else {
                            // new item
                            Integer qty = incoming.getQuantity() != null ? incoming.getQuantity() : 0;
                            BigDecimal price = incoming.getPrice() != null ? incoming.getPrice() : BigDecimal.ZERO;
                            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
                            incoming.setCart(patched);
                            if (incoming.getCreatedAt() == null) incoming.setCreatedAt(now);
                            incoming.setUpdatedAt(now);
                            incoming.setSubtotal(subtotal);
                            merged.add(incoming);
                            total = total.add(subtotal);
                        }
                    }
                    // Items not present in incoming will be removed by orphanRemoval when we set the merged list
                    patched.setCartProducts(merged);
                } else {
                    // No changes in items: keep existing and recompute total
                    patched.setCartProducts(cart.getCartProducts());
                    if (patched.getCartProducts() != null) {
                        for (var cp : patched.getCartProducts()) {
                            BigDecimal price = cp.getPrice() != null ? cp.getPrice() : BigDecimal.ZERO;
                            int qty = cp.getQuantity() != null ? cp.getQuantity() : 0;
                            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
                        }
                    }
                }
                patched.setTotal(total);
                patched.setId(cart.getId());
                patched.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(patched);
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
        if (cart == null) return null;

        if (request.getUser() != null && request.getUser().getId() != null) {
            User user = userRepository.getById(request.getUser().getId());
            cart.setUser(user);
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal total = BigDecimal.ZERO;

        // Trabajar SIEMPRE sobre la colección gestionada para evitar el error de orphanRemoval
        List<CartProduct> managedItems = cart.getCartProducts();
        if (managedItems == null) {
            managedItems = new ArrayList<>();
            cart.setCartProducts(managedItems);
        }

        if (request.getCartProducts() != null) {
            // Indexar existentes por productId
            Map<UUID, CartProduct> existingByProduct = new HashMap<>();
            for (var existing : managedItems) {
                if (existing.getProduct() != null && existing.getProduct().getId() != null) {
                    existingByProduct.put(existing.getProduct().getId(), existing);
                }
            }

            // Construir set de productIds entrantes (para eliminar los faltantes)
            Set<UUID> incomingIds = new HashSet<>();

            // Actualizar existentes y crear nuevos
            for (var incoming : request.getCartProducts()) {
                UUID pid = (incoming.getProduct() != null) ? incoming.getProduct().getId() : null;
                if (pid == null) continue;
                incomingIds.add(pid);

                Integer qty = incoming.getQuantity() != null ? incoming.getQuantity() : 0;
                BigDecimal price = incoming.getPrice() != null ? incoming.getPrice() : BigDecimal.ZERO;
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

                CartProduct existing = existingByProduct.get(pid);
                if (existing != null) {
                    // Actualizar registro existente
                    existing.setQuantity(qty);
                    existing.setPrice(price);
                    existing.setSubtotal(subtotal);
                    existing.setUpdatedAt(now);
                    existing.setCart(cart);
                } else {
                    // Crear registro nuevo
                    CartProduct newItem = new CartProduct();
                    newItem.setCart(cart);
                    newItem.setProduct(incoming.getProduct()); // asume product con id válido
                    newItem.setQuantity(qty);
                    newItem.setPrice(price);
                    newItem.setSubtotal(subtotal);
                    newItem.setCreatedAt(now);
                    newItem.setUpdatedAt(now);
                    managedItems.add(newItem);
                }
                total = total.add(subtotal);
            }

            // Eliminar los que ya no vienen en la request (in-place, sin reemplazar la lista)
            managedItems.removeIf(cp -> {
                UUID pid = (cp.getProduct() != null) ? cp.getProduct().getId() : null;
                return pid != null && !incomingIds.contains(pid);
            });
        } else {
            // Sin cambios en items; recomputar total desde existentes
            for (var cp : managedItems) {
                BigDecimal price = cp.getPrice() != null ? cp.getPrice() : BigDecimal.ZERO;
                int qty = cp.getQuantity() != null ? cp.getQuantity() : 0;
                total = total.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }

        cart.setTotal(total);
        cart.setUpdatedAt(now);
        return cartRepository.save(cart);
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
