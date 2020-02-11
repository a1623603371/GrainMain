package com.grain.manage.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.service.manage.SkuService;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SkuController {
    @Reference
    private SkuService skuService;

    @RequestMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo) {
        if (ObjectUtils.isEmpty(pmsSkuInfo)) {
            return "错误";
        }
        //将spu封装给product
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        //设置默认图片
        String skuDefaultImag = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isEmpty(skuDefaultImag)) {
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        skuService.saveSkuInfo(pmsSkuInfo);
        return "success";
    }
}
