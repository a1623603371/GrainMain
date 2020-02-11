package com.grain.test;

import com.grain.utils.RedisUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

@SpringBootTest
@RestController
public class RedisSon {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("testRedisson")
    @ResponseBody
    public String testRedisson() {
        Jedis jedis = redisUtil.getJedis();
        RLock lock = redissonClient.getLock("lock");// 声明锁
        lock.lock();//上锁
        try {
            String v = jedis.get("k");
            if (StringUtils.isEmpty(v)) {
                v = "1";
            }
            System.out.println("->" + v);
            jedis.set("k", (Integer.parseInt(v) + 1) + "");
        } finally {
            jedis.close();
            lock.unlock();// 解锁
        }
        return "success";
    }
}
