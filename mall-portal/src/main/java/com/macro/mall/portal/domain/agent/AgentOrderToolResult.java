package com.macro.mall.portal.domain.agent;

import com.macro.mall.model.OmsOrderItem;
import com.macro.mall.model.OmsOrderOperateHistory;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 客服Agent查询订单工具返回结果。
 */
@Data
public class AgentOrderToolResult {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("会员用户名")
    private String memberUsername;

    @ApiModelProperty("订单状态")
    private Integer status;

    @ApiModelProperty("订单状态描述")
    private String statusText;

    @ApiModelProperty("应付金额")
    private BigDecimal payAmount;

    @ApiModelProperty("支付方式")
    private Integer payType;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("支付时间")
    private Date paymentTime;

    @ApiModelProperty("发货时间")
    private Date deliveryTime;

    @ApiModelProperty("收货时间")
    private Date receiveTime;

    @ApiModelProperty("物流公司")
    private String deliveryCompany;

    @ApiModelProperty("物流单号")
    private String deliverySn;

    @ApiModelProperty("收货人")
    private String receiverName;

    @ApiModelProperty("收货人电话")
    private String receiverPhone;

    @ApiModelProperty("收货地址")
    private String receiverAddress;

    @ApiModelProperty("订单商品")
    private List<OmsOrderItem> items;

    @ApiModelProperty("订单操作日志")
    private List<OmsOrderOperateHistory> histories;
}
