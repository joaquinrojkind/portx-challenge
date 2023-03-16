package com.portx.payment.api;

import com.portx.payment.api.dto.PaymentDto;
import com.portx.payment.service.IdempotencyService;
import com.portx.payment.service.PaymentService;
import com.portx.payment.service.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/payments_api")
public class PaymentsApi {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private IdempotencyService idempotencyService;

    @PostMapping("/payments")
    public ResponseEntity acceptPayment(@RequestBody PaymentDto paymentDto, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        boolean requestHasIdempotencyKey = StringUtils.isNotBlank(idempotencyKey);
        if (requestHasIdempotencyKey) {
            Idempotency idempotency = idempotencyService.getIdempotency(idempotencyKey);
            if (Optional.ofNullable(idempotency).isPresent()) {
                return ResponseEntity.status(idempotency.getHttpStatus()).build();
            }
        }
        try {
            paymentService.acceptPayment(toPayment(paymentDto));
        } catch (RuntimeException e) {
            if (requestHasIdempotencyKey) {
                idempotencyService.storeIdempotency(
                        buildIdempotency(idempotencyKey, HttpStatus.INTERNAL_SERVER_ERROR.value()));
            }
            throw e;
        }
        if (requestHasIdempotencyKey) {
            idempotencyService.storeIdempotency(
                    buildIdempotency(idempotencyKey, HttpStatus.ACCEPTED.value()));
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Status> checkPaymentStatus(@PathVariable() Long paymentId) {
        return ResponseEntity.ok(paymentService.checkPaymentStatus(paymentId));
    }

    private Idempotency buildIdempotency(String key, Integer httpStatus) {
        return Idempotency.builder()
                .key(key)
                .httpStatus(httpStatus)
                .build();
    }
    private Payment toPayment(PaymentDto paymentDto) {
        return Payment.builder()
                .currency(paymentDto.getCurrency())
                .amount(paymentDto.getAmount())
                .originator(User.builder()
                        .name(paymentDto.getOriginator().getName())
                        .build())
                .beneficiary(User.builder()
                        .name(paymentDto.getBeneficiary().getName())
                        .build())
                .sender(Account.builder()
                        .accountType(paymentDto.getSender().getAccountType())
                        .accountNumber(paymentDto.getSender().getAccountNumber())
                        .build())
                .receiver(Account.builder()
                        .accountType(paymentDto.getReceiver().getAccountType())
                        .accountNumber(paymentDto.getReceiver().getAccountNumber())
                        .build())
                .build();
    }
}
