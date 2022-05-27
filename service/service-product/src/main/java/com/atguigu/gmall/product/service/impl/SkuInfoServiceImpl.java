package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.starter.cache.aop.CacheHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService{

    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageService skuImageService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Qualifier("skuIdBloom")
    @Autowired
    RBloomFilter<Object> skuIdBloom;

    @Autowired
    CacheHelper cacheHelper;

    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        Long id = skuInfo.getId();
        //1、SkuInfo 的基本信息  保存到 sku_info
        skuInfoMapper.insert(skuInfo);

        //sku的自增id
        Long skuId = skuInfo.getId();

        //给布隆保存skuId
        skuIdBloom.add(skuId);
        // 就算删了的，布隆说有，我们查询数据库结果为null，我们也会缓存null值。
        // 就算布隆误判或者真没，都不担心会跟数据库建立大量连接；

        //2、skuImageList sku图片集合； 存到 sku_image表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
        }
        skuImageService.saveBatch(skuImageList);

        //3、skuAttrValueList  sku的平台属性  保存到 sku_attr_value。前端提交的就够了
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
        }
        skuAttrValueService.saveBatch(skuAttrValueList);

        //4、skuSaleAttrValueList sku销售属性值集合 存到 sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue attrValue : skuSaleAttrValueList) {
            attrValue.setSkuId(skuId);
            attrValue.setSpuId(skuInfo.getSpuId());
        }
        skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);

        //删除缓存.快
        cacheHelper.deleteCache("sku:detail:47");


    }

    @Override
    public void upOrDownSku(Long skuId, int status) {
        //1、更数据库状态
        skuInfoMapper.updateSkuStatus(skuId,status);

        //TODO 2、给ES ，保存/删除 数据

    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return  skuInfoMapper.getPrice(skuId);
    }

    @Override
    public List<SpuSaleAttr> getSkudeSpuSaleAttrAndValue(Long skuId) {

        return spuSaleAttrMapper.getSkudeSpuSaleAttrAndValue(skuId);
    }

    @Override
    public List<Long> getAllSkuIds() {
        return skuInfoMapper.getSkuIds();
    }
}




