package com.playmatsec.app.service;

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
import com.playmatsec.app.controller.model.PaymentDTO;
import com.playmatsec.app.repository.OrderRepository;
import com.playmatsec.app.repository.PaymentRepository;
import com.playmatsec.app.repository.model.Order;
import com.playmatsec.app.repository.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<Payment> getPayments(String order, String amount, String providerPaymentId, String method, String status, String imageUrl, String paidAt, String createdAt) {
        if (StringUtils.hasLength(order) || StringUtils.hasLength(amount) || StringUtils.hasLength(providerPaymentId) || StringUtils.hasLength(method) || StringUtils.hasLength(status) || StringUtils.hasLength(imageUrl) || StringUtils.hasLength(paidAt) || StringUtils.hasLength(createdAt)) {
            return paymentRepository.search(order, amount, providerPaymentId, method, status, imageUrl, paidAt, createdAt);
        }
        List<Payment> payments = paymentRepository.getPayments();
        return payments.isEmpty() ? null : payments;
    }

    @Override
    public Payment getPaymentById(String id) {
        try {
            UUID paymentId = UUID.fromString(id);
            return paymentRepository.getById(paymentId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public Payment createPayment(PaymentDTO request) {
        if (request != null) {
            Payment payment = objectMapper.convertValue(request, Payment.class);
            if (payment.getOrder() != null && payment.getOrder().getId() != null) {
                Order order = orderRepository.getById(payment.getOrder().getId());
                payment.setOrder(order);
            }
            payment.setId(UUID.randomUUID());
            payment.setCreatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public Payment updatePayment(String id, String request) {
        Payment payment = getPaymentById(id);
        if (payment != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(payment)));
                Payment patched = objectMapper.treeToValue(target, Payment.class);
                if (patched.getOrder() == null || patched.getOrder().getId() == null) {
                    patched.setOrder(payment.getOrder());
                } else {
                    Order order = orderRepository.getById(patched.getOrder().getId());
                    patched.setOrder(order);
                }
                paymentRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating payment {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Payment updatePayment(String id, PaymentDTO request) {
        Payment payment = getPaymentById(id);
        if (payment != null) {
            payment.update(request);
            if (request.getOrder() != null && request.getOrder().getId() != null) {
                Order order = orderRepository.getById(request.getOrder().getId());
                payment.setOrder(order);
            } else {
                payment.setOrder(payment.getOrder());
            }
            paymentRepository.save(payment);
            return payment;
        }
        return null;
    }

    @Override
    public Boolean deletePayment(String id) {
        try {
            UUID paymentId = UUID.fromString(id);
            Payment payment = paymentRepository.getById(paymentId);
            if (payment != null) {
                // Si existe imagen, intentar eliminar primero en Cloudinary
                String imageUrl = payment.getImageUrl();
                if (StringUtils.hasLength(imageUrl)) {
                    String publicId = extractPublicId(imageUrl);
                    if (publicId != null) {
                        boolean deleted = cloudinaryService.deleteImage(publicId);
                        if (!deleted) {
                            log.warn("No se pudo eliminar la imagen en Cloudinary (publicId={}), se aborta borrado de payment {}", publicId, id);
                            return false; // evitar eliminar registro si la imagen no se pudo borrar
                        }
                    } else {
                        log.warn("No se pudo derivar publicId desde imageUrl={}, continuando con borrado de payment", imageUrl);
                    }
                }
                paymentRepository.delete(payment); // eliminar registro tras eliminar imagen (o si no había)
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment ID format: {}", id, e);
        }
        return false;
    }

    private String extractPublicId(String url) {
        if (!StringUtils.hasLength(url)) return null;
        try {
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String rest = url.substring(uploadIdx + 8); // después de /upload/
            // Remover versión (v123456789/)
            if (rest.startsWith("v")) {
                int slash = rest.indexOf('/');
                if (slash != -1) rest = rest.substring(slash + 1);
            }
            int qIdx = rest.indexOf('?');
            if (qIdx != -1) rest = rest.substring(0, qIdx);
            int dotIdx = rest.lastIndexOf('.');
            if (dotIdx != -1) rest = rest.substring(0, dotIdx);
            return rest;
        } catch (Exception ex) {
            log.warn("Error extrayendo publicId de url {}: {}", url, ex.getMessage());
            return null;
        }
    }
}
