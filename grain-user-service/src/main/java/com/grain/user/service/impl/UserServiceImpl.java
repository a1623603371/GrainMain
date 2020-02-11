package com.grain.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.grain.api.bean.user.UmsMember;
import com.grain.api.bean.user.UmsMemberReceiveAddress;
import com.grain.api.service.user.UserService;
import com.grain.user.mapper.UmsMemberReceiveAddressesMapper;
import com.grain.user.mapper.UserMapper;
import com.grain.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UmsMemberReceiveAddressesMapper umsMemberReceiveAddressesMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:" + memberId + ":info", 60 * 60 * 24, token);
        jedis.close();
    }

    /**
     * 登录
     *
     * @param umsMember
     * @return
     */
    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + ":info");
                if (!StringUtils.isEmpty(umsMemberStr)) {
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                }
            }
            //如果redis宕机或链接不上重数据库中获取
            UmsMember umsMemberDB = userMapper.selectOne(umsMember);
            if (!ObjectUtils.isEmpty(umsMemberDB)) {
                jedis.setex("user:" + umsMemberDB.getPassword() + ":info", 60 * 60 * 24, JSON.toJSONString(umsMemberDB));
            }
            return umsMemberDB;
        } finally {
            jedis.close();
        }
    }

    @Override
    public UmsMember checkOauthUser(UmsMember umsCheck) {
        UmsMember umsMember = userMapper.selectOne(umsCheck);
        return umsMember;
    }

    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        //封装参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressesMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }
}

