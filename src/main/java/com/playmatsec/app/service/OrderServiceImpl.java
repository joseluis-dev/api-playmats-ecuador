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
import com.playmatsec.app.repository.utils.Consts.PaymentMethod;
import com.playmatsec.app.repository.utils.Consts.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final CloudinaryService cloudinaryService;

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
        if (orderId == null) {
            return List.of();
        }
        List<OrderProduct> orderProducts = orderProductRepository.search(orderId, null, null, null, null, null);
        if (orderProducts == null || orderProducts.isEmpty()) {
            return List.of();
        }
        for (OrderProduct op : orderProducts) {
            try {
                if (op != null && op.getProduct() != null && op.getProduct().getId() != null) {
                    Product fullProduct = productRepository.getById(op.getProduct().getId());
                    if (fullProduct != null) {
                        op.setProduct(fullProduct);
                    }
                }
            } catch (Exception e) {
                log.warn("No se pudo cargar el detalle del producto para OrderProduct en orden {}", orderId, e);
            }
        }
        return orderProducts;
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
    @Transactional
    public Order createOrder(OrderDTO request) {
        if (request != null && request.getUser() != null) {
            // Validar que exista al menos un pago antes de crear la orden (regla de negocio)
            if (request.getPayments() == null || request.getPayments().isEmpty()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Operación no permitida: la orden debe tener al menos un pago");
            }
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
            // Calcular totalAmount a partir de los OrderProducts (fuente de verdad) si existen
            if (order.getOrderProducts() != null && !order.getOrderProducts().isEmpty()) {
                BigDecimal total = order.getOrderProducts().stream()
                        .map(OrderProduct::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalAmount(total);
            }

            // Persistimos la orden primero (sin pagos) para garantizar que exista el registro antes de referenciarlo desde Payment
            order.setCreatedAt(LocalDateTime.now());
            order = orderRepository.save(order);

            // Manejar pagos tras haber persistido la orden
            List<Payment> persistedPayments = new ArrayList<>();
        for (Payment paymentReq : request.getPayments()) {
                Payment paymentEntity;
                if (paymentReq.getId() != null) {
                    paymentEntity = paymentRepository.getById(paymentReq.getId());
                    if (paymentEntity == null) {
                        throw new IllegalArgumentException("Payment no encontrado: " + paymentReq.getId());
                    }
                    paymentEntity.setOrder(order);
                } else {
                    paymentEntity = new Payment();
                    paymentEntity.setId(UUID.randomUUID());
                    paymentEntity.setOrder(order);
                    paymentEntity.setAmount(paymentReq.getAmount());
                    paymentEntity.setProviderPaymentId(paymentReq.getProviderPaymentId());
                    paymentEntity.setMethod(paymentReq.getMethod());
                    paymentEntity.setStatus(paymentReq.getStatus() != null ? paymentReq.getStatus() : PaymentStatus.PENDING);
            // La imageUrl se definirá tras subir el archivo si se provee
                    paymentEntity.setPaidAt(paymentReq.getPaidAt());
                    paymentEntity.setCreatedAt(LocalDateTime.now());
                }
                if (paymentEntity.getMethod() != PaymentMethod.CASH) {
                    if (paymentEntity.getAmount() == null || paymentEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("El monto del pago debe ser > 0 para métodos no CASH");
                    }
                }
                paymentEntity = paymentRepository.save(paymentEntity);
                persistedPayments.add(paymentEntity);
            }
            order.setPayments(persistedPayments);

            boolean hasCash = persistedPayments.stream().anyMatch(p -> p.getMethod() == PaymentMethod.CASH);
            if (!hasCash && order.getTotalAmount() != null) {
                BigDecimal sumaPagos = persistedPayments.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (sumaPagos.compareTo(order.getTotalAmount()) != 0) {
                    throw new IllegalArgumentException("La suma de los pagos (" + sumaPagos + ") difiere del total de la orden (" + order.getTotalAmount() + ")");
                }
            }
            // Devolver orden con pagos; no es necesario re-guardar porque Payment es el dueño de la relación
            return order;
        }
        return null;
    }

    // Sobrecarga para aceptar archivo de comprobante de pago (solo 1 imagen por ahora)
    @Transactional
    public Order createOrder(OrderDTO request, MultipartFile paymentImage) {
        // Validar existencia de la imagen ANTES de crear la orden para que no se persista sin comprobante
        if (paymentImage == null || paymentImage.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "La imagen del comprobante de pago (paymentImage) es obligatoria"
            );
        }

        Order order = createOrder(request); // crea y persiste orden + pagos (sin imageUrl)
        if (order == null) return null; // fallback seguro

        try {
            String folder = "payments/" + order.getId();
            var uploadResult = cloudinaryService.uploadImage(paymentImage, folder);
            if (uploadResult == null || uploadResult.get("url") == null) {
                throw new IllegalStateException("No se pudo obtener URL de la imagen subida a Cloudinary");
            }
            // Asignar la URL al primer pago sin imageUrl
            for (Payment p : order.getPayments()) {
                if (p.getImageUrl() == null) {
                    p.setImageUrl(uploadResult.get("url"));
                    paymentRepository.save(p);
                    break;
                }
            }
        } catch (RuntimeException ex) {
            // Re-lanzar para provocar rollback de la transacción (orden y pagos)
            log.error("Fallo al subir la imagen del pago. Se revierte la creación de la orden {}", order.getId(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error inesperado al subir la imagen del pago. Se revierte la creación de la orden {}", order.getId(), ex);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Error subiendo la imagen del pago"
            );
        }
        return order;
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
                if (patched.getPayments() == null) {
                    patched.setPayments(order.getPayments());
                } else if (patched.getPayments() != null) {
                    List<Payment> updatedPayments = new ArrayList<>();
                    for (Payment pDTO : patched.getPayments()) {
                        if (pDTO.getId() == null) {
                            throw new IllegalArgumentException("Cada payment debe incluir id");
                        }
                        Payment payment = paymentRepository.getById(pDTO.getId());
                        if (payment == null) {
                            throw new IllegalArgumentException("Payment no encontrado: " + pDTO.getId());
                        }
                        updatedPayments.add(payment);
                    }
                    patched.setPayments(updatedPayments);
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
                // Recalcular el totalAmount a partir de los subtotales de los OrderProducts
                if (patched.getOrderProducts() != null && !patched.getOrderProducts().isEmpty()) {
                    BigDecimal total = patched.getOrderProducts().stream()
                            .map(op -> {
                                BigDecimal sub = op.getSubtotal();
                                if (sub == null) {
                                    // Fallback en caso de que no venga calculado el subtotal
                                    BigDecimal unit = op.getUnitPrice() != null ? op.getUnitPrice() : BigDecimal.ZERO;
                                    return unit.multiply(BigDecimal.valueOf(op.getQuantity()));
                                }
                                return sub;
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    patched.setTotalAmount(total);
                } else {
                    // Si no hay productos en la orden, el total debe ser 0
                    patched.setTotalAmount(BigDecimal.ZERO);
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
    @Transactional
    public Order updateOrder(String id, OrderDTO request) {
        Order order = getOrderById(id);
        if (order != null) {
            // Actualizar campos generales mediante el método update existente (estatus, direcciones simples, etc.)
            order.update(request);

            // Resolver referencias de user y shippingAddress a entidades administradas
            if (request.getUser() != null && request.getUser().getId() != null) {
                User user = userRespository.getById(request.getUser().getId());
                order.setUser(user);
            }
            if (request.getShippingAddress() != null && request.getShippingAddress().getId() != null) {
                ShippingAddress shippingAddress = shippingAddressRepository.getById(request.getShippingAddress().getId());
                order.setShippingAddress(shippingAddress);
            }

            // Actualización de OrderProducts (precio y subtotal desde Product como fuente de verdad)
            if (request.getOrderProducts() != null) {
                List<OrderProduct> updatedOrderProducts = new ArrayList<>();
                for (OrderProduct opDTO : request.getOrderProducts()) {
                    if (opDTO.getProduct() == null || opDTO.getProduct().getId() == null) {
                        throw new IllegalArgumentException("Cada orderProduct debe incluir product.id");
                    }
                    Product product = productRepository.getById(opDTO.getProduct().getId());
                    if (product == null) {
                        throw new IllegalArgumentException("Producto no encontrado: " + opDTO.getProduct().getId());
                    }
                    // Intentar reutilizar el OrderProduct existente para ese producto
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
                        existing.setQuantity(opDTO.getQuantity());
                        existing.setUnitPrice(product.getPrice());
                        existing.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(opDTO.getQuantity())));
                        existing.setUpdatedAt(LocalDateTime.now());
                        updatedOrderProducts.add(existing);
                    } else {
                        OrderProduct newOp = new OrderProduct();
                        newOp.setProduct(product);
                        newOp.setOrder(order);
                        newOp.setQuantity(opDTO.getQuantity());
                        newOp.setUnitPrice(product.getPrice());
                        newOp.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(opDTO.getQuantity())));
                        newOp.setCreatedAt(LocalDateTime.now());
                        updatedOrderProducts.add(newOp);
                    }
                }
                order.setOrderProducts(updatedOrderProducts);

                // Recalcular totalAmount desde los subtotales de OrderProducts
                BigDecimal total = updatedOrderProducts.stream()
                        .map(op -> {
                            BigDecimal sub = op.getSubtotal();
                            if (sub == null) {
                                BigDecimal unit = op.getUnitPrice() != null ? op.getUnitPrice() : BigDecimal.ZERO;
                                return unit.multiply(BigDecimal.valueOf(op.getQuantity()));
                            }
                            return sub;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalAmount(total);
            }

            // Actualizar/crear pagos según reglas de negocio
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                List<Payment> persistedPayments = new ArrayList<>();
                for (Payment pDTO : request.getPayments()) {
                    Payment paymentEntity;
                    if (pDTO.getId() != null) {
                        paymentEntity = paymentRepository.getById(pDTO.getId());
                        if (paymentEntity == null) {
                            throw new IllegalArgumentException("Payment no encontrado: " + pDTO.getId());
                        }
                        // Actualizar campos permitidos
                        if (pDTO.getAmount() != null) paymentEntity.setAmount(pDTO.getAmount());
                        if (pDTO.getProviderPaymentId() != null) paymentEntity.setProviderPaymentId(pDTO.getProviderPaymentId());
                        if (pDTO.getMethod() != null) paymentEntity.setMethod(pDTO.getMethod());
                        if (pDTO.getStatus() != null) paymentEntity.setStatus(pDTO.getStatus());
                        if (pDTO.getPaidAt() != null) paymentEntity.setPaidAt(pDTO.getPaidAt());
                    } else {
                        paymentEntity = new Payment();
                        paymentEntity.setId(UUID.randomUUID());
                        paymentEntity.setAmount(pDTO.getAmount());
                        paymentEntity.setProviderPaymentId(pDTO.getProviderPaymentId());
                        paymentEntity.setMethod(pDTO.getMethod());
                        paymentEntity.setStatus(pDTO.getStatus() != null ? pDTO.getStatus() : PaymentStatus.PENDING);
                        paymentEntity.setPaidAt(pDTO.getPaidAt());
                        paymentEntity.setCreatedAt(LocalDateTime.now());
                    }
                    paymentEntity.setOrder(order);

                    if (paymentEntity.getMethod() != PaymentMethod.CASH) {
                        if (paymentEntity.getAmount() == null || paymentEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            throw new IllegalArgumentException("El monto del pago debe ser > 0 para métodos no CASH");
                        }
                    }

                    paymentEntity = paymentRepository.save(paymentEntity);
                    persistedPayments.add(paymentEntity);
                }

                order.setPayments(persistedPayments);

                // Validación de suma de pagos vs total cuando no hay CASH
                boolean hasCash = persistedPayments.stream().anyMatch(p -> p.getMethod() == PaymentMethod.CASH);
                if (!hasCash && order.getTotalAmount() != null) {
                    BigDecimal sumaPagos = persistedPayments.stream()
                            .map(Payment::getAmount)
                            .filter(a -> a != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (sumaPagos.compareTo(order.getTotalAmount()) != 0) {
                        throw new IllegalArgumentException("La suma de los pagos (" + sumaPagos + ") difiere del total de la orden (" + order.getTotalAmount() + ")");
                    }
                }
            }

            order.setUpdatedAt(LocalDateTime.now());
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
                if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                    log.warn("No se puede eliminar la orden {} porque tiene pagos asociados ({})", id, order.getPayments().size());
                    return false;
                }
                orderRepository.delete(order);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID format: {}", id, e);
        }
        return false;
    }
}
