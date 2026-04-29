package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.biz.entity.LiveShieldEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 屏蔽自己&敏感词
 */
@Mapper
public interface LiveShieldDao extends BaseMapper<LiveShieldEntity> {
	
}
