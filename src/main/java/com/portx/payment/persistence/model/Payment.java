package com.portx.payment.persistence.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

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
    private User originator;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "beneficiary_user_id", referencedColumnName = "id")
    private User beneficiary;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_account_id", referencedColumnName = "id")
    private Account sender;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiver_account_id", referencedColumnName = "id")
    private Account receiver;

    @Column(name = "status", nullable = false)
    private Status status;
}
