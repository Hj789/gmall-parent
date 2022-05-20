package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理和平台属性有关的请求
 */
//admin/product/attrInfoList/2/0/0
@Slf4j
@RestController
@RequestMapping("/admin/product")
public class BaseAttrController {

    @Autowired
    BaseAttrInfoService baseAttrInfoService;


    /**
     * 获取制定分类下所有的平台属性列表
     * @return
     */
    @GetMapping("/attrInfoList/{c1Id}/{c2Id}/{c3Id}")
    public Result getAttrInfoList(@PathVariable Long c1Id,
                                  @PathVariable Long c2Id,
                                  @PathVariable Long c3Id){
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoService.findByAttrInfoAndAttrValueByCategoryId(c1Id,c2Id,c3Id);
        return Result.ok(baseAttrInfoList);
    }

    /**
     * 保存平台属性名
     * @param baseAttrInfo
     * @return
     */
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        log.info("保存/修改平台属性:{}",baseAttrInfo);
        //保存或修改
        baseAttrInfoService.saveOrUpdateAttrInfo(baseAttrInfo);

//        baseAttrInfoService.saveAttrInfoAndValue(baseAttrInfo);
        return Result.ok();
    }

    /**
     * 查询某个属性的名和值
     * @param attrId
     * @return
     */
    @GetMapping("/getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){

        //BaseAttrInfo baseAttrInfo = baseAttrInfoService.findAttrInfoAndValueByAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrInfoService.findAttrValuesByAttr(attrId);
        return Result.ok(baseAttrValueList);
    }

}
