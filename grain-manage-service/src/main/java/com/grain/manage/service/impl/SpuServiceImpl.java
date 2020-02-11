package com.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.grain.api.bean.manage.*;
import com.grain.api.service.manage.SpuService;
import com.grain.manage.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrs;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        //添加spu
        pmsProductInfoMapper.insert(pmsProductInfo);
        //获取商品id
        String productId = pmsProductInfo.getId();
        //保存图片信息
        List<PmsProductImage> pmsProductImages = pmsProductInfo.getSpuImageList();
        pmsProductImages.forEach(pmsProductImage -> {
            pmsProductImage.setProductId(productId);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        });
        //保存销售信息
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductInfo.getSpuSaleAttrList();
        pmsProductSaleAttrs.forEach(pmsProductSaleAttr -> {
            pmsProductSaleAttr.setProductId(productId);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            //保存销售属性值
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttr.getSpuSaleAttrValueList();
            pmsProductSaleAttrValues.forEach(pmsProductSaleAttrValue -> {
                        pmsProductSaleAttrValue.setProductId(productId);
                        pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
                    }
            );
        });

        return "succss";
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        pmsProductSaleAttrs.forEach(pmsProductSaleAttr1 -> {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(pmsProductSaleAttr1.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            pmsProductSaleAttr1.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        });
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckBySku(productId, skuId);
        return pmsProductSaleAttrs;
    }

}
