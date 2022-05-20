package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface BaseAttrInfoService extends IService<BaseAttrInfo> {

    /**
     * 根据分类id查询对应的所有平台属性名和值
     * @param c1Id
     * @param c2Id
     * @param c3Id
     * @return
     */
    List<BaseAttrInfo> findByAttrInfoAndAttrValueByCategoryId(Long c1Id, Long c2Id, Long c3Id);

    /**
     * 保存平台属性名和值
     * @param baseAttrInfo
     */
    void saveAttrInfoAndValue(BaseAttrInfo baseAttrInfo);

    /**
     * 修改平台属性名和值
     * @param baseAttrInfo
     */
    void updateAttrInfoAndValue(BaseAttrInfo baseAttrInfo);

    /**
     * 根据属性id返回属性名和值
     * @param attrId
     * @return
     */
    BaseAttrInfo findAttrInfoAndValueByAttrId(Long attrId);

    /**
     * 根据属性id返回所有属性值
     * @param attrId
     * @return
     */
    List<BaseAttrValue> findAttrValuesByAttr(Long attrId);

    /**
     * 保存或更新属性
     * @param baseAttrInfo
     */
    void saveOrUpdateAttrInfo(BaseAttrInfo baseAttrInfo);
}
