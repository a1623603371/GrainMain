package com.grain.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.grain.api.bean.manage.PmsProductSaleAttr;
import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.bean.manage.PmsSkuSaleAttrValue;
import com.grain.api.service.manage.SkuService;
import com.grain.api.service.manage.SpuService;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.http.HttpRequest;
import org.assertj.core.internal.cglib.asm.$Attribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    @RequestMapping("index")
    public String index(ModelMap modelMap) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据" + i);
        }

        modelMap.put("list", list);
        modelMap.put("hello", "hello thymeleaf !!");

        modelMap.put("check", "0");

        return "index";
    }

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap modelMap, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId, ip);
        //sku对象
        modelMap.put("skuInfo", pmsSkuInfo);
        //销售属性
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), skuId);
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);
        //查询单前sku的spu的其他sku的集合hash表
        Map<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        pmsSkuInfos.forEach(pmsSkuInfo1 -> {
            String k = "";
            String v = pmsSkuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValues = pmsSkuInfo1.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValues) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            skuSaleAttrHash.put(k, v);
        });
        //将sku的销售属性hash表放到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);

        return "item";
    }

}