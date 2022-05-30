package com.atguigu.gmall.product.service.impl;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.product.domain.CategoryView;
import com.atguigu.gmall.product.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import java.util.Date;

import com.atguigu.gmall.feign.list.SearchFeignClient;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.SkuAttrValueService;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.starter.cache.aop.CacheHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SkuInfoService;
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

    @Autowired
    SearchFeignClient searchFeignClient;

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    CategoryViewMapper categoryViewMapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

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

        if (status == 1){
            //2、上架给ES ，保存/删除 数据.查到当前sku的详细信息,封装程Goods
            Goods goods = this.getSkuInfoSearch(skuId);

            //3. 保存到es
            searchFeignClient.saveGoods(goods);
        }
        if (status == 0){
            //4.下架去es中删除数据
            searchFeignClient.deleteGoods(skuId);
        }

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

    @Override
    public Goods getSkuInfoSearch(Long skuId) {

        Goods goods = new Goods();

        //1.查询sku信息,并封装
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setCreateTime(new Date()); //上架时间
        //2. 封装品牌信息
        BaseTrademark trademark = baseTrademarkMapper.selectById(skuInfo.getTmId());
        goods.setTmId(trademark.getId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        //3. 封装分类信息
        QueryWrapper<CategoryView> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",skuInfo.getCategory3Id());
        CategoryView categoryView = categoryViewMapper.selectOne(wrapper);
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        //4. 热度
        goods.setHotScore(0L);

        //5. 当前sku 的所有平台属性的名和值
        //{attrId,attrValue,attrName}
        List<SearchAttr> searchAttrs = baseAttrInfoMapper.getSkuBaseAttrNameAndValue(skuId);
        goods.setAttrs(searchAttrs);

        return goods;
    }
}




