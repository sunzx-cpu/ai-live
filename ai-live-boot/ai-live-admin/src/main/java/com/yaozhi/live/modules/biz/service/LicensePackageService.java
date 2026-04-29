package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.modules.biz.entity.LicensePackageEntity;
import com.yaozhi.live.common.utils.PageUtils;
import java.util.Map;

/**
 * License套餐信息表Service
 * @author Administrator
 */
public interface LicensePackageService extends IService<LicensePackageEntity> {

    /**
     * 分页查询套餐列表
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 创建套餐
     * @param entity 套餐实体
     * @return 是否成功
     */
    boolean savePackage(LicensePackageEntity entity);

    /**
     * 更新套餐
     * @param entity 套餐实体
     * @return 是否成功
     */
    boolean updatePackage(LicensePackageEntity entity);

    /**
     * 删除套餐
     * @param id 套餐ID
     * @return 是否成功
     */
    boolean deletePackage(Long id);
}