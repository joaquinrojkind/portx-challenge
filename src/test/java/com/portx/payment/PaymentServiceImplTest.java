package com.portx.payment;

import com.google.gson.Gson;
import com.portx.payment.messaging.KafkaService;
import com.portx.payment.messaging.KafkaTopic;
import com.portx.payment.persistence.entity.AccountEntity;
import com.portx.payment.persistence.entity.PaymentEntity;
import com.portx.payment.persistence.entity.Status;
import com.portx.payment.persistence.entity.UserEntity;
import com.portx.payment.persistence.repository.AccountRepository;
import com.portx.payment.persistence.repository.PaymentRepository;
import com.portx.payment.persistence.repository.UserRepository;
import com.portx.payment.service.PaymentServiceImpl;
import com.portx.payment.service.model.Account;
import com.portx.payment.service.model.Payment;
import com.portx.payment.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private KafkaService kafkaService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAcceptPayment_success() {

        // The payment passed as argument to the method
        Payment incomingPayment = Payment.builder()
                .currency("USD")
                .amount(12.34)
                .originator(User.builder()
                        .name("John Smith")
                        .build())
                .beneficiary(User.builder()
                        .name("Alicia Toner")
                        .build())
                .sender(Account.builder()
                        .accountType("checking")
                        .accountNumber("75846657")
                        .build())
                .receiver(Account.builder()
                        .accountType("savings")
                        .accountNumber("89997365")
                        .build())
                .build();

        // The payment entity mapped from the incoming payment which is to be stored in the db by the repository
        PaymentEntity paymentToBeSaved = PaymentEntity.builder()
                .currency(incomingPayment.getCurrency())
                .amount(incomingPayment.getAmount())
                .originator(UserEntity.builder()
                        .name(incomingPayment.getOriginator().getName())
                        .build())
                .beneficiary(UserEntity.builder()
                        .name(incomingPayment.getBeneficiary().getName())
                        .build())
                .sender(AccountEntity.builder()
                        .accountType(incomingPayment.getSender().getAccountType())
                        .accountNumber(incomingPayment.getSender().getAccountNumber())
                        .build())
                .receiver(AccountEntity.builder()
                        .accountType(incomingPayment.getReceiver().getAccountType())
                        .accountNumber(incomingPayment.getReceiver().getAccountNumber())
                        .build())
                .status(Status.CREATED)
                .build();

        // The payment entity returned by the repository after executing the save operation
        PaymentEntity savedPayment = PaymentEntity.builder()
                .id(44L)
                .currency(incomingPayment.getCurrency())
                .amount(incomingPayment.getAmount())
                .originator(UserEntity.builder()
                        .id(1L)
                        .name(incomingPayment.getOriginator().getName())
                        .build())
                .beneficiary(UserEntity.builder()
                        .id(2L)
                        .name(incomingPayment.getBeneficiary().getName())
                        .build())
                .sender(AccountEntity.builder()
                        .id(3L)
                        .accountType(incomingPayment.getSender().getAccountType())
                        .accountNumber(incomingPayment.getSender().getAccountNumber())
                        .build())
                .receiver(AccountEntity.builder()
                        .id(4L)
                        .accountType(incomingPayment.getReceiver().getAccountType())
                        .accountNumber(incomingPayment.getReceiver().getAccountNumber())
                        .build())
                .status(Status.CREATED)
                .build();

        when(paymentRepository.save(any())).thenReturn(savedPayment);

        // invoke the method under test passing the appropriate payment object
        Long id = paymentService.acceptPayment(incomingPayment);
        assertThat(id).isEqualTo(savedPayment.getId());

        // Capture the payment entity that was passed to the repository
        ArgumentCaptor<PaymentEntity> paymentEntityCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository, times(1)).save(paymentEntityCaptor.capture());

        // Assert that the payment entity that was passed to the repository has the expected values and objects
        PaymentEntity toBeSaved = paymentEntityCaptor.getValue();
        assertThat(toBeSaved.getCurrency()).isEqualTo(paymentToBeSaved.getCurrency());
        assertThat(toBeSaved.getAmount()).isEqualTo(paymentToBeSaved.getAmount());
        assertThat(toBeSaved.getOriginator().getName()).isEqualTo(paymentToBeSaved.getOriginator().getName());
        assertThat(toBeSaved.getBeneficiary().getName()).isEqualTo(paymentToBeSaved.getBeneficiary().getName());
        assertThat(toBeSaved.getSender().getAccountType()).isEqualTo(paymentToBeSaved.getSender().getAccountType());
        assertThat(toBeSaved.getSender().getAccountNumber()).isEqualTo(paymentToBeSaved.getSender().getAccountNumber());
        assertThat(toBeSaved.getReceiver().getAccountType()).isEqualTo(paymentToBeSaved.getReceiver().getAccountType());
        assertThat(toBeSaved.getReceiver().getAccountNumber()).isEqualTo(paymentToBeSaved.getReceiver().getAccountNumber());
        assertThat(toBeSaved.getStatus()).isEqualTo(paymentToBeSaved.getStatus());

        // Capture the message that was passed to the kafka service
        ArgumentCaptor<String> kafkaCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaService, times(1)).publishEvent(eq(KafkaTopic.TRANSACTION_CREATED.getValue()),
                kafkaCaptor.capture());

        // Assert that the kafka message is a json string reflecting all the appropriate fields and values
        assertThat(kafkaCaptor.getValue()).isEqualTo(new Gson().toJson(savedPayment));
    }

    @Test
    public void testAcceptPayment_existingUserAndAccount() {

        // The payment passed as argument to the method, the originator and the sender are ids only
        Payment incomingPayment = Payment.builder()
                .currency("USD")
                .amount(12.34)
                .originator(User.builder()
                        .id(22L)
                        .build())
                .beneficiary(User.builder()
                        .name("Alicia Toner")
                        .build())
                .sender(Account.builder()
                        .id(33L)
                        .build())
                .receiver(Account.builder()
                        .accountType("savings")
                        .accountNumber("89997365")
                        .build())
                .build();

        // The existing user (originator) referenced by the incoming payment object
        UserEntity existingUser = UserEntity.builder()
                .id(incomingPayment.getOriginator().getId())
                .name("Name from the db")
                .build();

        when(userRepository.findById(incomingPayment.getOriginator().getId())).thenReturn(Optional.of(existingUser));

        // The existing account (sender) referenced by the incoming payment object
        AccountEntity existingAccount = AccountEntity.builder()
                .id(incomingPayment.getSender().getId())
                .accountType("Account type from the db")
                .accountNumber("Account number from the db")
                .build();

        when(accountRepository.findById(incomingPayment.getSender().getId())).thenReturn(Optional.of(existingAccount));

        // The payment entity mapped from the incoming payment and the existing user and account, which is to be stored in the db by the repository
        PaymentEntity paymentToBeSaved = PaymentEntity.builder()
                .currency(incomingPayment.getCurrency())
                .amount(incomingPayment.getAmount())
                .originator(UserEntity.builder()
                        .id(existingUser.getId())
                        .name(existingUser.getName())
                        .build())
                .beneficiary(UserEntity.builder()
                        .name(incomingPayment.getBeneficiary().getName())
                        .build())
                .sender(AccountEntity.builder()
                        .id(existingAccount.getId())
                        .accountType(existingAccount.getAccountType())
                        .accountNumber(existingAccount.getAccountNumber())
                        .build())
                .receiver(AccountEntity.builder()
                        .accountType(incomingPayment.getReceiver().getAccountType())
                        .accountNumber(incomingPayment.getReceiver().getAccountNumber())
                        .build())
                .status(Status.CREATED)
                .build();

        // The payment entity returned by the repository after executing the save operation
        PaymentEntity savedPayment = PaymentEntity.builder()
                .id(44L)
                .currency(incomingPayment.getCurrency())
                .amount(incomingPayment.getAmount())
                .originator(UserEntity.builder()
                        .id(existingUser.getId())
                        .name(existingUser.getName())
                        .build())
                .beneficiary(UserEntity.builder()
                        .id(2L)
                        .name(incomingPayment.getBeneficiary().getName())
                        .build())
                .sender(AccountEntity.builder()
                        .id(existingAccount.getId())
                        .accountType(existingAccount.getAccountType())
                        .accountNumber(existingAccount.getAccountNumber())
                        .build())
                .receiver(AccountEntity.builder()
                        .id(4L)
                        .accountType(incomingPayment.getReceiver().getAccountType())
                        .accountNumber(incomingPayment.getReceiver().getAccountNumber())
                        .build())
                .status(Status.CREATED)
                .build();

        when(paymentRepository.save(any())).thenReturn(savedPayment);

        // invoke the method under test passing the appropriate payment object
        Long id = paymentService.acceptPayment(incomingPayment);
        assertThat(id).isEqualTo(savedPayment.getId());

        // Capture the payment entity that was passed to the repository
        ArgumentCaptor<PaymentEntity> paymentEntityCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository, times(1)).save(paymentEntityCaptor.capture());

        // Assert that the payment entity that was passed to the repository has the expected values and objects
        PaymentEntity toBeSaved = paymentEntityCaptor.getValue();
        assertThat(toBeSaved.getCurrency()).isEqualTo(paymentToBeSaved.getCurrency());
        assertThat(toBeSaved.getAmount()).isEqualTo(paymentToBeSaved.getAmount());
        assertThat(toBeSaved.getOriginator().getName()).isEqualTo(existingUser.getName());
        assertThat(toBeSaved.getBeneficiary().getName()).isEqualTo(paymentToBeSaved.getBeneficiary().getName());
        assertThat(toBeSaved.getSender().getAccountType()).isEqualTo(existingAccount.getAccountType());
        assertThat(toBeSaved.getSender().getAccountNumber()).isEqualTo(existingAccount.getAccountNumber());
        assertThat(toBeSaved.getReceiver().getAccountType()).isEqualTo(paymentToBeSaved.getReceiver().getAccountType());
        assertThat(toBeSaved.getReceiver().getAccountNumber()).isEqualTo(paymentToBeSaved.getReceiver().getAccountNumber());
        assertThat(toBeSaved.getStatus()).isEqualTo(paymentToBeSaved.getStatus());

        // Capture the message that was passed to the kafka service
        ArgumentCaptor<String> kafkaCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaService, times(1)).publishEvent(eq(KafkaTopic.TRANSACTION_CREATED.getValue()),
                kafkaCaptor.capture());

        // Assert that the kafka message is a json string reflecting all the appropriate fields and values
        assertThat(kafkaCaptor.getValue()).isEqualTo(new Gson().toJson(savedPayment));
    }

    @Test
    public void testAcceptPayment_exception_userNotFound() {

        // The payment passed as argument to the method, the originator has an id only
        Payment incomingPayment = Payment.builder()
                .currency("USD")
                .amount(12.34)
                .originator(User.builder()
                        .id(22L)
                        .build())
                .beneficiary(User.builder()
                        .name("Alicia Toner")
                        .build())
                .sender(Account.builder()
                        .accountType("checking")
                        .accountNumber("75846657")
                        .build())
                .receiver(Account.builder()
                        .accountType("savings")
                        .accountNumber("89997365")
                        .build())
                .build();

        when(userRepository.findById(incomingPayment.getOriginator().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.acceptPayment(incomingPayment))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testAcceptPayment_exception_accountNotFound() {

        // The payment passed as argument to the method, the sender has an id only
        Payment incomingPayment = Payment.builder()
                .currency("USD")
                .amount(12.34)
                .originator(User.builder()
                        .name("John Smith")
                        .build())
                .beneficiary(User.builder()
                        .name("Alicia Toner")
                        .build())
                .sender(Account.builder()
                        .id(22L)
                        .build())
                .receiver(Account.builder()
                        .accountType("savings")
                        .accountNumber("89997365")
                        .build())
                .build();

        when(accountRepository.findById(incomingPayment.getSender().getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.acceptPayment(incomingPayment))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testCheckPaymentStatus_success() {
        PaymentEntity existingPayment = PaymentEntity.builder()
                .status(Status.CREATED)
                .build();
        when(paymentRepository.findById(any())).thenReturn(Optional.ofNullable(existingPayment));
        com.portx.payment.service.model.Status status = paymentService.checkPaymentStatus(1L);
        assertThat(status.name()).isEqualTo(existingPayment.getStatus().name());
    }

    @Test
    public void testCheckPaymentStatus_exception_paymentNotFound() {
        when(paymentRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.checkPaymentStatus(1L)).isInstanceOf(EntityNotFoundException.class);
    }
}
