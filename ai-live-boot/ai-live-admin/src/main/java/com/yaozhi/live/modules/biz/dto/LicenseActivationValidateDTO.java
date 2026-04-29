package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

/**
 * License激活码验证DTO
 * @author Administrator
 */
@Data
public class LicenseActivationValidateDTO {
    
    /**
     * 激活码
     */
    private String activationCode;
    
    /**
     * 是否有效
     */
    private Boolean valid;
    
    /**
     * 是否已使用
     */
    private Boolean used;
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 套餐价格
     */
    private java.math.BigDecimal price;
}