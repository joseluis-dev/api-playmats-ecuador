package com.playmatsec.app.service;

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
import com.playmatsec.app.repository.PaymentRepository;
import com.playmatsec.app.repository.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Payment> getPayments(String orderId, String providerPaymentId) {
        if (StringUtils.hasLength(orderId) || StringUtils.hasLength(providerPaymentId)) {
            return paymentRepository.search(orderId, providerPaymentId);
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
