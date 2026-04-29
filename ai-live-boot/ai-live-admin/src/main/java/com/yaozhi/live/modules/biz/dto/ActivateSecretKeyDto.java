package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

@Data
public class ActivateSecretKeyDto {
    /**
     * 商户id
     */
    private Long id;
    /**
     * 密钥
     */
    private String secretKey;
}
