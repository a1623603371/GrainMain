package com.grain.api.service.cart;

import com.grain.api.bean.cart.OmsCartItem;
import com.grain.api.bean.order.OmsOrderItem;

import java.util.List;

public interface CartService {
    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void updateCart(OmsCartItem omsCartItem);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void delCart(OmsOrderItem omsOrderItem);
}
