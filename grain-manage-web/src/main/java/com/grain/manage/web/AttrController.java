package com.grain.manage.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.grain.api.bean.manage.PmsBaseAttrInfo;
import com.grain.api.bean.manage.PmsBaseAttrValue;
import com.grain.api.service.manage.AttrService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class AttrController {
    @Reference
    private AttrService attrService;

    /**
     * 查看属性
     *
     * @param catalog3Id
     * @return
     */
    @RequestMapping(value = "/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        List<PmsBaseAttrInfo> pmsBaseAttrInfo = attrService.getAttrInfoList(catalog3Id);
        return pmsBaseAttrInfo;
    }

    /**
     * 添加属性
     *
     * @return
     */
    @RequestMapping(value = "/saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {
        if (ObjectUtils.isEmpty(pmsBaseAttrInfo)) {
            return "属性对象为空";
        }
        String result = attrService.saveAttrInfo(pmsBaseAttrInfo);

        return result;
    }

    @RequestMapping(value = "/getAttrValueList", method = RequestMethod.POST)
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        List<PmsBaseAttrValue> pmsBaseAttrValues = attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;

    }
}
