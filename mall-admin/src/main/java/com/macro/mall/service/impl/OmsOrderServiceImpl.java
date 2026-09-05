package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.common.enums.OrderStatus;
import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.dao.OmsOrderOperateHistoryDao;
import com.macro.mall.dto.*;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.OmsOrderExample;
import com.macro.mall.model.OmsOrderOperateHistory;
import com.macro.mall.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单管理Service实现类
 * Created by macro on 2018/10/11.
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderDao orderDao;
    @Autowired
    private OmsOrderOperateHistoryDao orderOperateHistoryDao;
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;

    @Override
    public List<OmsOrder> list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        return orderDao.getList(queryParam);
    }

    @Override
    public int delivery(List<OmsOrderDeliveryParam> deliveryParamList) {
        if (deliveryParamList == null || deliveryParamList.isEmpty()) {
            return 0;
        }
        List<Long> orderIds = deliveryParamList.stream()
                .map(OmsOrderDeliveryParam::getOrderId)
                .collect(Collectors.toList());
        Set<Long> deliverableOrderIds = selectOrderIdsByStatus(orderIds, OrderStatus.PENDING_DELIVERY.getValue());
        List<OmsOrderDeliveryParam> deliverableParamList = deliveryParamList.stream()
                .filter(item -> deliverableOrderIds.contains(item.getOrderId()))
                .collect(Collectors.toList());
        if (deliverableParamList.isEmpty()) {
            return 0;
        }
        //批量发货
        int count = orderDao.delivery(deliverableParamList);
        //添加操作记录
        List<OmsOrderOperateHistory> operateHistoryList = deliverableParamList.stream()
                .map(omsOrderDeliveryParam -> {
                    OmsOrderOperateHistory history = new OmsOrderOperateHistory();
                    history.setOrderId(omsOrderDeliveryParam.getOrderId());
                    history.setCreateTime(new Date());
                    history.setOperateMan("后台管理员");
                    history.setOrderStatus(OrderStatus.DELIVERED.getValue());
                    history.setNote("完成发货");
                    return history;
                }).collect(Collectors.toList());
        orderOperateHistoryDao.insertList(operateHistoryList);
        return count;
    }

    @Override
    public int close(List<Long> ids, String note) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Set<Long> closableOrderIds = selectOrderIdsByStatus(ids, OrderStatus.PENDING_PAYMENT.getValue());
        if (closableOrderIds.isEmpty()) {
            return 0;
        }
        OmsOrder record = new OmsOrder();
        record.setStatus(OrderStatus.CLOSED.getValue());
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andIdIn(new java.util.ArrayList<>(closableOrderIds))
                .andStatusEqualTo(OrderStatus.PENDING_PAYMENT.getValue());
        int count = orderMapper.updateByExampleSelective(record, example);
        if (count == 0) {
            return 0;
        }
        List<OmsOrderOperateHistory> historyList = closableOrderIds.stream().map(orderId -> {
            OmsOrderOperateHistory history = new OmsOrderOperateHistory();
            history.setOrderId(orderId);
            history.setCreateTime(new Date());
            history.setOperateMan("后台管理员");
            history.setOrderStatus(OrderStatus.CLOSED.getValue());
            history.setNote("订单关闭:"+note);
            return history;
        }).collect(Collectors.toList());
        if (!historyList.isEmpty()) {
            orderOperateHistoryDao.insertList(historyList);
        }
        return count;
    }

    @Override
    public int delete(List<Long> ids) {
        OmsOrder record = new OmsOrder();
        record.setDeleteStatus(1);
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdIn(ids);
        return orderMapper.updateByExampleSelective(record, example);
    }

    @Override
    public OmsOrderDetail detail(Long id) {
        return orderDao.getDetail(id);
    }

    @Override
    public int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(receiverInfoParam.getStatus());
        history.setNote("修改收货人信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(moneyInfoParam.getOrderId());
        order.setFreightAmount(moneyInfoParam.getFreightAmount());
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        //插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(moneyInfoParam.getStatus());
        history.setNote("修改费用信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    @Override
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(status);
        history.setNote("修改备注信息："+note);
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    private Set<Long> selectOrderIdsByStatus(List<Long> orderIds, Integer status) {
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andIdIn(orderIds)
                .andStatusEqualTo(status);
        return orderMapper.selectByExample(example).stream()
                .map(OmsOrder::getId)
                .collect(Collectors.toSet());
    }
}
