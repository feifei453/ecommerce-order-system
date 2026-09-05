package com.macro.mall.portal.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单维度的SKU库存锁定状态，用于验证下单锁库存、支付扣库存、取消释放库存链路。
 */
@Data
public class OrderSkuStockLockStatus {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单编号")
    private String orderSn;

    @ApiModelProperty("订单状态")
    private Integer orderStatus;

    @ApiModelProperty("商品SKU ID")
    private Long productSkuId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("购买数量")
    private Integer productQuantity;

    @ApiModelProperty("SKU总库存")
    private Integer stock;

    @ApiModelProperty("SKU锁定库存")
    private Integer lockStock;

    @ApiModelProperty("SKU可售库存")
    private Integer availableStock;
}
