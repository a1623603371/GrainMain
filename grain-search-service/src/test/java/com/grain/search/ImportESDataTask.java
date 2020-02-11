package com.grain.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.manage.PmsSkuInfo;
import com.grain.api.bean.search.PmsSearchSkuInfo;
import com.grain.api.service.manage.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.matchedqueries.MatchedQueriesFetchSubPhase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImportESDataTask {
    @Reference
    private SkuService skuService;
    @Autowired
    private JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {
        put();
    }

    @Test
    public void put() {
        //查询出所有商品
        List<PmsSkuInfo> pmsSkuInfos = skuService.getAllSku();
        //es数据集合
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        //转换成es数据结构
        pmsSkuInfos.forEach(pmsSkuInfo -> {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        });
        //导入到es
        pmsSearchSkuInfos.forEach(pmsSearchSkuInfo -> {
                    Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
                    Bulk.Builder bulk = new Bulk.Builder();
                    try {
                        bulk.addAction(put);

                        BulkResult result = jestClient.execute(bulk.build());
                        if (!ObjectUtils.isEmpty(result))
                            System.out.println(result.getJsonObject());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @Test
    public void get() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "43");
        boolQueryBuilder.filter(termQueryBuilder);
        //must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "华为");
        boolQueryBuilder.must(matchQueryBuilder);
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        searchSourceBuilder.highlight(null);
        String dslStr = searchSourceBuilder.toString();
        System.out.println(dslStr);
        //使用api执行查询
        Search search = new Search.Builder(dslStr).addIndex("gmall0105").addType("PmsSkuInfo").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        hits.forEach(pmsSearchSkuInfoVoidHit -> pmsSearchSkuInfos.add(pmsSearchSkuInfoVoidHit.source)
        );
        System.out.println(pmsSearchSkuInfos);

    }


}
