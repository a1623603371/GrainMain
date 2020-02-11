package com.grain.manage.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.manage.PmsBaseCatalog1;
import com.grain.api.bean.manage.PmsBaseCatalog2;
import com.grain.api.bean.manage.PmsBaseCatalog3;
import com.grain.api.service.manage.CatalogService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class CatalogController {
    @Reference
    private CatalogService catalogService;

    /**
     * 一级分类
     *
     * @return
     */
    @RequestMapping(value = "/getCatalog1", method = RequestMethod.POST)
    public List<PmsBaseCatalog1> getCatalog1() {
        List<PmsBaseCatalog1> pmsBaseCatalog1s = catalogService.getCatalog1();
        return pmsBaseCatalog1s;
    }

    /**
     * 二级分类
     *
     * @return
     */
    @RequestMapping(value = "/getCatalog2", method = RequestMethod.POST)
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        List<PmsBaseCatalog2> pmsBaseCatalog2s = catalogService.getCatalog2(catalog1Id);
        return pmsBaseCatalog2s;
    }

    /**
     * 三级分类
     *
     * @return
     */
    @RequestMapping(value = "/getCatalog3", method = RequestMethod.POST)
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        List<PmsBaseCatalog3> pmsBaseCatalog3s = catalogService.getCatalog3(catalog2Id);
        return pmsBaseCatalog3s;
    }
}
