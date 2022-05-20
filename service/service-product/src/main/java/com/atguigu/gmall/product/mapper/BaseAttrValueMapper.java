package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.atguigu.gmall.product.domain.BaseAttrValue
 */
public interface BaseAttrValueMapper extends BaseMapper<BaseAttrValue> {

    /**
     * 根据属性id返回所有属性值
     * @param attrId
     * @return
     */
    List<BaseAttrValue> findAttrValuesByAttr(@Param("attrId") Long attrId);
}




