package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;

import java.util.List;
import java.util.Map;

/**
 * AI配置服务
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
public interface AiConfigService extends IService<AiConfigEntity> {

    /**
     * 分页查询AI配置
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据项目ID获取默认AI配置（第一个配置）
     */
    AiConfigEntity getByProjectId(Long projectId);

    /**
     * 根据项目ID和提示词名称获取配置
     */
    AiConfigEntity getByProjectIdAndPromptName(Long projectId, String promptName);

    /**
     * 获取项目的所有AI配置列表
     */
    List<AiConfigEntity> getListByProjectId(Long projectId);

    /**
     * 获取项目的所有提示词名称列表
     */
    List<String> getPromptNamesByProjectId(Long projectId);

    /**
     * 测试AI配置连接
     */
    R testConnection(AiConfigEntity config);

    /**
     * 保存或更新AI配置
     */
    R saveOrUpdateConfig(AiConfigEntity config);

    /**
     * 复制配置到新的提示词名称
     */
    R copyConfig(Long projectId, String fromPromptName, String toPromptName);
} 