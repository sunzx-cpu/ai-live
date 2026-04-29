package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.modules.biz.dao.LicenseActivationDao;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.service.LicenseActivationService;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * License激活信息表Service实现类
 * @author Administrator
 */
@Service("licenseActivationService")
public class LicenseActivationServiceImpl extends ServiceImpl<LicenseActivationDao, LicenseActivationEntity> implements LicenseActivationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String activationCode = (String)params.get("activationCode");
        String userId = (String)params.get("userId");

        QueryWrapper<LicenseActivationEntity> queryWrapper = new QueryWrapper<>();
        if (activationCode != null && !activationCode.isEmpty()) {
            queryWrapper.like("activation_code", activationCode);
        }
        if (userId != null && !userId.isEmpty()) {
            queryWrapper.eq("user_id", userId);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<LicenseActivationEntity> page = this.page(
                new Query<LicenseActivationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean saveActivation(LicenseActivationEntity entity) {
        return this.save(entity);
    }

    @Override
    public boolean activateLicense(String activationCode, Long userId, String deviceInfo) {
        // 验证激活码是否存在且未使用
        QueryWrapper<LicenseActivationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activation_code", activationCode);
        queryWrapper.eq("is_used", 0); // 未使用
        
        LicenseActivationEntity activation = this.getOne(queryWrapper);
        if (activation == null) {
            return false;
        }
        
        // 更新激活记录
        activation.setUserId(userId);
        activation.setDeviceInfo(deviceInfo);
        activation.setIsUsed(1);
        activation.setActivateTime(new java.util.Date());
        
        return this.updateById(activation);
    }

    @Override
    public LicenseActivationEntity getByActivationCode(String activationCode) {
        QueryWrapper<LicenseActivationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activation_code", activationCode);
        return this.getOne(queryWrapper);
    }
}