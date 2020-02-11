package com.grain.search.service.lmpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.grain.api.bean.manage.PmsSkuAttrValue;
import com.grain.api.bean.search.PmsSearchParam;
import com.grain.api.bean.search.PmsSearchSkuInfo;
import com.grain.api.service.search.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregationStreams;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> getSearchSkuInfos(PmsSearchParam pmsSearchParam) {
        String dslStr = getSearchDsl(pmsSearchParam);
        System.err.println(dslStr);
        //用api执行查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder(dslStr).addIndex("gmall0105").addType("PmsSkuInfo").build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);

        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        hits.forEach(pmsSearchSkuInfoVoidHit -> {
                    PmsSearchSkuInfo pmsSearchSkuInfo = pmsSearchSkuInfoVoidHit.source;
                    Map<String, List<String>> highlight = pmsSearchSkuInfoVoidHit.highlight;
                    if (!ObjectUtils.isEmpty(highlight)) {
                        String skuName = highlight.get("skuName").get(0);
                        pmsSearchSkuInfo.setSkuName(skuName);
                    }
                    pmsSearchSkuInfos.add(pmsSearchSkuInfo);
                }

        );
        System.out.println(pmsSearchSkuInfos.size());
        return pmsSearchSkuInfos;
    }

    public String getSearchDsl(PmsSearchParam pmsSearchParam) {
        String[] pmsSkuAttrValues = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        //jest工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        if (!StringUtils.isEmpty(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (!ObjectUtils.isEmpty(pmsSkuAttrValues)) {
            for (String valueId : pmsSkuAttrValues) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }
        //must
        if (!StringUtils.isEmpty(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<Span Style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("<Span />");
        searchSourceBuilder.highlight(highlightBuilder);
        //sort
        searchSourceBuilder.sort("id", SortOrder.DESC);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //aggs
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        return searchSourceBuilder.toString();
    }
}
