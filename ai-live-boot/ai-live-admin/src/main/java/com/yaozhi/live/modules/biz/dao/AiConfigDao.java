package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI配置DAO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Mapper
public interface AiConfigDao extends BaseMapper<AiConfigEntity> {

    /**
     * 根据项目ID获取默认AI配置（第一个配置）
     */
    AiConfigEntity selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和提示词名称获取配置
     */
    AiConfigEntity selectByProjectIdAndPromptName(@Param("projectId") Long projectId, @Param("promptName") String promptName);

    /**
     * 获取项目的所有AI配置列表
     */
    List<AiConfigEntity> selectListByProjectId(@Param("projectId") Long projectId);

    /**
     * 获取项目的所有提示词名称列表
     */
    List<String> selectPromptNamesByProjectId(@Param("projectId") Long projectId);
} 