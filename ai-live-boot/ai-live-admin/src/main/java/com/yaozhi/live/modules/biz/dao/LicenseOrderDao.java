package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * license订单信息表Mapper
 * @author Administrator
 */
@Mapper
public interface LicenseOrderDao extends BaseMapper<LicenseOrderEntity> {
    
}