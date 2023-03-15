package com.portx.payment.service;

import com.portx.payment.service.model.Payment;
import com.portx.payment.service.model.Status;

public interface PaymentService {

    void acceptPayment(Payment payment);

    Status checkPaymentStatus(Long paymentId);
}
