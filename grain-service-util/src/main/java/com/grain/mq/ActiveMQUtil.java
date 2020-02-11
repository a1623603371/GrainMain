package com.grain.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;


import javax.jms.ConnectionFactory;

/**
 * @author Administrator
 */

public class ActiveMQUtil {
    private PooledConnectionFactory pooledConnectionFactory;

    public ConnectionFactory init(String brokerURL) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
        //加入连接池
        pooledConnectionFactory = new PooledConnectionFactory(factory);
        //出现异常时重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        pooledConnectionFactory.setMaxConnections(5);
        pooledConnectionFactory.setExpiryTimeout(10000);
        return pooledConnectionFactory;

    }

    public ConnectionFactory getConnectionFactory() {
        return pooledConnectionFactory;
    }
}
