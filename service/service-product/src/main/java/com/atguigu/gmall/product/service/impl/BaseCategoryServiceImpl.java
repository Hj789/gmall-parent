package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.dao.BaseCategory2Dao;
import com.atguigu.gmall.product.dao.BaseCategory3Dao;
import com.atguigu.gmall.product.dao.BaseCategoryDao;
import com.atguigu.gmall.product.service.BaseCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class BaseCategoryServiceImpl implements BaseCategoryService {

   // @Autowired
    @Resource
    BaseCategoryDao baseCategoryDao;

    @Autowired
    BaseCategory2Dao baseCategory2Dao;

    @Autowired
    BaseCategory3Dao baseCategory3Dao;

    @Override
    public List<BaseCategory1> getCategory1() {
        List<BaseCategory1> category1s = baseCategoryDao.selectList(null);
        return category1s;
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {

        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Dao.selectList(queryWrapper);

        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category2_id",category2Id);
        List<BaseCategory3> baseCategory3s = baseCategory3Dao.selectList(queryWrapper);
        return baseCategory3s;
    }
}
