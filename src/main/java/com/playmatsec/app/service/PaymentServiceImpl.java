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
            // payment.update(request); // Implementar si existe método update
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
                paymentRepository.delete(payment);
                return true;
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment ID format: {}", id, e);
        }
        return false;
    }
}
