package com.grain.payment.service.impl;

import com.grain.api.bean.pay.PaymentInfo;
import com.grain.api.service.pay.PaymentService;
import com.grain.mq.ActiveMQUtil;
import com.grain.payment.mapper.PaymentInfoMapper;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

/**
 * @author Administrator
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        String orderSn = paymentInfo.getOrderSn();
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn", orderSn);
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
            //支付成功后，引起的系统服务--》订单服务的更新-》库存服务-》物流服务
            //调用mq发送支付成功的消息
            Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);
            //TextMessage textMessage=new ActiveMQTextMessage();//字符串文本
            MapMessage mapMessage = new ActiveMQMapMessage();//hash结构
            mapMessage.setString("out_trade_no", paymentInfo.getOrderSn());
            producer.send(mapMessage);
            session.commit();
        } catch (Exception e) {
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
