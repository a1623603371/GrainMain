package com.grain.search.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.annotations.LoginRequrired;
import com.grain.api.bean.manage.PmsBaseAttrInfo;
import com.grain.api.bean.manage.PmsBaseAttrValue;
import com.grain.api.bean.manage.PmsSkuAttrValue;
import com.grain.api.bean.search.PmsSearchCrumb;
import com.grain.api.bean.search.PmsSearchParam;
import com.grain.api.bean.search.PmsSearchSkuInfo;
import com.grain.api.service.manage.AttrService;
import com.grain.api.service.search.SearchService;
import jdk.management.resource.internal.inst.FileOutputStreamRMHooks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.channels.FileChannel;
import java.util.*;


@SuppressWarnings("AlibabaStringConcat")
@Controller
public class SearchController {
    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @RequestMapping("/list")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        //搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.getSearchSkuInfos(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
        //抽取检索结果包含的平台的属性
        Set<String> valueIdSet = new HashSet<>();
        pmsSearchSkuInfos.forEach(pmsSearchSkuInfo -> {
            List<PmsSkuAttrValue> skuAttrValues = pmsSearchSkuInfo.getSkuAttrValueList();
            skuAttrValues.forEach(pmsSkuAttrValue -> {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            });
        });
        //使用valueId将平台属性查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);
        //对平台属性集合进行另一步处理,去掉当前条件中valueId的所在组
        String[] delValueIds = pmsSearchParam.getValueId();
        if (!StringUtils.isEmpty(delValueIds)) {
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delvalueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> pmsBaseAttrInfoIterator = pmsBaseAttrInfos.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑cangs
                pmsSearchCrumb.setValueId(delvalueId);
                pmsSearchCrumb.setUrlParam(getUrlParmForCrumb(pmsSearchParam, delvalueId));
                while (pmsBaseAttrInfoIterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = pmsBaseAttrInfoIterator.next();
                    List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
                    pmsBaseAttrValues.forEach(pmsBaseAttrValue -> {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delvalueId.equals(valueId)) {
                            //查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除属性值的所在的属性组
                            pmsBaseAttrInfoIterator.remove();
                        }
                    });
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);

        }
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            modelMap.put("keyword", keyword);
        }
        return "list";
    }

    private static String getUrlParmForCrumb(PmsSearchParam pmsSearchParam, String delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueIds = pmsSearchParam.getValueId();
        StringBuffer urlParam = new StringBuffer("");
        if (!StringUtils.isEmpty(keyword)) {
            if (StringUtils.isEmpty(urlParam)) {
                urlParam.append("&");
                //  urlParam=urlParam+"&";
            }
            //   urlParam=urlParam+"keyword="+keyword;
            urlParam.append("keyword=").append(keyword);
        }

        if (!StringUtils.isEmpty(catalog3Id)) {
            if (!StringUtils.isEmpty(urlParam)) {
                //  urlParam=urlParam+"&";
                urlParam.append("&");
            }
            // urlParam=urlParam+"catalog3Id="+catalog3Id;
            urlParam.append("catalog3Id=").append(catalog3Id);
        }
        if (!StringUtils.isEmpty(skuAttrValueIds)) {
            for (String pmsSkuAttrValueId : skuAttrValueIds) {
                if (!pmsSkuAttrValueId.equals(delValueId)) {
                    // urlParam=urlParam+"&+valueId="+pmsSkuAttrValueId;
                    urlParam.append("&valueId=").append(pmsSkuAttrValueId);
                }
            }
        }

        return urlParam.toString();

    }

    public static String getUrlParam(PmsSearchParam pmsSearchParam) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();
        StringBuffer urlParam = new StringBuffer("");
        if (!StringUtils.isEmpty(keyword)) {
            if (StringUtils.isEmpty(urlParam)) {
                // urlParam=urlParam+"&";
                urlParam.append("&");
            }
            // urlParam=urlParam+"keyword="+keyWord;
            urlParam.append("keyword=").append(keyword);
        }
        if (!StringUtils.isEmpty(catalog3Id)) {
            if (!StringUtils.isEmpty(urlParam)) {
                // urlParam=urlParam+"&";
                urlParam.append("&");
            }
            //urlParam=urlParam+"catalog3Id="+catalog3Id;
            urlParam.append("catalog3Id=").append(catalog3Id);
        }
        if (!StringUtils.isEmpty(skuAttrValueList)) {
            for (String skuAttrValueId : skuAttrValueList) {
                //urlParam=urlParam+"&valueId="+skuAttrValueId;
                urlParam.append("&valueId=").append(skuAttrValueId);
            }

        }
        return urlParam.toString();
    }

    @RequestMapping("/index")
    @LoginRequrired(loginSuccess = false)
    public String index() {
        return "index";
    }
}
