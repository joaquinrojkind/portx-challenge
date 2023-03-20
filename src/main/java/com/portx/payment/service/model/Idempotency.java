package com.portx.payment.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Idempotency {

    private String key;
    private Integer httpStatus;
    private String jsonBody;
}
