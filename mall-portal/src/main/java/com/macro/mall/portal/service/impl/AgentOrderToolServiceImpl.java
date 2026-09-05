package com.macro.mall.portal.service.impl;

import com.macro.mall.common.enums.OrderStatus;
import com.macro.mall.common.enums.ReturnApplyStatus;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.mapper.OmsOrderReturnApplyMapper;
import com.macro.mall.model.*;
import com.macro.mall.portal.domain.agent.AgentLogisticsToolResult;
import com.macro.mall.portal.domain.agent.AgentOrderContextResult;
import com.macro.mall.portal.domain.agent.AgentOrderToolResult;
import com.macro.mall.portal.domain.agent.AgentReturnApplyToolResult;
import com.macro.mall.portal.service.AgentOrderToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客服Agent业务工具API实现。
 */
@Service
public class AgentOrderToolServiceImpl implements AgentOrderToolService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderItemMapper orderItemMapper;
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;
    @Autowired
    private OmsOrderReturnApplyMapper orderReturnApplyMapper;

    @Override
    public AgentOrderToolResult getOrderByOrderSn(String orderSn) {
        return buildOrderResult(getOrderBySn(orderSn));
    }

    @Override
    public AgentOrderToolResult getOrderById(Long orderId) {
        OmsOrder order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getDeleteStatus())) {
            Asserts.fail("订单不存在！");
        }
        return buildOrderResult(order);
    }

    @Override
    public AgentLogisticsToolResult getLogisticsByOrderSn(String orderSn) {
        OmsOrder order = getOrderBySn(orderSn);
        AgentLogisticsToolResult result = new AgentLogisticsToolResult();
        result.setOrderId(order.getId());
        result.setOrderSn(order.getOrderSn());
        result.setStatus(order.getStatus());
        result.setStatusText(orderStatusText(order.getStatus()));
        result.setDeliveryCompany(order.getDeliveryCompany());
        result.setDeliverySn(order.getDeliverySn());
        result.setDeliveryTime(order.getDeliveryTime());
        result.setReceiverName(order.getReceiverName());
        result.setReceiverPhone(maskPhone(order.getReceiverPhone()));
        result.setReceiverAddress(buildReceiverAddress(order));
        result.setLogisticsMessage(buildLogisticsMessage(order));
        return result;
    }

    @Override
    public List<AgentReturnApplyToolResult> listReturnAppliesByOrderSn(String orderSn) {
        OmsOrder order = getOrderBySn(orderSn);
        OmsOrderReturnApplyExample example = new OmsOrderReturnApplyExample();
        example.createCriteria().andOrderIdEqualTo(order.getId());
        example.setOrderByClause("create_time desc");
        return orderReturnApplyMapper.selectByExample(example).stream()
                .map(this::buildReturnApplyResult)
                .collect(Collectors.toList());
    }

    @Override
    public AgentOrderContextResult getOrderContext(String orderSn) {
        AgentOrderContextResult result = new AgentOrderContextResult();
        result.setOrder(getOrderByOrderSn(orderSn));
        result.setLogistics(getLogisticsByOrderSn(orderSn));
        result.setReturnApplies(listReturnAppliesByOrderSn(orderSn));
        return result;
    }

    private OmsOrder getOrderBySn(String orderSn) {
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria()
                .andOrderSnEqualTo(orderSn)
                .andDeleteStatusEqualTo(0);
        List<OmsOrder> orderList = orderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(orderList)) {
            Asserts.fail("订单不存在！");
        }
        return orderList.get(0);
    }

    private AgentOrderToolResult buildOrderResult(OmsOrder order) {
        AgentOrderToolResult result = new AgentOrderToolResult();
        result.setOrderId(order.getId());
        result.setOrderSn(order.getOrderSn());
        result.setMemberId(order.getMemberId());
        result.setMemberUsername(order.getMemberUsername());
        result.setStatus(order.getStatus());
        result.setStatusText(orderStatusText(order.getStatus()));
        result.setPayAmount(order.getPayAmount());
        result.setPayType(order.getPayType());
        result.setCreateTime(order.getCreateTime());
        result.setPaymentTime(order.getPaymentTime());
        result.setDeliveryTime(order.getDeliveryTime());
        result.setReceiveTime(order.getReceiveTime());
        result.setDeliveryCompany(order.getDeliveryCompany());
        result.setDeliverySn(order.getDeliverySn());
        result.setReceiverName(order.getReceiverName());
        result.setReceiverPhone(maskPhone(order.getReceiverPhone()));
        result.setReceiverAddress(buildReceiverAddress(order));
        result.setItems(listOrderItems(order.getId()));
        result.setHistories(listOrderHistories(order.getId()));
        return result;
    }

    private List<OmsOrderItem> listOrderItems(Long orderId) {
        OmsOrderItemExample example = new OmsOrderItemExample();
        example.createCriteria().andOrderIdEqualTo(orderId);
        return orderItemMapper.selectByExample(example);
    }

    private List<OmsOrderOperateHistory> listOrderHistories(Long orderId) {
        OmsOrderOperateHistoryExample example = new OmsOrderOperateHistoryExample();
        example.createCriteria().andOrderIdEqualTo(orderId);
        example.setOrderByClause("create_time desc");
        return orderOperateHistoryMapper.selectByExample(example);
    }

    private AgentReturnApplyToolResult buildReturnApplyResult(OmsOrderReturnApply apply) {
        AgentReturnApplyToolResult result = new AgentReturnApplyToolResult();
        result.setId(apply.getId());
        result.setOrderId(apply.getOrderId());
        result.setOrderSn(apply.getOrderSn());
        result.setStatus(apply.getStatus());
        result.setStatusText(returnApplyStatusText(apply.getStatus()));
        result.setReturnAmount(apply.getReturnAmount());
        result.setProductName(apply.getProductName());
        result.setProductCount(apply.getProductCount());
        result.setReason(apply.getReason());
        result.setDescription(apply.getDescription());
        result.setHandleNote(apply.getHandleNote());
        result.setHandleMan(apply.getHandleMan());
        result.setReceiveNote(apply.getReceiveNote());
        result.setReceiveMan(apply.getReceiveMan());
        result.setCreateTime(apply.getCreateTime());
        result.setHandleTime(apply.getHandleTime());
        result.setReceiveTime(apply.getReceiveTime());
        return result;
    }

    private String orderStatusText(Integer status) {
        OrderStatus orderStatus = OrderStatus.of(status);
        return orderStatus == null ? "未知状态" : orderStatus.getLabel();
    }

    private String returnApplyStatusText(Integer status) {
        if (ReturnApplyStatus.PENDING.getValue().equals(status)) {
            return ReturnApplyStatus.PENDING.getLabel();
        }
        if (ReturnApplyStatus.RETURNING.getValue().equals(status)) {
            return ReturnApplyStatus.RETURNING.getLabel();
        }
        if (ReturnApplyStatus.COMPLETED.getValue().equals(status)) {
            return ReturnApplyStatus.COMPLETED.getLabel();
        }
        if (ReturnApplyStatus.REJECTED.getValue().equals(status)) {
            return ReturnApplyStatus.REJECTED.getLabel();
        }
        return "未知状态";
    }

    private String buildLogisticsMessage(OmsOrder order) {
        if (OrderStatus.PENDING_PAYMENT.getValue().equals(order.getStatus())) {
            return "订单待付款，暂未发货";
        }
        if (OrderStatus.PENDING_DELIVERY.getValue().equals(order.getStatus())) {
            return "订单已支付，等待仓库发货";
        }
        if (OrderStatus.DELIVERED.getValue().equals(order.getStatus()) || OrderStatus.COMPLETED.getValue().equals(order.getStatus())) {
            if (order.getDeliveryCompany() == null || order.getDeliverySn() == null) {
                return "订单已发货，但物流信息暂未补全";
            }
            return "订单已发货，可根据物流公司和物流单号查询轨迹";
        }
        if (OrderStatus.CLOSED.getValue().equals(order.getStatus())) {
            return "订单已关闭，无物流信息";
        }
        return "暂无物流提示";
    }

    private String buildReceiverAddress(OmsOrder order) {
        return safe(order.getReceiverProvince())
                + safe(order.getReceiverCity())
                + safe(order.getReceiverRegion())
                + safe(order.getReceiverDetailAddress());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
