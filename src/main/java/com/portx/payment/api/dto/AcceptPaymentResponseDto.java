package com.portx.payment.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AcceptPaymentResponseDto {

    private Long id;
    private String location;
}
