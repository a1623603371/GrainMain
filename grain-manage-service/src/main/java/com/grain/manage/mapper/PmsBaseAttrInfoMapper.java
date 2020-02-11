package com.grain.manage.mapper;

import com.grain.api.bean.manage.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    public List<PmsBaseAttrInfo> selectAttrValueListByValueId(@Param("valueIdStr") String valueIdStr);
}
