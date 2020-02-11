package com.grain.api.service.manage;

import com.grain.api.bean.manage.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    public PmsSkuInfo getSkuById(String skuId, String ip);

    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    public List<PmsSkuInfo> getAllSku();

    boolean checkPrice(String productSkuId, BigDecimal price);
}
