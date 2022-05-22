package com.atguigu.gmall.model.to;

import lombok.Data;

import java.util.List;

/**
 * 分类以及自分类数据模型
 */
@Data
public class CategoryAndChildTo {
    Long categoryId; //当前分类id
    String categoryName; //当前分类名字
    List<CategoryAndChildTo> categoryChild; //当前分类子分类
}
