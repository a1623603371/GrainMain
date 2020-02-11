package com.grain.api.service.manage;

import com.grain.api.bean.manage.PmsBaseSaleAttr;
import com.grain.api.bean.manage.PmsProductImage;
import com.grain.api.bean.manage.PmsProductInfo;
import com.grain.api.bean.manage.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    public List<PmsProductInfo> spuList(String catalog3Id);

    public List<PmsBaseSaleAttr> baseSaleAttrList();

    public String saveSpuInfo(PmsProductInfo pmsProductInfo);

    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    public List<PmsProductImage> spuImageList(String spuId);

    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId);
}
