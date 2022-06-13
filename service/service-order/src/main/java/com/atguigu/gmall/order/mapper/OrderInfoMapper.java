package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.atguigu.gmall.order.domain.OrderInfo1
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    void updateOrderStatus(@Param("originStatus") String originStatus,
                           @Param("orderInfo") OrderInfo orderInfo);

    void updateOrderStatusToPaid(@Param("outTradeNo") String outTradeNo,
                                 @Param("userId") long userId,
                                 @Param("processStatus") String processStatus,
                                 @Param("orderStatus") String orderStatus);


    /**
     * 找到这个流水号对应的订单
     * @param userId
     * @param outTradeNo
     * @return
     */
    List<OrderInfo> getOrderAndDetailByOutTradeNo(@Param("userId") long userId, @Param("outTradeNo") String outTradeNo);

    /**
     * 修改订单状态为已拆分
     * @param processStatus
     * @param orderStatus
     * @param userId
     * @param id
     */
    void updateStatus(@Param("processStatus") String processStatus, @Param("orderStatus") String orderStatus, @Param("userId") Long userId, @Param("id") Long id);

    /**
     * 修改订单状态
     * @param processStatus
     * @param orderStatus
     * @param orderId
     */
    void updateStatusById(@Param("processStatus") String processStatus,
                          @Param("orderStatus") String orderStatus,
                          @Param("orderId") Long orderId);
}




