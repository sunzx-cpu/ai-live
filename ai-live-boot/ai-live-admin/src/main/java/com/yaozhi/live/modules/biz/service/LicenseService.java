package com.yaozhi.live.modules.biz.service;

import com.yaozhi.live.modules.biz.dto.LicensePackageDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseOrderDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseActivationValidateDTO;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import java.util.List;

/**
 * License综合服务接口
 * @author Administrator
 */
public interface LicenseService {

    /**
     * 获取套餐详情（包含统计信息）
     * @param packageId 套餐ID
     * @return 套餐详情
     */
    LicensePackageDetailDTO getPackageDetail(Long packageId);

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    LicenseOrderDetailDTO getOrderDetail(Long orderId);

    /**
     * 验证激活码
     * @param activationCode 激活码
     * @return 验证结果
     */
    LicenseActivationValidateDTO validateActivationCode(String activationCode);

    /**
     * 获取用户所有有效的订单
     * @param userId 用户ID
     * @return 订单列表
     */
    List<LicenseOrderEntity> getUserActiveOrders(Long userId);

    /**
     * 获取用户所有激活记录
     * @param userId 用户ID
     * @return 激活记录列表
     */
    List<LicenseActivationEntity> getUserActivations(Long userId);
}