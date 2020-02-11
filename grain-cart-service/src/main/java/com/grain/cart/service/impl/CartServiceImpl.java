package com.grain.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.grain.api.bean.cart.OmsCartItem;
import com.grain.api.bean.order.OmsOrderItem;
import com.grain.api.service.cart.CartService;
import com.grain.cart.mapper.OmsCartItemMapper;
import com.grain.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsCartItemMapper omsCartItemMapper;


    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDB) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id", omsCartItemFromDB.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDB, example);
    }

    @Override
    public void flushCartCache(String memberId) {
        Jedis jedis = null;
        try {
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
            //同步到缓存中
            jedis = redisUtil.getJedis();
            Map<String, String> map = new HashMap<>();
            omsCartItems.forEach(omsCartItem1 -> {
                omsCartItem1.setTotalPrice(omsCartItem1.getPrice().multiply(omsCartItem1.getQuantity()));
                map.put(omsCartItem1.getProductSkuId(), JSON.toJSONString(omsCartItem1));
            });
            jedis.del("user:" + memberId + ":cart");
            jedis.hmset("user:" + memberId + ":cart", map);
        } finally {
            jedis.close();
        }


    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + memberId + ":cart");
            hvals.forEach(hval -> {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            });

        } catch (Exception e) {
            //处理异常，加入日志
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productId");
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
        //同步缓存
        flushCartCache(omsCartItem.getMemberId());

    }

    @Override
    public void delCart(OmsOrderItem omsOrderItem) {

    }


}
