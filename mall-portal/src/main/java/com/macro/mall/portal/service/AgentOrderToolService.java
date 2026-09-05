package com.macro.mall.portal.service;

import com.macro.mall.portal.domain.agent.AgentLogisticsToolResult;
import com.macro.mall.portal.domain.agent.AgentOrderContextResult;
import com.macro.mall.portal.domain.agent.AgentOrderToolResult;
import com.macro.mall.portal.domain.agent.AgentReturnApplyToolResult;

import java.util.List;

/**
 * 客服Agent业务工具API。
 */
public interface AgentOrderToolService {
    AgentOrderToolResult getOrderByOrderSn(String orderSn);

    AgentOrderToolResult getOrderById(Long orderId);

    AgentLogisticsToolResult getLogisticsByOrderSn(String orderSn);

    List<AgentReturnApplyToolResult> listReturnAppliesByOrderSn(String orderSn);

    AgentOrderContextResult getOrderContext(String orderSn);
}
