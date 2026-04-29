package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.common.utils.PageUtils;
import java.util.Map;

/**
 * License激活信息表Service
 * @author Administrator
 */
public interface LicenseActivationService extends IService<LicenseActivationEntity> {

    /**
     * 分页查询激活记录列表
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 创建激活记录
     * @param entity 激活记录实体
     * @return 是否成功
     */
    boolean saveActivation(LicenseActivationEntity entity);

    /**
     * 激活License
     * @param activationCode 激活码
     * @param userId 用户ID
     * @param deviceInfo 设备信息
     * @return 是否成功
     */
    boolean activateLicense(String activationCode, Long userId, String deviceInfo);

    /**
     * 根据激活码查询激活记录
     * @param activationCode 激活码
     * @return 激活记录实体
     */
    LicenseActivationEntity getByActivationCode(String activationCode);
}