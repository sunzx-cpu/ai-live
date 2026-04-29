package com.yaozhi.live.modules.biz.service;

/**
 * License设备绑定Service
 * @author Administrator
 */
public interface LicenseDeviceBindService {

    /**
     * 绑定设备到License
     * @param activationId 激活记录ID
     * @param deviceId 设备ID
     * @param deviceInfo 设备信息
     * @return 是否绑定成功
     */
    boolean bindDeviceToLicense(Long activationId, String deviceId, String deviceInfo);

    /**
     * 解绑设备
     * @param activationId 激活记录ID
     * @return 是否解绑成功
     */
    boolean unbindDeviceFromLicense(Long activationId);

    /**
     * 检查设备是否已绑定
     * @param activationId 激活记录ID
     * @return 是否已绑定
     */
    boolean isDeviceBound(Long activationId);

    /**
     * 获取绑定的设备信息
     * @param activationId 激活记录ID
     * @return 设备信息
     */
    String getBoundDeviceInfo(Long activationId);
}