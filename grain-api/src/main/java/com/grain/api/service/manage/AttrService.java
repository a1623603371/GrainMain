package com.grain.api.service.manage;

import com.grain.api.bean.manage.PmsBaseAttrInfo;
import com.grain.api.bean.manage.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id);

    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    public List<PmsBaseAttrValue> getAttrValueList(String attrId);

    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set valueIdSet);

}
