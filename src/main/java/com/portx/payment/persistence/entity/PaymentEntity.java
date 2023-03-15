package com.portx.payment.persistence.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "originator_user_id", referencedColumnName = "id")
    private UserEntity originator;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "beneficiary_user_id", referencedColumnName = "id")
    private UserEntity beneficiary;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_account_id", referencedColumnName = "id")
    private AccountEntity sender;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiver_account_id", referencedColumnName = "id")
    private AccountEntity receiver;

    @Column(name = "status", nullable = false)
    private StatusEntity status;
}
