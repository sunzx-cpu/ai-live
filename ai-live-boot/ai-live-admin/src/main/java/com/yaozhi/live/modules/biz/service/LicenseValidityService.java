package com.yaozhi.live.modules.biz.service;

import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;

/**
 * License有效期管理Service
 * @author Administrator
 */
public interface LicenseValidityService {

    /**
     * 检查License是否有效
     * @param activation License激活记录
     * @return 是否有效
     */
    boolean isValid(LicenseActivationEntity activation);

    /**
     * 获取License剩余天数
     * @param activation License激活记录
     * @return 剩余天数
     */
    int getRemainingDays(LicenseActivationEntity activation);

    /**
     * 检查License是否过期
     * @param activation License激活记录
     * @return 是否过期
     */
    boolean isExpired(LicenseActivationEntity activation);

    /**
     * 设置License有效期（如需要）
     * @param activation License激活记录
     * @param validityDays 有效期天数
     */
    void setLicenseValidity(LicenseActivationEntity activation, int validityDays);
}