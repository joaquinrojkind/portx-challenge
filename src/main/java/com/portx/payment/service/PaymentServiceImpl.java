package com.portx.payment.service;

import com.google.gson.Gson;
import com.portx.payment.messaging.KafkaService;
import com.portx.payment.messaging.KafkaTopic;
import com.portx.payment.persistence.entity.AccountEntity;
import com.portx.payment.persistence.entity.PaymentEntity;
import com.portx.payment.persistence.entity.Status;
import com.portx.payment.persistence.entity.UserEntity;
import com.portx.payment.persistence.repository.AccountRepository;
import com.portx.payment.persistence.repository.PaymentRepository;
import com.portx.payment.persistence.repository.UserRepository;
import com.portx.payment.service.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Objects;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private KafkaService kafkaService;

    /**
     * The transactional annotation ensures that nothing will be committed to the database until
     * the method returns successfully. Otherwise, it will rollback the transaction. This should
     * take care of some of the possible failure scenarios although not all of them. More details
     * on this can be found in the README file of the project.
     */
    @Override
    @Transactional
    public Long acceptPayment(Payment payment) {
        PaymentEntity savedPayment = paymentRepository.save(toPaymentEntity(payment));
        String message = new Gson().toJson(savedPayment);
        kafkaService.publishEvent(KafkaTopic.TRANSACTION_CREATED.getValue(), message);
        return savedPayment.getId();
    }

    @Override
    public com.portx.payment.service.model.Status checkPaymentStatus(Long paymentId) {
        return com.portx.payment.service.model.Status.valueOf(
                paymentRepository.findById(paymentId).orElseThrow(EntityNotFoundException::new)
                        .getStatus().name());
    }

    /**
     * This method will check for ids in the composed entities within a payment structure (Account, User),
     * meaning that if the client sent ids of existing entities, rather than creating a new entity
     * the method will leverage the existing one and attach it to the payment that will be created.
     */
    private PaymentEntity toPaymentEntity(Payment payment) {

        UserEntity originator;
        UserEntity beneficiary;
        AccountEntity sender;
        AccountEntity receiver;

        if (Objects.nonNull(payment.getOriginator().getId())) {
            originator = userRepository.findById(payment.getOriginator().getId()).orElseThrow(EntityNotFoundException::new);
        } else {
            originator = UserEntity.builder()
                    .name(payment.getOriginator().getName())
                    .build();
        }
        if (Objects.nonNull(payment.getBeneficiary().getId())) {
            beneficiary = userRepository.findById(payment.getBeneficiary().getId()).orElseThrow(EntityNotFoundException::new);
        } else {
            beneficiary = UserEntity.builder()
                    .name(payment.getBeneficiary().getName())
                    .build();
        }
        if (Objects.nonNull(payment.getSender().getId())) {
            sender = accountRepository.findById(payment.getSender().getId()).orElseThrow(EntityNotFoundException::new);
        } else {
            sender = AccountEntity.builder()
                    .accountType(payment.getSender().getAccountType())
                    .accountNumber(payment.getSender().getAccountNumber())
                    .build();
        }
        if (Objects.nonNull(payment.getReceiver().getId())) {
            receiver = accountRepository.findById(payment.getReceiver().getId()).orElseThrow(EntityNotFoundException::new);
        } else {
            receiver = AccountEntity.builder()
                    .accountType(payment.getReceiver().getAccountType())
                    .accountNumber(payment.getReceiver().getAccountNumber())
                    .build();
        }
        return PaymentEntity.builder()
                .currency(payment.getCurrency())
                .amount(payment.getAmount())
                .originator(originator)
                .beneficiary(beneficiary)
                .sender(sender)
                .receiver(receiver)
                .status(Status.CREATED)
                .build();
    }
}

