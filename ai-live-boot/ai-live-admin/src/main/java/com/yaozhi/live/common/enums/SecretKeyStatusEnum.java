package com.yaozhi.live.common.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.AllArgsConstructor;

/**
 * 密钥状态枚举
 */
@AllArgsConstructor
public enum SecretKeyStatusEnum implements IEnum<Integer> {

    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    ;

    private final int value;
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}