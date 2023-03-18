package com.portx.payment.api.dto;

import com.portx.payment.service.model.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentStatusResponseDto {

    private Status status;
}
