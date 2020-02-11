package com.grain.api.service.pay;

import com.grain.api.bean.pay.PaymentInfo;

/**
 * @author Administrator
 */
public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);
}
