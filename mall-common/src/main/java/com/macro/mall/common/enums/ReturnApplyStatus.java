package com.macro.mall.common.enums;

/**
 * 售后退货申请状态机。
 */
public enum ReturnApplyStatus {
    PENDING(0, "待处理"),
    RETURNING(1, "退货中"),
    COMPLETED(2, "已完成"),
    REJECTED(3, "已拒绝");

    private final Integer value;
    private final String label;

    ReturnApplyStatus(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static boolean canTransit(Integer currentStatus, Integer targetStatus) {
        if (PENDING.value.equals(currentStatus)) {
            return RETURNING.value.equals(targetStatus) || REJECTED.value.equals(targetStatus);
        }
        if (RETURNING.value.equals(currentStatus)) {
            return COMPLETED.value.equals(targetStatus);
        }
        return false;
    }
}
