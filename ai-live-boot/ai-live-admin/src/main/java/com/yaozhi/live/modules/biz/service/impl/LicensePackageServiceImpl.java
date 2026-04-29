package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.modules.biz.dao.LicensePackageDao;
import com.yaozhi.live.modules.biz.entity.LicensePackageEntity;
import com.yaozhi.live.modules.biz.service.LicensePackageService;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * License套餐信息表Service实现类
 * @author Administrator
 */
@Service("licensePackageService")
public class LicensePackageServiceImpl extends ServiceImpl<LicensePackageDao, LicensePackageEntity> implements LicensePackageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String packageName = (String)params.get("packageName");
        String status = (String)params.get("status");

        QueryWrapper<LicensePackageEntity> queryWrapper = new QueryWrapper<>();
        if (packageName != null && !packageName.isEmpty()) {
            queryWrapper.like("package_name", packageName);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<LicensePackageEntity> page = this.page(
                new Query<LicensePackageEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean savePackage(LicensePackageEntity entity) {
        return this.save(entity);
    }

    @Override
    public boolean updatePackage(LicensePackageEntity entity) {
        return this.updateById(entity);
    }

    @Override
    public boolean deletePackage(Long id) {
        return this.removeById(id);
    }
}