package com.portx.payment.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {

    private Long id;
    private String name;
}
