package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yaozhi.live.modules.biz.dao.LicenseActivationDao;
import com.yaozhi.live.modules.biz.dao.LicenseOrderDao;
import com.yaozhi.live.modules.biz.dao.LicensePackageDao;
import com.yaozhi.live.modules.biz.dto.LicensePackageDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseOrderDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseActivationValidateDTO;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.entity.LicensePackageEntity;
import com.yaozhi.live.modules.biz.service.LicenseActivationService;
import com.yaozhi.live.modules.biz.service.LicenseOrderService;
import com.yaozhi.live.modules.biz.service.LicensePackageService;
import com.yaozhi.live.modules.biz.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

/**
 * License综合服务实现类
 * @author Administrator
 */
@Service("licenseService")
public class LicenseServiceImpl implements LicenseService {

    @Autowired
    private LicensePackageService licensePackageService;
    
    @Autowired
    private LicenseOrderService licenseOrderService;
    
    @Autowired
    private LicenseActivationService licenseActivationService;
    
    @Autowired
    private LicensePackageDao licensePackageDao;
    
    @Autowired
    private LicenseOrderDao licenseOrderDao;
    
    @Autowired
    private LicenseActivationDao licenseActivationDao;

    @Override
    public LicensePackageDetailDTO getPackageDetail(Long packageId) {
        LicensePackageDetailDTO detail = new LicensePackageDetailDTO();
        
        // 获取套餐基本信息
        LicensePackageEntity packageInfo = licensePackageService.getById(packageId);
        detail.setPackageInfo(packageInfo);
        
        // 统计激活数量
        QueryWrapper<LicenseActivationEntity> activationWrapper = new QueryWrapper<>();
        activationWrapper.eq("package_id", packageId);
        activationWrapper.eq("is_used", 1);
        long activatedCount = licenseActivationDao.selectCount(activationWrapper);
        detail.setActivatedCount((int)activatedCount);
        
        // 统计订单金额和数量
        QueryWrapper<LicenseOrderEntity> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("package_id", packageId);
        orderWrapper.eq("status", 2); // 已支付状态
        List<LicenseOrderEntity> orders = licenseOrderDao.selectList(orderWrapper);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for(LicenseOrderEntity order : orders) {
            totalAmount = totalAmount.add(order.getAmount());
        }
        detail.setTotalAmount(totalAmount);
        detail.setOrderCount(orders.size());
        
        // 获取最近激活时间
        if(activatedCount > 0) {
            QueryWrapper<LicenseActivationEntity> recentWrapper = new QueryWrapper<>();
            recentWrapper.eq("package_id", packageId);
            recentWrapper.eq("is_used", 1);
            recentWrapper.orderByDesc("activate_time");
            recentWrapper.last("LIMIT 1");
            LicenseActivationEntity latestActivation = licenseActivationDao.selectOne(recentWrapper);
            detail.setLastActivateTime(latestActivation.getActivateTime());
        }
        
        return detail;
    }

    @Override
    public LicenseOrderDetailDTO getOrderDetail(Long orderId) {
        LicenseOrderDetailDTO detail = new LicenseOrderDetailDTO();
        
        // 获取订单信息
        LicenseOrderEntity orderInfo = licenseOrderService.getById(orderId);
        detail.setOrderInfo(orderInfo);
        
        // 获取套餐信息
        LicensePackageEntity packageInfo = licensePackageService.getById(orderInfo.getPackageId());
        detail.setPackageName(packageInfo.getPackageName());
        detail.setPrice(packageInfo.getPrice());
        
        return detail;
    }

    @Override
    public LicenseActivationValidateDTO validateActivationCode(String activationCode) {
        LicenseActivationValidateDTO validateResult = new LicenseActivationValidateDTO();
        validateResult.setActivationCode(activationCode);
        
        LicenseActivationEntity activation = licenseActivationService.getByActivationCode(activationCode);
        if(activation == null) {
            validateResult.setValid(false);
            return validateResult;
        }
        
        validateResult.setValid(true);
        validateResult.setUsed(activation.getIsUsed() == 1);
        
        // 获取套餐信息
        LicensePackageEntity packageInfo = licensePackageService.getById(activation.getPackageId());
        if(packageInfo != null) {
            validateResult.setPackageName(packageInfo.getPackageName());
            validateResult.setPrice(packageInfo.getPrice());
        }
        
        return validateResult;
    }

    @Override
    public List<LicenseOrderEntity> getUserActiveOrders(Long userId) {
        QueryWrapper<LicenseOrderEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.in("status", 1, 2); // 待支付或已支付状态
        wrapper.orderByDesc("create_time");
        return licenseOrderDao.selectList(wrapper);
    }

    @Override
    public List<LicenseActivationEntity> getUserActivations(Long userId) {
        QueryWrapper<LicenseActivationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return licenseActivationDao.selectList(wrapper);
    }
}