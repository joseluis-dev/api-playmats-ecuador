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
import com.playmatsec.app.controller.model.OrderDTO;
import com.playmatsec.app.repository.OrderRepository;
import com.playmatsec.app.repository.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Order> getOrders(String userId, String createdAt, String updatedAt, String status) {
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
        if (StringUtils.hasLength(userId) || createdAtParsed != null || updatedAtParsed != null || StringUtils.hasLength(status)) {
            return orderRepository.search(userId, createdAtParsed, updatedAtParsed, status);
        }
        List<Order> orders = orderRepository.getOrders();
        return orders.isEmpty() ? null : orders;
    }

    @Override
    public Order getOrderById(String id) {
        try {
            UUID orderId = UUID.fromString(id);
            return orderRepository.getById(orderId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Order createOrder(OrderDTO request) {
        if (request != null && request.getUser() != null) {
            Order order = objectMapper.convertValue(request, Order.class);
            order.setCreatedAt(LocalDateTime.now());
            return orderRepository.save(order);
        }
        return null;
    }

    @Override
    public Order updateOrder(String id, String request) {
        Order order = getOrderById(id);
        if (order != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(order)));
                Order patched = objectMapper.treeToValue(target, Order.class);
                patched.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating order {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Order updateOrder(String id, OrderDTO request) {
        Order order = getOrderById(id);
        if (order != null) {
            // order.update(request); // Implementar si existe método update
            orderRepository.save(order);
            return order;
        }
        return null;
    }

    @Override
    public Boolean deleteOrder(String id) {
        try {
            UUID orderId = UUID.fromString(id);
            Order order = orderRepository.getById(orderId);
            if (order != null) {
                orderRepository.delete(order);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID format: {}", id, e);
        }
        return false;
    }
}
