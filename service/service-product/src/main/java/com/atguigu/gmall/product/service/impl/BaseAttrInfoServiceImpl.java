package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService{

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> findByAttrInfoAndAttrValueByCategoryId(Long c1Id, Long c2Id, Long c3Id) {

        return  baseAttrInfoMapper.selectAttrInfoAndAttrValueByCategoryId(c1Id,c2Id,c3Id);
    }

    @Transactional
    @Override
    public void saveAttrInfoAndValue(BaseAttrInfo baseAttrInfo) {
    //1.保存属性名信息到base_attr_info
        baseAttrInfoMapper.insert(baseAttrInfo);

    //2. 保存属性值信息到base_attr_value
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            //回填属性id
            Long id = baseAttrInfo.getId();
            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Transactional
    @Override
    public void updateAttrInfoAndValue(BaseAttrInfo baseAttrInfo) {
        //1. 修改属性
        baseAttrInfoMapper.updateById(baseAttrInfo);

        //2.3 从提交的数据里面,对比发现数据库中原纪录此次没提交的id记录,这些id是要删除掉的
        List<Long> nodel_vids = new ArrayList<>();
        for (BaseAttrValue value : baseAttrInfo.getAttrValueList()) {
            if (value.getId() != null){
                nodel_vids.add(value.getId());
            }
        }
        //有东西,说明保留前端的部分数据
        if (nodel_vids.size() > 0){
        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id",baseAttrInfo.getId());
        queryWrapper.notIn("id",nodel_vids);
        //所有属性值不在前端提交的范围内的都是不要的,删除掉.
        baseAttrValueMapper.delete(queryWrapper);
        }else {
            //没东西,全删除
            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);
            QueryWrapper<BaseAttrInfo> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("id",baseAttrInfo.getId());
            baseAttrInfoMapper.delete(queryWrapper1);
        }



        //2. 修改属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue value : attrValueList) {
            Long id = value.getId();
            //2.1 有id的属性值,直接修改base_attr_value
            if (id != null){
                baseAttrValueMapper.updateById(value);
            }
            //2.2 无id的 属性值,是新增base_attr_value
            if (id == null){
                //新增要回填id
                value.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(value);
            }
        }


    }

    @Override
    public BaseAttrInfo findAttrInfoAndValueByAttrId(Long attrId) {

        return baseAttrInfoMapper.findAttrInfoAndValueByAttrId(attrId);
    }

    @Override
    public List<BaseAttrValue> findAttrValuesByAttr(Long attrId) {
        return baseAttrValueMapper.findAttrValuesByAttr(attrId);
    }

    @Transactional
    @Override
    public void saveOrUpdateAttrInfo(BaseAttrInfo baseAttrInfo) {
        Long id = baseAttrInfo.getId();
        if (id == null){
            //前端没有提交属性id,则是新增属性
            saveAttrInfoAndValue(baseAttrInfo);
        }else {
            //前端提交了属性id,则是修改属性
            updateAttrInfoAndValue(baseAttrInfo);
        }
    }
}




