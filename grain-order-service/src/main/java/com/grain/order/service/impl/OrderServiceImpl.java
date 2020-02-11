package com.grain.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.grain.api.bean.order.OmsOrder;
import com.grain.api.bean.order.OmsOrderItem;
import com.grain.api.service.cart.CartService;
import com.grain.api.service.order.OrderService;
import com.grain.mq.ActiveMQUtil;
import com.grain.order.service.mapper.OmsOrderItemMapper;
import com.grain.order.service.mapper.OmsOrderMapper;
import com.grain.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    CartService cartService;

    @Override
    public String ckeckTrandeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user" + memberId + "tradeCode";
            //使用lua脚本在发现可以的时候的同事删除key
            String tradeCodeFromCache = jedis.get(tradeKey);
            //对比防重删令牌
            String script = "if redis.call('get',KEYS[1]==argv[1] then return redis.call('del',KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                return "success";
            } else {
                return "fail";
            }
        } finally {
            jedis.close();
        }

    }


    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        //保存订单详情
        List<OmsOrderItem> omsOrderItemList = omsOrder.getOmsOrderItems();
        omsOrderItemList.forEach(omsOrderItem -> {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据
            cartService.delCart(omsOrderItem);
        });
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + "tradeCode";
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeKey, 60 * 15, tradeCode);
            return tradeCode;
        } finally {
            jedis.close();
        }

    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);

        return omsOrderMapper.selectOne(omsOrder);
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", omsOrder.getOrderSn());
        OmsOrder omsOrderUpdate = new OmsOrder();
        //发送一个的订单已支付队列，提供给库存消费
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payhment_success_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);
            //TextMessage textMessage=new ActiveMQTextMessage();//字符串结构
            MapMessage mapMessage = new ActiveMQMapMessage();//hash结构
            omsOrderMapper.updateByExampleSelective(omsOrderUpdate, example);
            producer.send(mapMessage);
            session.commit();
        } catch (Exception e) {
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }

        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
