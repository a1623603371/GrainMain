package com.grain.api.bean.pay;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @param
 * @return
 */
@Data
public class PaymentInfo {

    @Column
    @Id
    private String id;

    @Column
    private String orderSn;

    @Column
    private String orderId;

    @Column
    private String alipayTradeNo;

    @Column
    private BigDecimal totalAmount;

    @Column
    private String Subject;

    @Column
    private String paymentStatus;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime callbackTime;

    @Column
    private String callbackContent;

}
