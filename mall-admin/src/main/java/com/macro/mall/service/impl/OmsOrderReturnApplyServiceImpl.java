package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.common.enums.OrderStatus;
import com.macro.mall.common.enums.ReturnApplyStatus;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.dao.OmsOrderReturnApplyDao;
import com.macro.mall.dto.OmsOrderReturnApplyResult;
import com.macro.mall.dto.OmsReturnApplyQueryParam;
import com.macro.mall.dto.OmsUpdateStatusParam;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.mapper.OmsOrderReturnApplyMapper;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.OmsOrderOperateHistory;
import com.macro.mall.model.OmsOrderReturnApply;
import com.macro.mall.model.OmsOrderReturnApplyExample;
import com.macro.mall.service.OmsOrderReturnApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 订单退货管理Service实现类
 * Created by macro on 2018/10/18.
 */
@Service
public class OmsOrderReturnApplyServiceImpl implements OmsOrderReturnApplyService {
    @Autowired
    private OmsOrderReturnApplyDao returnApplyDao;
    @Autowired
    private OmsOrderReturnApplyMapper returnApplyMapper;
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;

    @Override
    public List<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        return returnApplyDao.getList(queryParam);
    }

    @Override
    public int delete(List<Long> ids) {
        OmsOrderReturnApplyExample example = new OmsOrderReturnApplyExample();
        example.createCriteria().andIdIn(ids).andStatusEqualTo(3);
        return returnApplyMapper.deleteByExample(example);
    }

    @Override
    @Transactional
    public int updateStatus(Long id, OmsUpdateStatusParam statusParam) {
        OmsOrderReturnApply currentApply = returnApplyMapper.selectByPrimaryKey(id);
        if (currentApply == null) {
            Asserts.fail("退货申请不存在！");
        }
        Integer status = statusParam.getStatus();
        if (!ReturnApplyStatus.canTransit(currentApply.getStatus(), status)) {
            Asserts.fail("当前退货申请状态不允许该操作！");
        }
        OmsOrderReturnApply returnApply = new OmsOrderReturnApply();
        if(ReturnApplyStatus.RETURNING.getValue().equals(status)){
            //确认退货
            returnApply.setId(id);
            returnApply.setStatus(ReturnApplyStatus.RETURNING.getValue());
            returnApply.setReturnAmount(statusParam.getReturnAmount());
            returnApply.setCompanyAddressId(statusParam.getCompanyAddressId());
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else if(ReturnApplyStatus.COMPLETED.getValue().equals(status)){
            //完成退货
            returnApply.setId(id);
            returnApply.setStatus(ReturnApplyStatus.COMPLETED.getValue());
            returnApply.setReceiveTime(new Date());
            returnApply.setReceiveMan(statusParam.getReceiveMan());
            returnApply.setReceiveNote(statusParam.getReceiveNote());
        }else if(ReturnApplyStatus.REJECTED.getValue().equals(status)){
            //拒绝退货
            returnApply.setId(id);
            returnApply.setStatus(ReturnApplyStatus.REJECTED.getValue());
            returnApply.setHandleTime(new Date());
            returnApply.setHandleMan(statusParam.getHandleMan());
            returnApply.setHandleNote(statusParam.getHandleNote());
        }else{
            return 0;
        }
        int count = returnApplyMapper.updateByPrimaryKeySelective(returnApply);
        if (count > 0) {
            if (ReturnApplyStatus.COMPLETED.getValue().equals(status)) {
                updateOrderClosed(currentApply.getOrderId());
            }
            addOrderOperateHistory(currentApply, statusParam, status);
        }
        return count;
    }

    @Override
    public OmsOrderReturnApplyResult getItem(Long id) {
        return returnApplyDao.getDetail(id);
    }

    private void updateOrderClosed(Long orderId) {
        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setStatus(OrderStatus.CLOSED.getValue());
        order.setModifyTime(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
    }

    private void addOrderOperateHistory(OmsOrderReturnApply currentApply, OmsUpdateStatusParam statusParam, Integer returnApplyStatus) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(currentApply.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan(resolveOperateMan(statusParam));
        history.setOrderStatus(resolveOrderStatus(currentApply.getOrderId(), returnApplyStatus));
        history.setNote(resolveHistoryNote(statusParam, returnApplyStatus));
        orderOperateHistoryMapper.insert(history);
    }

    private String resolveOperateMan(OmsUpdateStatusParam statusParam) {
        if (statusParam.getHandleMan() != null && !"".equals(statusParam.getHandleMan())) {
            return statusParam.getHandleMan();
        }
        if (statusParam.getReceiveMan() != null && !"".equals(statusParam.getReceiveMan())) {
            return statusParam.getReceiveMan();
        }
        return "后台管理员";
    }

    private Integer resolveOrderStatus(Long orderId, Integer returnApplyStatus) {
        if (ReturnApplyStatus.COMPLETED.getValue().equals(returnApplyStatus)) {
            return OrderStatus.CLOSED.getValue();
        }
        OmsOrder order = orderMapper.selectByPrimaryKey(orderId);
        return order == null ? null : order.getStatus();
    }

    private String resolveHistoryNote(OmsUpdateStatusParam statusParam, Integer returnApplyStatus) {
        if (ReturnApplyStatus.RETURNING.getValue().equals(returnApplyStatus)) {
            return "售后审核通过，等待用户退货：" + safeText(statusParam.getHandleNote());
        }
        if (ReturnApplyStatus.COMPLETED.getValue().equals(returnApplyStatus)) {
            return "退货收货完成，订单关闭：" + safeText(statusParam.getReceiveNote());
        }
        if (ReturnApplyStatus.REJECTED.getValue().equals(returnApplyStatus)) {
            return "售后审核拒绝：" + safeText(statusParam.getHandleNote());
        }
        return "售后状态更新";
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
