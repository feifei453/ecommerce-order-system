package com.macro.mall.common.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 订单状态机，集中约束交易链路中的状态流转。
 */
public enum OrderStatus {
    PENDING_PAYMENT(0, "待付款"),
    PENDING_DELIVERY(1, "待发货"),
    DELIVERED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    INVALID(5, "无效订单");

    private final Integer value;
    private final String label;

    OrderStatus(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static OrderStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static boolean canPay(Integer currentStatus) {
        return PENDING_PAYMENT.value.equals(currentStatus);
    }

    public static boolean canCancel(Integer currentStatus) {
        return PENDING_PAYMENT.value.equals(currentStatus);
    }

    public static boolean canDeliver(Integer currentStatus) {
        return PENDING_DELIVERY.value.equals(currentStatus);
    }

    public static boolean canConfirmReceive(Integer currentStatus) {
        return DELIVERED.value.equals(currentStatus);
    }

    public static boolean canDelete(Integer currentStatus) {
        return COMPLETED.value.equals(currentStatus) || CLOSED.value.equals(currentStatus);
    }

    public static boolean canCloseByAdmin(Integer currentStatus) {
        return PENDING_PAYMENT.value.equals(currentStatus);
    }

    public static Set<Integer> valuesOf(OrderStatus... statuses) {
        Set<Integer> result = new HashSet<>();
        Arrays.stream(statuses).forEach(status -> result.add(status.value));
        return result;
    }
}
