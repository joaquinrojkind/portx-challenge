package com.portx.payment.api;

import com.google.gson.Gson;
import com.portx.payment.api.dto.AcceptPaymentResponseDto;
import com.portx.payment.api.dto.PaymentDto;
import com.portx.payment.api.dto.PaymentStatusResponseDto;
import com.portx.payment.service.IdempotencyService;
import com.portx.payment.service.PaymentService;
import com.portx.payment.service.model.Account;
import com.portx.payment.service.model.Idempotency;
import com.portx.payment.service.model.Payment;
import com.portx.payment.service.model.User;
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
    public ResponseEntity<AcceptPaymentResponseDto> acceptPayment(@RequestBody PaymentDto paymentDto, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Gson gson = new Gson();
        boolean requestHasIdempotencyKey = StringUtils.isNotBlank(idempotencyKey);
        if (requestHasIdempotencyKey) {
            Idempotency idempotency = idempotencyService.getIdempotency(idempotencyKey);
            if (Optional.ofNullable(idempotency).isPresent()) {
                    AcceptPaymentResponseDto response = StringUtils.isNotBlank(idempotency.getJsonBody()) ?
                            gson.fromJson(idempotency.getJsonBody(), AcceptPaymentResponseDto.class) :
                            null;
                    return ResponseEntity.status(idempotency.getHttpStatus()).body(response);
            }
        }
        Long paymentId;
        try {
            paymentId = paymentService.acceptPayment(toPayment(paymentDto));
        } catch (RuntimeException e) {
            if (requestHasIdempotencyKey) {
                idempotencyService.storeIdempotency(
                        buildIdempotency(idempotencyKey, HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
            }
            throw e;
        }
        AcceptPaymentResponseDto response = AcceptPaymentResponseDto.builder()
                .id(paymentId)
                .location(String.format("payments_api/payments/%s/status", paymentId))
                .build();
        if (requestHasIdempotencyKey) {
            idempotencyService.storeIdempotency(
                    buildIdempotency(idempotencyKey, HttpStatus.ACCEPTED.value(), gson.toJson(response)));
        }
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Payment id is currently the database id. In order not to expose the db id we could add an alphanumeric
     * uuid field to the relevant entities. For CRUD operations which return the entity such as GET we could
     * return the uuid of the existing entity facing the API.
     */
    @GetMapping("/payments/{id}/status")
    public ResponseEntity<PaymentStatusResponseDto> checkPaymentStatus(@PathVariable("id") Long paymentId) {
        return ResponseEntity.ok(
                PaymentStatusResponseDto.builder()
                        .status(paymentService.checkPaymentStatus(paymentId))
                        .build());
    }

    private Idempotency buildIdempotency(String key, Integer httpStatus, String jsonBody) {
        return Idempotency.builder()
                .key(key)
                .httpStatus(httpStatus)
                .jsonBody(jsonBody)
                .build();
    }
    private Payment toPayment(PaymentDto paymentDto) {
        return Payment.builder()
                .currency(paymentDto.getCurrency())
                .amount(paymentDto.getAmount())
                .originator(User.builder()
                        .id(paymentDto.getOriginator().getId())
                        .name(paymentDto.getOriginator().getName())
                        .build())
                .beneficiary(User.builder()
                        .id(paymentDto.getBeneficiary().getId())
                        .name(paymentDto.getBeneficiary().getName())
                        .build())
                .sender(Account.builder()
                        .id(paymentDto.getSender().getId())
                        .accountType(paymentDto.getSender().getAccountType())
                        .accountNumber(paymentDto.getSender().getAccountNumber())
                        .build())
                .receiver(Account.builder()
                        .id(paymentDto.getReceiver().getId())
                        .accountType(paymentDto.getReceiver().getAccountType())
                        .accountNumber(paymentDto.getReceiver().getAccountNumber())
                        .build())
                .build();
    }
}
