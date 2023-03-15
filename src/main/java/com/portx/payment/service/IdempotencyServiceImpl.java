package com.portx.payment.service;

import com.portx.payment.persistence.entity.IdempotencyEntity;
import com.portx.payment.persistence.repository.IdempotencyRepository;
import com.portx.payment.service.model.Idempotency;
import org.springframework.beans.factory.annotation.Autowired;

public class IdempotencyServiceImpl implements IdempotencyService {

    @Autowired
    private IdempotencyRepository idempotencyRepository;
    @Override
    public void storeIdempotency(Idempotency idempotency) {
        idempotencyRepository.save(IdempotencyEntity.builder()
                .key(idempotency.getKey())
                .httpStatus(idempotency.getHttpStatus())
                .build());
    }

    @Override
    public Idempotency getIdempotency(String idempotencyKey) {
        return toIdempotency(idempotencyRepository.findByKey(idempotencyKey));
    }

    private Idempotency toIdempotency(IdempotencyEntity idempotencyEntity) {
        return Idempotency.builder()
                .key(idempotencyEntity.getKey())
                .httpStatus(idempotencyEntity.getHttpStatus())
                .build();
    }
}
