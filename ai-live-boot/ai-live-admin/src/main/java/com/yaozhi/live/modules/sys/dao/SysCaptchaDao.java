package com.yaozhi.live.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.sys.entity.SysCaptchaEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码
 */
@Mapper
public interface SysCaptchaDao extends BaseMapper<SysCaptchaEntity> {

}
