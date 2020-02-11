package com.grain.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.grain.api.bean.manage.PmsBaseAttrInfo;
import com.grain.api.bean.manage.PmsBaseAttrValue;
import com.grain.api.service.manage.AttrService;

import com.grain.manage.mapper.PmsBaseAttrInfoMapper;
import com.grain.manage.mapper.PmsBaseAttrValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        pmsBaseAttrInfos.forEach(pmsBaseAttrInfo1 -> {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo1.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            pmsBaseAttrInfo1.setAttrValueList(pmsBaseAttrValues);
        });
        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();
        //id为空保存
        if (StringUtils.isEmpty(id)) {
            //保存属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            //保存属性值
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
            pmsBaseAttrValues.forEach(pmsBaseAttrValue -> {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            });
            return "success";
        } else {
            //如果id不为空表示修改
            Example example = new Example(PmsBaseAttrInfo.class);
            //根据id查询修改
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);
            //修改属性值,先删除，在重新插入
            PmsBaseAttrValue deletePmsBaseAttrValue = new PmsBaseAttrValue();
            deletePmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(deletePmsBaseAttrValue);
            //保存属性值
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
            pmsBaseAttrValues.forEach(pmsBaseAttrValue -> {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            });
            return "success";
        }

    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set valueIdSet) {
        String valueList = com.alibaba.dubbo.common.utils.StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueList);
        return pmsBaseAttrInfos;
    }
}
