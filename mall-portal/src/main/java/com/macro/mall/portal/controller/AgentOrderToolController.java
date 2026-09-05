package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.domain.agent.AgentLogisticsToolResult;
import com.macro.mall.portal.domain.agent.AgentOrderContextResult;
import com.macro.mall.portal.domain.agent.AgentOrderToolResult;
import com.macro.mall.portal.domain.agent.AgentReturnApplyToolResult;
import com.macro.mall.portal.service.AgentOrderToolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客服Agent工具API。
 */
@Controller
@Api(tags = "AgentOrderToolController")
@Tag(name = "AgentOrderToolController", description = "客服Agent订单工具API")
@RequestMapping("/agent-tools")
public class AgentOrderToolController {
    @Autowired
    private AgentOrderToolService agentOrderToolService;

    @ApiOperation("Agent工具：根据订单编号查询订单详情")
    @RequestMapping(value = "/orders/{orderSn}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<AgentOrderToolResult> getOrderByOrderSn(@PathVariable String orderSn) {
        return CommonResult.success(agentOrderToolService.getOrderByOrderSn(orderSn));
    }

    @ApiOperation("Agent工具：根据订单ID查询订单详情")
    @RequestMapping(value = "/orders/id/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<AgentOrderToolResult> getOrderById(@PathVariable Long orderId) {
        return CommonResult.success(agentOrderToolService.getOrderById(orderId));
    }

    @ApiOperation("Agent工具：根据订单编号查询物流信息")
    @RequestMapping(value = "/orders/{orderSn}/logistics", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<AgentLogisticsToolResult> getLogistics(@PathVariable String orderSn) {
        return CommonResult.success(agentOrderToolService.getLogisticsByOrderSn(orderSn));
    }

    @ApiOperation("Agent工具：根据订单编号查询售后申请")
    @RequestMapping(value = "/orders/{orderSn}/return-applies", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<AgentReturnApplyToolResult>> listReturnApplies(@PathVariable String orderSn) {
        return CommonResult.success(agentOrderToolService.listReturnAppliesByOrderSn(orderSn));
    }

    @ApiOperation("Agent工具：根据订单编号聚合查询订单上下文")
    @RequestMapping(value = "/orders/{orderSn}/context", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<AgentOrderContextResult> getOrderContext(@PathVariable String orderSn) {
        return CommonResult.success(agentOrderToolService.getOrderContext(orderSn));
    }
}
