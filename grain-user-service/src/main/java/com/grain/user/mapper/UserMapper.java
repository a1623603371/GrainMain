package com.grain.user.mapper;


import com.grain.api.bean.user.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {

    public List<UmsMember> selectAllUser();
}
