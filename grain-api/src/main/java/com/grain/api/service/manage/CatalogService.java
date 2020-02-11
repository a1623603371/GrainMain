package com.grain.api.service.manage;

import com.grain.api.bean.manage.PmsBaseCatalog1;
import com.grain.api.bean.manage.PmsBaseCatalog2;
import com.grain.api.bean.manage.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {
    public List<PmsBaseCatalog1> getCatalog1();

    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
