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

    public List<Payment> search(String orderId, String providerPaymentId) {
        PaymentSearchCriteria spec = new PaymentSearchCriteria();
        if (StringUtils.isNotBlank(orderId)) {
            spec.add(new SearchStatement(PaymentConsts.ORDER, orderId, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(providerPaymentId)) {
            spec.add(new SearchStatement(PaymentConsts.PROVIDER_PAYMENT_ID, providerPaymentId, SearchOperation.MATCH));
        }
        return repository.findAll(spec);
    }
}
