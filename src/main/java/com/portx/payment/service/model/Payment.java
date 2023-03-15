package com.portx.payment.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Payment {

    private Long id;
    private String currency;
    private Double amount;
    private User originator;
    private User beneficiary;
    private Account sender;
    private Account receiver;
    private Status status;
}
