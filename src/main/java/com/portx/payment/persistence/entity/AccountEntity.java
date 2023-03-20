package com.portx.payment.persistence.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_type", nullable = false, length = 30)
    private String accountType;

    @Column(name = "account_number", nullable = false, unique = true, length = 30)
    private String accountNumber;
}
