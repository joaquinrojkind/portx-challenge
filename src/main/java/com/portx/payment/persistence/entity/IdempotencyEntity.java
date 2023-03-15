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

    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "httpStatus", nullable = false)
    private Integer httpStatus;
}
