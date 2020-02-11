package com.grain.user.service.impl;

import com.grain.user.bean.UmsMember;
import com.grain.user.mapper.UserMapper;
import com.grain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }
}

