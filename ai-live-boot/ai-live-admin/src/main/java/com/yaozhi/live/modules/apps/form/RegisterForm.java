package com.yaozhi.live.modules.apps.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 注册表单
 */
@Data
public class RegisterForm {
    /**
     * 手机号
     */
    @NotBlank(message="手机号不能为空")
    private String mobile;

    /**
     * 密码
     */
    @NotBlank(message="密码不能为空")
    private String password;

}
