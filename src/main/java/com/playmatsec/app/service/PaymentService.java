package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.Payment;
import com.playmatsec.app.controller.model.PaymentDTO;

public interface PaymentService {
    List<Payment> getPayments(String orderId, String providerPaymentId);
    Payment getPaymentById(String id);
    Payment createPayment(PaymentDTO payment);
    Payment updatePayment(String id, String updateRequest);
    Payment updatePayment(String id, PaymentDTO payment);
    Boolean deletePayment(String id);
}
