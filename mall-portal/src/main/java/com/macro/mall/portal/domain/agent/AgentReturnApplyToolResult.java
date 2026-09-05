package com.macro.mall.portal.domain.agent;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 客服Agent查询售后工具返回结果。
 */
@Data
public class AgentReturnApplyToolResult {
    @ApiModelProperty("售后申请ID")
    private Long id;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("售后状态")
    private Integer status;

    @ApiModelProperty("售后状态描述")
    private String statusText;

    @ApiModelProperty("退款金额")
    private BigDecimal returnAmount;

    @ApiModelProperty("退货商品")
    private String productName;

    @ApiModelProperty("退货数量")
    private Integer productCount;

    @ApiModelProperty("申请原因")
    private String reason;

    @ApiModelProperty("问题描述")
    private String description;

    @ApiModelProperty("处理备注")
    private String handleNote;

    @ApiModelProperty("处理人")
    private String handleMan;

    @ApiModelProperty("收货备注")
    private String receiveNote;

    @ApiModelProperty("收货人")
    private String receiveMan;

    @ApiModelProperty("申请时间")
    private Date createTime;

    @ApiModelProperty("处理时间")
    private Date handleTime;

    @ApiModelProperty("收货时间")
    private Date receiveTime;
}
