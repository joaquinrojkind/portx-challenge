package com.portx.payment.service;

import com.portx.payment.persistence.entity.AccountEntity;
import com.portx.payment.persistence.entity.PaymentEntity;
import com.portx.payment.persistence.entity.StatusEntity;
import com.portx.payment.persistence.entity.UserEntity;
import com.portx.payment.persistence.repository.PaymentRepository;
import com.portx.payment.service.model.Payment;
import com.portx.payment.service.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void acceptPayment(Payment payment) {
        paymentRepository.save(toPaymentEntity(payment));

        // publish event to Kafka
    }

    @Override
    public Status checkPaymentStatus(Long paymentId) {
        return Status.valueOf(paymentRepository.getOne(paymentId).getStatus().name());
    }

    private PaymentEntity toPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .currency(payment.getCurrency())
                .amount(payment.getAmount())
                .originator(UserEntity.builder()
                        .name(payment.getOriginator().getName())
                        .build())
                .beneficiary(UserEntity.builder()
                        .name(payment.getBeneficiary().getName())
                        .build())
                .sender(AccountEntity.builder()
                        .accountType(payment.getSender().getAccountType())
                        .accountNumber(payment.getSender().getAccountNumber())
                        .build())
                .receiver(AccountEntity.builder()
                        .accountType(payment.getReceiver().getAccountType())
                        .accountNumber(payment.getReceiver().getAccountNumber())
                        .build())
                .status(StatusEntity.CREATED)
                .build();
    }
}

