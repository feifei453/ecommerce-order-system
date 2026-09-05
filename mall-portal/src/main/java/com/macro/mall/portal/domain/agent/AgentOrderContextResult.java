package com.macro.mall.portal.domain.agent;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 客服Agent聚合订单上下文。
 */
@Data
public class AgentOrderContextResult {
    @ApiModelProperty("订单信息")
    private AgentOrderToolResult order;

    @ApiModelProperty("物流信息")
    private AgentLogisticsToolResult logistics;

    @ApiModelProperty("售后申请列表")
    private List<AgentReturnApplyToolResult> returnApplies;
}
