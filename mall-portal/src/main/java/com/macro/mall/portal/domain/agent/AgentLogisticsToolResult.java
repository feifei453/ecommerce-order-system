package com.macro.mall.portal.domain.agent;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 客服Agent查询物流工具返回结果。
 */
@Data
public class AgentLogisticsToolResult {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("订单状态")
    private Integer status;

    @ApiModelProperty("订单状态描述")
    private String statusText;

    @ApiModelProperty("物流公司")
    private String deliveryCompany;

    @ApiModelProperty("物流单号")
    private String deliverySn;

    @ApiModelProperty("发货时间")
    private Date deliveryTime;

    @ApiModelProperty("收货人")
    private String receiverName;

    @ApiModelProperty("收货人电话")
    private String receiverPhone;

    @ApiModelProperty("收货地址")
    private String receiverAddress;

    @ApiModelProperty("物流提示")
    private String logisticsMessage;
}
