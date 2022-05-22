package com.atguigu.gmall.product.dao;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.to.CategoryAndChildTo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

//@Mapper
public interface BaseCategoryDao extends BaseMapper<BaseCategory1> {

    /**
     * 查询所有分类以及子分类数据
     * @return
     */
    List<CategoryAndChildTo> getAllCategoryWithChilds();

    /**
     * 查询sku的分类路径层级
     * @param skuId
     * @return
     */
    BaseCategoryView getSkuCategoryView(@Param("skuId") Long skuId);
}
