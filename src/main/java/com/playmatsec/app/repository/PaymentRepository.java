package com.playmatsec.app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.Payment;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.PaymentSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.PaymentConsts;
import io.micrometer.common.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {
    private final PaymentJpaRepository repository;

    public List<Payment> getPayments() {
        return repository.findAll();
    }

    public Payment getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    public void delete(Payment payment) {
        repository.delete(payment);
    }

    public List<Payment> search(String order, String amount, String providerPaymentId, String method, String status, String imageUrl, String paidAt, String createdAt) {
        PaymentSearchCriteria spec = new PaymentSearchCriteria();
        if (StringUtils.isNotBlank(order)) {
            spec.add(new SearchStatement(PaymentConsts.ORDER, order, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(amount)) {
            spec.add(new SearchStatement(PaymentConsts.AMOUNT, amount, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(providerPaymentId)) {
            spec.add(new SearchStatement(PaymentConsts.PROVIDER_PAYMENT_ID, providerPaymentId, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(method)) {
            spec.add(new SearchStatement(PaymentConsts.METHOD, method, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(status)) {
            spec.add(new SearchStatement(PaymentConsts.STATUS, status, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(imageUrl)) {
            spec.add(new SearchStatement(PaymentConsts.IMAGE_URL, imageUrl, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(paidAt)) {
            spec.add(new SearchStatement(PaymentConsts.PAID_AT, paidAt, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(createdAt)) {
            spec.add(new SearchStatement(PaymentConsts.CREATED_AT, createdAt, SearchOperation.MATCH));
        }
        return repository.findAll(spec);
    }
}
