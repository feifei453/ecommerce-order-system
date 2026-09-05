package com.macro.mall.portal.domain;

import com.macro.mall.model.OmsOrderOperateHistory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 订单超时关闭链路追踪结果。
 */
@Data
public class OrderTimeoutCancelTrace {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("正常消费队列")
    private String queueName;

    @ApiModelProperty("延迟TTL队列")
    private String ttlQueueName;

    @ApiModelProperty("正常交换机")
    private String exchange;

    @ApiModelProperty("延迟交换机")
    private String ttlExchange;

    @ApiModelProperty("库存锁定状态")
    private List<OrderSkuStockLockStatus> stockLockStatusList;

    @ApiModelProperty("订单操作日志")
    private List<OmsOrderOperateHistory> historyList;
}
