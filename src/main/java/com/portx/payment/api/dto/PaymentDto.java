package com.portx.payment.api.dto;

import com.portx.payment.service.model.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentDto {

    private Long id;
    private String currency;
    private Double amount;
    private UserDto originator;
    private UserDto beneficiary;
    private AccountDto sender;
    private AccountDto receiver;
    private Status status;
}
