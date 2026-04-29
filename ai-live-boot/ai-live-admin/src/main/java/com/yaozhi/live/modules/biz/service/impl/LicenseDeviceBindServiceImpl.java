package com.yaozhi.live.modules.biz.service.impl;

import com.yaozhi.live.modules.biz.dao.LicenseActivationDao;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.service.LicenseDeviceBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * License设备绑定Service实现类
 * @author Administrator
 */
@Service("licenseDeviceBindService")
public class LicenseDeviceBindServiceImpl implements LicenseDeviceBindService {

    @Autowired
    private LicenseActivationDao licenseActivationDao;

    @Override
    public boolean bindDeviceToLicense(Long activationId, String deviceId, String deviceInfo) {
        try {
            LicenseActivationEntity activation = licenseActivationDao.selectById(activationId);
            if (activation == null) {
                return false;
            }
            
            // 设置设备信息
            activation.setDeviceInfo(deviceInfo);
            
            // 保存更新
            return licenseActivationDao.updateById(activation) > 0;
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }

    @Override
    public boolean unbindDeviceFromLicense(Long activationId) {
        try {
            LicenseActivationEntity activation = licenseActivationDao.selectById(activationId);
            if (activation == null) {
                return false;
            }
            
            // 清除设备信息
            activation.setDeviceInfo(null);
            
            // 保存更新
            return licenseActivationDao.updateById(activation) > 0;
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }

    @Override
    public boolean isDeviceBound(Long activationId) {
        LicenseActivationEntity activation = licenseActivationDao.selectById(activationId);
        if (activation == null) {
            return false;
        }
        
        return activation.getDeviceInfo() != null && !activation.getDeviceInfo().isEmpty();
    }

    @Override
    public String getBoundDeviceInfo(Long activationId) {
        LicenseActivationEntity activation = licenseActivationDao.selectById(activationId);
        if (activation == null) {
            return null;
        }
        
        return activation.getDeviceInfo();
    }
}