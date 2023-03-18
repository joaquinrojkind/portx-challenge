package com.portx.payment.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Account {

    private Long id;
    private String accountType;
    private String accountNumber; // account number could include alphanumeric chars and/or a slash, hence it's a string
}
