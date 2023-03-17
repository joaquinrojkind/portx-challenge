package com.portx.payment.service;

import com.portx.payment.persistence.entity.IdempotencyEntity;
import com.portx.payment.persistence.repository.IdempotencyRepository;
import com.portx.payment.service.model.Idempotency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    @Autowired
    private IdempotencyRepository idempotencyRepository;
    @Override
    public void storeIdempotency(Idempotency idempotency) {
        idempotencyRepository.save(IdempotencyEntity.builder()
                .idempotencyKey(idempotency.getKey())
                .httpStatus(idempotency.getHttpStatus())
                .build());
    }

    @Override
    public Idempotency getIdempotency(String idempotencyKey) {
        IdempotencyEntity idempotencyEntity = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (Optional.ofNullable(idempotencyEntity).isPresent()) {
            return toIdempotency(idempotencyEntity);
        }
        return null;
    }

    private Idempotency toIdempotency(IdempotencyEntity idempotencyEntity) {
        return Idempotency.builder()
                .key(idempotencyEntity.getIdempotencyKey())
                .httpStatus(idempotencyEntity.getHttpStatus())
                .build();
    }
}
