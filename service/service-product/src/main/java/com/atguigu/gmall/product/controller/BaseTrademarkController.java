package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理和品牌有关的请求
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    BaseTrademarkService baseTrademarkService;

    /**
     * 分页查询所有品牌
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/{pageNum}/{pageSize}")
    public Result getBaseTrademarkPage(@PathVariable Long pageNum,
                                       @PathVariable Long pageSize){

        Page<BaseTrademark> page = new Page<>(pageNum,pageSize);
        //调用分页查询方法
        Page<BaseTrademark> result = baseTrademarkService.page(page);
        //前端全量接受分页数据以及查到的结果
        return Result.ok(result);
    }

    @PostMapping("/save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @GetMapping("/get/{id}")
    public Result getBaseTrademarkById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @PutMapping("/update")
    public Result updateBaseTrademark(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @DeleteMapping("/remove/{id}")
    public Result deleteBaseTrademark(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    /**
     * 获取所有品牌信息
     * /admin/product/baseTrademark/getTrademarkList
     */
    @GetMapping("/getTrademarkList")
    public Result  getTrademarkList(){
        List<BaseTrademark> list = baseTrademarkService.list();
        return Result.ok(list);
    }





}
