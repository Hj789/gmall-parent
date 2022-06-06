package com.atguigu.gmall.model.cart;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 购物车中的一个商品信息
 */
@Data
@ApiModel(description = "购物车中的一个商品")
public class CartItem  {
    private static final long serialVersionUID = 1L;

    private Long id; //商品id

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "skuid")
    private Long skuId; //商品id


    @ApiModelProperty(value = "数量")
    private Integer skuNum;

    @ApiModelProperty(value = "图片文件")
    private String skuDefaultImg;

    @ApiModelProperty(value = "sku名称 (冗余)")
    private String skuName;

    @ApiModelProperty(value = "isChecked")
    private Integer isChecked = 1;

    //  ,fill = FieldFill.INSERT
    private Date createTime;

    //  ,fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "放入购物车时价格")
    private BigDecimal cartPrice;

    // 实时价格 skuInfo.price
    BigDecimal skuPrice;


}
