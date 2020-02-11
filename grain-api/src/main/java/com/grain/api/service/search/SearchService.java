package com.grain.api.service.search;

import com.grain.api.bean.search.PmsSearchParam;
import com.grain.api.bean.search.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    public List<PmsSearchSkuInfo> getSearchSkuInfos(PmsSearchParam pmsSearchParam);
}
