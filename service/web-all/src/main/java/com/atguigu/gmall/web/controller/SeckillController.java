package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.seckill.SeckillFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillFeignClient seckillFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    /**
     * 展示秒杀列表页
     * @param model
     * @return
     */
    @GetMapping("/seckill.html")
    public String seckillListPage(Model model){

        //远程查询出当天参与的秒杀的所有商品
        Result<List<SeckillGoods>> result = seckillFeignClient.queryCurrentDaySeckillGoods();


        //list.item.skuId skuDefaultImg skuName costPrice price num stockCount
        model.addAttribute("list",result.getData());

        return "seckill/index";
    }

    /**
     *  秒杀详情页
     * @param skuId
     * @return
     */
    @GetMapping("/seckill/{skuId}.html")
    public String seckillDetailPage(@PathVariable Long skuId,Model model){

        //TODO 查询当前skuId对应的秒杀商品的详细数据
        Result<SeckillGoods> result = seckillFeignClient.seckillGoodsDetail(skuId);

        model.addAttribute("item",result.getData());


        //秒杀详情页
        return "seckill/item";
    }


    /**
     * 秒杀排队页
     * @param skuId
     * @param skuIdStr
     * @return
     */
    @GetMapping("/seckill/queue.html")
    public String queuePage(@RequestParam("skuId") Long skuId,
                            @RequestParam("skuIdStr") String skuIdStr,
                            Model model){

        model.addAttribute("skuId",skuId);
        model.addAttribute("skuIdStr",skuIdStr);


        return "seckill/queue";
    }

    /**
     * 订单确认页
     * skuId=46
     */
    @GetMapping("/seckill/trade.html")
    public String orderConfirm(@RequestParam("skuId") Long skuId,Model model){

        //拿到这个商品的订单确认页数据
        Result<OrderInfo> info = seckillFeignClient.getOrderInfoBySkuId(skuId);
        List<OrderDetail> detailList = info.getData().getOrderDetailList();

        //detailArrayList：订单的所有商品列表
        model.addAttribute("detailArrayList",detailList);


        //totalNum
        model.addAttribute("totalNum","1");

        //totalAmount
        model.addAttribute("totalAmount",info.getData().getTotalAmount());

        //userAddressList
        Result<List<UserAddress>> addressList = userFeignClient.getUserAddressList();
        model.addAttribute("userAddressList",addressList.getData());
        return "seckill/trade";
    }


}
