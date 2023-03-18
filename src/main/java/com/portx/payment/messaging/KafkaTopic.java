package com.portx.payment.messaging;

public enum KafkaTopic {

    TRANSACTION_CREATED("transaction-created");

    private String name;

    KafkaTopic(String name) {
        this.name = name;
    }

    public String getValue() {
        return name;
    }
}
