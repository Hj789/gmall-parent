package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.to.CategoryAndChildTo;

import java.util.List;

public interface BaseCategoryService {

    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long category1Id);

    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 获取所有分类以及子分类数据
     * @return
     */
    List<CategoryAndChildTo> getAllCategoryWithChilds();

    BaseCategoryView getSkuCategoryView(Long skuId);
}
