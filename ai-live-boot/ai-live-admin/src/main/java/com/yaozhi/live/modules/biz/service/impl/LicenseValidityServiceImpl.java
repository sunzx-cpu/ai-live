package com.yaozhi.live.modules.biz.service.impl;

import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.service.LicenseValidityService;
import org.springframework.stereotype.Service;

/**
 * License有效期管理Service实现类
 * @author Administrator
 */
@Service("licenseValidityService")
public class LicenseValidityServiceImpl implements LicenseValidityService {

    // 假设默认有效期为30天
    private static final int DEFAULT_VALIDITY_DAYS = 30;

    @Override
    public boolean isValid(LicenseActivationEntity activation) {
        if (activation == null) {
            return false;
        }
        
        // 检查是否已使用
        if (activation.getIsUsed() == null || activation.getIsUsed() != 1) {
            return false;
        }
        
        // 检查是否过期
        return !isExpired(activation);
    }

    @Override
    public int getRemainingDays(LicenseActivationEntity activation) {
        if (activation == null || activation.getActivateTime() == null) {
            return 0;
        }
        
        // 假设有效期为30天
        long validityMillis = DEFAULT_VALIDITY_DAYS * 24 * 60 * 60 * 1000L;
        long activationTimeMillis = activation.getActivateTime().getTime();
        long currentTimeMillis = System.currentTimeMillis();
        
        long diffMillis = (activationTimeMillis + validityMillis) - currentTimeMillis;
        
        if (diffMillis <= 0) {
            return 0;
        }
        
        return (int)(diffMillis / (24 * 60 * 60 * 1000L));
    }

    @Override
    public boolean isExpired(LicenseActivationEntity activation) {
        if (activation == null || activation.getActivateTime() == null) {
            return true;
        }
        
        // 假设有效期为30天
        long validityMillis = DEFAULT_VALIDITY_DAYS * 24 * 60 * 60 * 1000L;
        long activationTimeMillis = activation.getActivateTime().getTime();
        long currentTimeMillis = System.currentTimeMillis();
        
        return (activationTimeMillis + validityMillis) < currentTimeMillis;
    }

    @Override
    public void setLicenseValidity(LicenseActivationEntity activation, int validityDays) {
        // 此方法可以根据业务需要实现
        // 目前为占位符，实际实现可根据需要扩展
    }
}