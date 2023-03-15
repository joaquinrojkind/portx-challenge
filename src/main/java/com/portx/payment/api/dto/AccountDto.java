package com.portx.payment.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountDto {

    private Long id;
    private String accountType;
    private String accountNumber;
}
