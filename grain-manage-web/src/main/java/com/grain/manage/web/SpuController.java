package com.grain.manage.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.manage.PmsBaseSaleAttr;
import com.grain.api.bean.manage.PmsProductImage;
import com.grain.api.bean.manage.PmsProductInfo;
import com.grain.api.bean.manage.PmsProductSaleAttr;
import com.grain.api.service.manage.SpuService;
import com.grain.utils.PmsUploadUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {
    @Reference
    private SpuService spuService;

    @RequestMapping(value = "/spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("/baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = spuService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }

    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        String imagUrl = PmsUploadUtils.UploadFImage(multipartFile);
        System.out.println(imagUrl);
        return imagUrl;
    }

    @RequestMapping(value = "/saveSpuInfo", method = RequestMethod.POST)
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        if (ObjectUtils.isEmpty(pmsProductInfo)) {
            return "没有对象";
        }
        return spuService.saveSpuInfo(pmsProductInfo);
    }

    @RequestMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }
}
