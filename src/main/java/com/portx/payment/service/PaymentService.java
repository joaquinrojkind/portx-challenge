package com.portx.payment.service;

import com.portx.payment.service.model.Payment;
import com.portx.payment.service.model.Status;

public interface PaymentService {

    Long acceptPayment(Payment payment);

    Status checkPaymentStatus(Long paymentId);
}
