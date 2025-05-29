package com.playmatsec.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.OrderProductDTO;
import com.playmatsec.app.repository.OrderProductRepository;
import com.playmatsec.app.repository.model.OrderProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProductServiceImpl implements OrderProductService {
    private final OrderProductRepository orderProductRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<OrderProduct> getOrderProducts(Integer orderId, Integer productId, Integer quantity) {
        return orderProductRepository.search(orderId, productId, quantity);
    }

    @Override
    public OrderProduct getOrderProductById(String id) {
        try {
            Integer orderProductId = Integer.parseInt(id);
            return orderProductRepository.getById(orderProductId);
        } catch (NumberFormatException e) {
            log.error("Invalid orderProduct ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public OrderProduct createOrderProduct(OrderProductDTO request) {
        if (request != null) {
            OrderProduct orderProduct = objectMapper.convertValue(request, OrderProduct.class);
            return orderProductRepository.save(orderProduct);
        }
        return null;
    }

    @Override
    public OrderProduct updateOrderProduct(String id, String request) {
        OrderProduct orderProduct = getOrderProductById(id);
        if (orderProduct != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(orderProduct)));
                OrderProduct patched = objectMapper.treeToValue(target, OrderProduct.class);
                orderProductRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating orderProduct {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public OrderProduct updateOrderProduct(String id, OrderProductDTO request) {
        OrderProduct orderProduct = getOrderProductById(id);
        if (orderProduct != null) {
            // orderProduct.update(request); // Implementar si existe método update
            orderProductRepository.save(orderProduct);
            return orderProduct;
        }
        return null;
    }

    @Override
    public Boolean deleteOrderProduct(String id) {
        try {
            Integer orderProductId = Integer.parseInt(id);
            OrderProduct orderProduct = orderProductRepository.getById(orderProductId);
            if (orderProduct != null) {
                orderProductRepository.delete(orderProduct);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid orderProduct ID format: {}", id, e);
        }
        return false;
    }
}
