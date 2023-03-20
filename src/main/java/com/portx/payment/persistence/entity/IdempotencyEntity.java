package com.portx.payment.persistence.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idempotency")
public class IdempotencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "httpStatus", nullable = false, length = 10)
    private Integer httpStatus;

    @Column(name = "jsonBody", columnDefinition = "TEXT")
    private String jsonBody;
}
