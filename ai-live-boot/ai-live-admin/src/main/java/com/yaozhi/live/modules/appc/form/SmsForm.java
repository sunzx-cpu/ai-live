package com.yaozhi.live.modules.appc.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 短信发送表单
 */
@Data
public class SmsForm {
    /**
     * 手机号
     */
    @NotBlank(message="手机号不能为空")
    private String phone;
    /**
     * 短信验证码
     */
    private String smsCode;
}
