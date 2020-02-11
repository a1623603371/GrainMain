package com.grain.mq;

import com.sun.org.apache.regexp.internal.RE;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJcaListenerContainerFactory;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

import javax.jms.Session;


/**
 * @author Administrator
 */
@Configuration
public class ActiveMQConfig {
    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL;
    @Value("${activemq.listener.enable.disabled}")
    String listenerEnable;

    @Bean
    public ActiveMQUtil getActiveMQUtil() {
        if ("disabled".equals(brokerURL)) {
            return null;
        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.init(brokerURL);
        return activeMQUtil;
    }

    //定义一个消息监听器连接工厂，这里定义的是点对点模式和监听器连接工厂
    @Bean
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        if (!"true".equals(listenerEnable)) {
            return null;
        }
        factory.setConnectionFactory(activeMQConnectionFactory);
        //设置并发数
        factory.setConcurrency("5");
        //重连间隔时间
        factory.setRecoveryInterval(5000L);
        factory.setSessionTransacted(false);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;

    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
        return activeMQConnectionFactory;
    }
}
