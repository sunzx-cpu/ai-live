package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.biz.entity.AiScriptLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI话术日志DAO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Mapper
public interface AiScriptLogDao extends BaseMapper<AiScriptLogEntity> {

    /**
     * 获取项目的所有话术日志记录
     */
    List<AiScriptLogEntity> selectListByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据话术类型获取日志记录
     */
    List<AiScriptLogEntity> selectByScriptType(@Param("projectId") Long projectId, @Param("scriptType") String scriptType);

    /**
     * 获取用户的话术生成统计
     */
    List<AiScriptLogEntity> selectStatsByUserId(@Param("userId") Long userId);
} 