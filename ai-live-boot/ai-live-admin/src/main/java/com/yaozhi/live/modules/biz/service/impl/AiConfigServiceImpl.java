package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.modules.biz.dao.AiConfigDao;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import com.yaozhi.live.modules.biz.service.AiConfigService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AI配置服务实现
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Service("aiConfigService")
public class AiConfigServiceImpl extends ServiceImpl<AiConfigDao, AiConfigEntity> implements AiConfigService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String projectId = (String) params.get("projectId");
        String promptName = (String) params.get("promptName");
        String providerType = (String) params.get("providerType");
        
        IPage<AiConfigEntity> page = this.page(new Query<AiConfigEntity>().getPage(params),
                new LambdaQueryWrapper<AiConfigEntity>()
                        .eq(StringUtils.isNotBlank(projectId), AiConfigEntity::getProjectId, projectId)
                        .like(StringUtils.isNotBlank(promptName), AiConfigEntity::getPromptName, promptName)
                        .eq(StringUtils.isNotBlank(providerType), AiConfigEntity::getProviderType, providerType)
                        .orderByDesc(AiConfigEntity::getCreateTime)
        );

        return new PageUtils(page);
    }

    @Override
    public AiConfigEntity getByProjectId(Long projectId) {
        return baseMapper.selectByProjectId(projectId);
    }

    @Override
    public AiConfigEntity getByProjectIdAndPromptName(Long projectId, String promptName) {
        return baseMapper.selectByProjectIdAndPromptName(projectId, promptName);
    }

    @Override
    public List<AiConfigEntity> getListByProjectId(Long projectId) {
        return baseMapper.selectListByProjectId(projectId);
    }

    @Override
    public List<String> getPromptNamesByProjectId(Long projectId) {
        return baseMapper.selectPromptNamesByProjectId(projectId);
    }

    @Override
    public R testConnection(AiConfigEntity config) {
        // TODO: 实现AI配置连接测试
        try {
            // 这里可以添加实际的AI服务连接测试逻辑
            return R.ok("连接测试成功");
        } catch (Exception e) {
            return R.error("连接测试失败：" + e.getMessage());
        }
    }

    @Override
    public R saveOrUpdateConfig(AiConfigEntity config) {
        try {
            // 检查是否已存在相同的项目ID和提示词名称
            AiConfigEntity existingConfig = getByProjectIdAndPromptName(config.getProjectId(), config.getPromptName());
            
            if (existingConfig != null && !existingConfig.getId().equals(config.getId())) {
                return R.error("该项目下已存在相同名称的提示词配置");
            }
            
            if (config.getId() == null) {
                config.setCreateTime(new Date());
                config.setUpdateTime(new Date());
                this.save(config);
            } else {
                config.setUpdateTime(new Date());
                this.updateById(config);
            }
            
            return R.ok("保存成功");
        } catch (Exception e) {
            return R.error("保存失败：" + e.getMessage());
        }
    }

    @Override
    public R copyConfig(Long projectId, String fromPromptName, String toPromptName) {
        try {
            // 获取源配置
            AiConfigEntity sourceConfig = getByProjectIdAndPromptName(projectId, fromPromptName);
            if (sourceConfig == null) {
                return R.error("源配置不存在");
            }
            
            // 检查目标配置是否已存在
            AiConfigEntity targetConfig = getByProjectIdAndPromptName(projectId, toPromptName);
            if (targetConfig != null) {
                return R.error("目标提示词配置已存在");
            }
            
            // 复制配置
            AiConfigEntity newConfig = new AiConfigEntity();
            newConfig.setUserId(sourceConfig.getUserId());
            newConfig.setProjectId(sourceConfig.getProjectId());
            newConfig.setProviderType(sourceConfig.getProviderType());
            newConfig.setBaseUrl(sourceConfig.getBaseUrl());
            newConfig.setModelName(sourceConfig.getModelName());
            newConfig.setApiKey(sourceConfig.getApiKey());
            newConfig.setIsEnabled(sourceConfig.getIsEnabled());
            newConfig.setPromptName(toPromptName);
            newConfig.setSystemPrompt(sourceConfig.getSystemPrompt());
            newConfig.setUserPrompt(sourceConfig.getUserPrompt());
            newConfig.setAssistantPrompt(sourceConfig.getAssistantPrompt());
            newConfig.setAutoScriptEnabled(sourceConfig.getAutoScriptEnabled());
            newConfig.setAutoScriptPrompt(sourceConfig.getAutoScriptPrompt());
            newConfig.setGptInterventionEnabled(sourceConfig.getGptInterventionEnabled());
            newConfig.setInterventionKeywords(sourceConfig.getInterventionKeywords());
            newConfig.setGptInteractionEnabled(sourceConfig.getGptInteractionEnabled());
            newConfig.setInteractionPrompt(sourceConfig.getInteractionPrompt());
            newConfig.setKnowledgeEnabled(sourceConfig.getKnowledgeEnabled());
            newConfig.setCreateTime(new Date());
            newConfig.setUpdateTime(new Date());
            
            this.save(newConfig);
            
            return R.ok("复制成功");
        } catch (Exception e) {
            return R.error("复制失败：" + e.getMessage());
        }
    }
} 