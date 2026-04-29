package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    /**
     * 商户id
     */
    private Long id;
    /**
     * 密码
     */
    private String password;
}
