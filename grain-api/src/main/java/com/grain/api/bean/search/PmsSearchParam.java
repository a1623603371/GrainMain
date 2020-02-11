package com.grain.api.bean.search;


import com.grain.api.bean.manage.PmsBaseAttrValue;
import com.grain.api.bean.manage.PmsSkuAttrValue;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PmsSearchParam implements Serializable {

    private String catalog3Id;

    private String keyword;
    private String[] valueId;


}
