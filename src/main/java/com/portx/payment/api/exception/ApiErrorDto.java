package com.portx.payment.api.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiErrorDto {

    private Integer status;
    private String code;
    private String message;
}

