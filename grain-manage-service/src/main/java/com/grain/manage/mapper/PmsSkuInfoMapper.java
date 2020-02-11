package com.grain.manage.mapper;

import com.grain.api.bean.manage.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {

    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
}
