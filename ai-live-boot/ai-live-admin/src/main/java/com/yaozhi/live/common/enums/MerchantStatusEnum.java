package com.yaozhi.live.common.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.AllArgsConstructor;

/**
 * 商户状态枚举
 */
@AllArgsConstructor
public enum MerchantStatusEnum implements IEnum<Integer> {

    INACTIVE(0, "未激活"),
    ACTIVATE(1, "激活"),
    EXPIRE(2, "到期"),
    DISABLE(3, "禁用"),
    ;

    private final int value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}