package com.playmatsec.app.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.playmatsec.app.repository.OrderProductRepository;
import com.playmatsec.app.repository.OrderRepository;
import com.playmatsec.app.repository.PaymentRepository;
import com.playmatsec.app.repository.ProductRepository;
import com.playmatsec.app.repository.ShippingAddressRepository;
import com.playmatsec.app.repository.UserRepository;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.model.OrderProduct;
import com.playmatsec.app.repository.model.Payment;
import com.playmatsec.app.repository.model.Product;
import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.repository.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRespository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Order> getOrders(String user, String createdAt, String updatedAt, String status, String totalAmount, String shippingAddress, String billingAddress, String payment) {
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
        if (StringUtils.hasLength(user) || createdAtParsed != null || updatedAtParsed != null || StringUtils.hasLength(status) || StringUtils.hasLength(totalAmount) || StringUtils.hasLength(shippingAddress) || StringUtils.hasLength(billingAddress) || StringUtils.hasLength(payment)) {
            return orderRepository.search(user, createdAtParsed, updatedAtParsed, status, totalAmount, shippingAddress, billingAddress, payment);
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
    public List<OrderProduct> getOrderProductsByOrderId(String orderId) {
        if (orderId != null) {
            return orderProductRepository.search(orderId, null, null, null, null, null);
        }
        return List.of();
    }

    @Override
    public List<Product> getProductsByOrderId(String orderId) {
        List<OrderProduct> orderProducts = getOrderProductsByOrderId(orderId);
        List<Product> products = new ArrayList<>();
        for (OrderProduct op : orderProducts) {
            if (op.getProduct() != null) {
                products.add(op.getProduct());
            }
        }
        return products;
    }

    @Override
    public Order createOrder(OrderDTO request) {
        if (request != null && request.getUser() != null) {
            Order order = objectMapper.convertValue(request, Order.class);
            if (request.getUser().getId() != null) {
                User user = userRespository.getById(request.getUser().getId());
                order.setUser(user);
            }
            if (request.getShippingAddress() != null && request.getShippingAddress().getId() != null) {
                ShippingAddress shippingAddress = shippingAddressRepository.getById(request.getShippingAddress().getId());
                order.setShippingAddress(shippingAddress);
            }
            order.setId(UUID.randomUUID());
            // Manejar correctamente los OrderProducts
            if (request.getOrderProducts() != null) {
                for (int i = 0; i < request.getOrderProducts().size(); i++) {
                    OrderProduct opDTO = request.getOrderProducts().get(i);
                    if (opDTO.getProduct() == null || opDTO.getProduct().getId() == null) {
                        throw new IllegalArgumentException("Cada orderProduct debe incluir product.id");
                    }
                    Product product = productRepository.getById(opDTO.getProduct().getId());
                    if (product == null) {
                        throw new IllegalArgumentException("Producto no encontrado: " + opDTO.getProduct().getId());
                    }
                    OrderProduct op = new OrderProduct();
                    op.setProduct(product);
                    op.setOrder(order);
                    op.setQuantity(opDTO.getQuantity());
                    op.setUnitPrice(product.getPrice());
                    op.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(opDTO.getQuantity())));
                    op.setCreatedAt(LocalDateTime.now());
                    if (order.getOrderProducts() == null || order.getOrderProducts().size() <= i) {
                        if (order.getOrderProducts() == null) order.setOrderProducts(new ArrayList<>());
                        order.getOrderProducts().add(op);
                    } else {
                        order.getOrderProducts().set(i, op);
                    }
                }
            }
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
                // Si el patch no incluye user, conservar el original
                if (patched.getUser() == null) {
                    patched.setUser(order.getUser());
                } else if (patched.getUser().getId() != null) {
                    User user = userRespository.getById(patched.getUser().getId());
                    patched.setUser(user);
                }
                // Si el patch no incluye shippingAddress, conservar el original
                if (patched.getShippingAddress() == null) {
                    patched.setShippingAddress(order.getShippingAddress());
                } else if (patched.getShippingAddress().getId() != null) {
                    ShippingAddress shippingAddress = shippingAddressRepository.getById(patched.getShippingAddress().getId());
                    patched.setShippingAddress(shippingAddress);
                }
                // Si el patch no incluye payment, conservar el original
                if (patched.getPayment() == null) {
                    patched.setPayment(order.getPayment());
                } else if (patched.getPayment().getId() != null) {
                    Payment payment = paymentRepository.getById(patched.getPayment().getId());
                    patched.setPayment(payment);
                }
                if (patched.getOrderProducts() != null) {
                    List<OrderProduct> updatedOrderProducts = new ArrayList<>();
                    for (OrderProduct opDTO : patched.getOrderProducts()) {
                        if (opDTO.getProduct() == null || opDTO.getProduct().getId() == null) {
                            throw new IllegalArgumentException("Cada orderProduct debe incluir product.id");
                        }
                        Product product = productRepository.getById(opDTO.getProduct().getId());
                        if (product == null) {
                            throw new IllegalArgumentException("Producto no encontrado: " + opDTO.getProduct().getId());
                        }
                        // Buscar si el producto ya existe en la orden original
                        OrderProduct existing = null;
                        if (order.getOrderProducts() != null) {
                            for (OrderProduct op : order.getOrderProducts()) {
                                if (op.getProduct() != null && op.getProduct().getId().equals(product.getId())) {
                                    existing = op;
                                    break;
                                }
                            }
                        }
                        if (existing != null) {
                            // Actualizar cantidad y valores
                            existing.setQuantity(opDTO.getQuantity());
                            existing.setUnitPrice(product.getPrice());
                            existing.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(opDTO.getQuantity())));
                            existing.setUpdatedAt(LocalDateTime.now());
                            updatedOrderProducts.add(existing);
                        } else {
                            // Añadir nuevo OrderProduct
                            OrderProduct newOp = new OrderProduct();
                            newOp.setProduct(product);
                            newOp.setOrder(patched);
                            newOp.setQuantity(opDTO.getQuantity());
                            newOp.setUnitPrice(product.getPrice());
                            newOp.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(opDTO.getQuantity())));
                            newOp.setCreatedAt(LocalDateTime.now());
                            updatedOrderProducts.add(newOp);
                        }
                    }
                    patched.setOrderProducts(updatedOrderProducts);
                }
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
            if (request.getUser() != null && request.getUser().getId() != null) {
                User user = userRespository.getById(request.getUser().getId());
                order.setUser(user);
            }
            if (request.getShippingAddress() != null && request.getShippingAddress().getId() != null) {
                ShippingAddress shippingAddress = shippingAddressRepository.getById(request.getShippingAddress().getId());
                order.setShippingAddress(shippingAddress);
            }
            if (request.getPayment() != null && request.getPayment().getId() != null) {
                Payment payment = paymentRepository.getById(request.getPayment().getId());
                order.setPayment(payment);
            }
            order.update(request);
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
