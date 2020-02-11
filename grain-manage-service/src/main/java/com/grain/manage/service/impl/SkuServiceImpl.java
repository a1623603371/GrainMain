package com.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.grain.api.bean.manage.PmsSkuAttrValue;
import com.grain.api.bean.manage.PmsSkuImage;
import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.bean.manage.PmsSkuSaleAttrValue;
import com.grain.api.service.manage.SkuService;
import com.grain.manage.mapper.PmsSkuAttrValueMapper;
import com.grain.manage.mapper.PmsSkuImageMapper;
import com.grain.manage.mapper.PmsSkuInfoMapper;
import com.grain.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.grain.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SkuServiceImpl implements SkuService {
    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;


    /**
     * 保存sku信息
     *
     * @param pmsSkuInfo
     */
    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        pmsSkuAttrValues.forEach(pmsSkuAttrValue -> {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        });
        List<PmsSkuImage> pmsSkuImages = pmsSkuInfo.getSkuImageList();
        pmsSkuImages.forEach(pmsSkuImage -> {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insert(pmsSkuImage);
        });
        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        pmsSkuSaleAttrValues.forEach(pmsSkuSaleAttrValue -> {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        });

    }

    /**
     * 根据skuID查询sku信息
     *
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuById(String skuId, String ip) {
        log.info("ip为" + ip + Thread.currentThread().getName() + "进入商品详情获取数据");
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //获取redis连接
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String key = "sku" + skuId + "info";
        String skuInfo = jedis.get(key);
        //从缓存中获取数据，如果缓存中么有数据从数据库获取
        if (!StringUtils.isEmpty(skuInfo)) {
            log.info("ip为" + ip + Thread.currentThread().getName() + "从缓存中获取数据");
            pmsSkuInfo = JSON.parseObject(skuInfo, PmsSkuInfo.class);
        } else {
            log.info("ip为" + ip + Thread.currentThread().getName() + "获取锁");
            //从数据库获取数据如果数据不是空将数据设置到缓存中
            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("key" + skuId + "lock", token, "nx", "px", 10);
            if (!StringUtils.isEmpty(OK) && OK.equals("OK")) {
                log.info("ip为" + ip + Thread.currentThread().getName() + "获取到锁");
                pmsSkuInfo.setId(skuId);
                pmsSkuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
                if (!ObjectUtils.isEmpty(pmsSkuInfo)) {
                    //存入缓存中
                    jedis.set(key, JSON.toJSONString(pmsSkuInfo));

                } else {
                    //如果数据等于空
                    //防止缓存穿透，在redis中设置过期时间
                    jedis.setex(key, 60 * 3, JSON.toJSONString(""));
                }
                //设置完毕后将锁删除
                String lockToken = jedis.get("key" + skuId + "lock");
                if (!StringUtils.isEmpty(lockToken) && token.equals(lockToken)) {
                    log.info("ip为" + ip + Thread.currentThread().getName() + "删除" + "key" + skuId + "lock");
                    //使用lua脚本防止查询锁还没删除网络还在传输，锁自动过期
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(token));
                    jedis.del("key" + skuId + "lock");
                }

            } else {
                //设置失败，自旋（线程睡眠几秒，重新访问方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId, ip);
            }
        }
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.getSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        pmsSkuInfos.stream().forEach(pmsSkuInfo -> {
                    String skuId = pmsSkuInfo.getId();
                    PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
                    pmsSkuAttrValue.setSkuId(skuId);
                    List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
                    pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
                }
        );
        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal productPrice) {
        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal prire = pmsSkuInfo1.getPrice();
        if (prire.compareTo(productPrice) == 0) {
            b = true;
        }
        return b;
    }
}
