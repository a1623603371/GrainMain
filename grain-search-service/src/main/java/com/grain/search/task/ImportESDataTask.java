package com.grain.search.task;

import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.bean.search.PmsSearchSkuInfo;
/*
import com.grain.api.service.manage.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImportESDataTask {
    @Autowired
    private SkuService skuService;
    @Autowired
    private JestClient jestClient;

    public  void put(){
        //查询出所有商品
        List<PmsSkuInfo> pmsSkuInfos = skuService.getAllSku();
        //es数据集合
        List<PmsSearchSkuInfo> pmsSearchSkuInfos=new ArrayList<>();
        //转换成es数据结构
        pmsSkuInfos.forEach(pmsSkuInfo -> {
            PmsSearchSkuInfo pmsSearchSkuInfo=new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        });
        //导入到es
        pmsSearchSkuInfos.forEach(pmsSearchSkuInfo -> {
                    Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
                }
            );
    }

    public static void main(String[] args) {
        ImportESDataTask importESDataTask=new ImportESDataTask();
       importESDataTask.put();
    }
}
*/
