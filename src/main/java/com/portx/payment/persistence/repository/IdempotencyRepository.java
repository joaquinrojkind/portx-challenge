package com.portx.payment.persistence.repository;

import com.portx.payment.persistence.entity.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyEntity, Long> {

    IdempotencyEntity findByIdempotencyKey(String idempotencyKey);
}
