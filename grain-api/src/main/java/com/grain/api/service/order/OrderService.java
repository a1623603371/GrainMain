package com.grain.api.service.order;

import com.grain.api.bean.order.OmsOrder;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */

public interface OrderService {
    public String ckeckTrandeCode(String memberId, String nickname);


    public void saveOrder(OmsOrder omsOrder);

    String genTradeCode(String memberId);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
