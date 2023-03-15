package com.portx.payment.service;

import com.portx.payment.service.model.Idempotency;

public interface IdempotencyService {

    void storeIdempotency(Idempotency idempotency);

    Idempotency getIdempotency(String idempotencyKey);
}
