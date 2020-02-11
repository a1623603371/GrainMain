package com.grain.order.service.consumer;

import com.grain.api.bean.order.OmsOrder;
import com.grain.api.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author Administrator
 */
@Component
public class OrderServiceMqConsumer {
    @Autowired
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) {
        try {
            String out_trader_no = mapMessage.getString("out_trade_no");
            //更新订单状态业务
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setOrderSn(out_trader_no);
            orderService.updateOrder(omsOrder);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
