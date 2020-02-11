package com.grain.user.mapper;

import com.grain.user.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {

    public List<UmsMember> selectAllUser();
}
