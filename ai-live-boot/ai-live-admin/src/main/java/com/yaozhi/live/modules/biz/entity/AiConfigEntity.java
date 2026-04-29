package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI配置实体类
 * 合并了供应商配置、提示词配置和互动配置
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
@TableName("tb_ai_config")
public class AiConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 项目ID
     */
    private Long projectId;

    // ============ AI供应商配置 ============
    /**
     * 供应商类型：openai,qianwen,ernie
     */
    private String providerType;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    // ============ 提示词配置 ============
    /**
     * 提示词名称
     */
    private String promptName;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户提示词
     */
    private String userPrompt;

    /**
     * 助手提示词
     */
    private String assistantPrompt;

    // ============ 自动话术配置 ============
    /**
     * 是否启用自动话术
     */
    private Boolean autoScriptEnabled;

    /**
     * 自动话术提示词
     */
    private String autoScriptPrompt;

    // ============ GPT介入互动配置 ============
    /**
     * 是否启用GPT介入
     */
    private Boolean gptInterventionEnabled;

    /**
     * 介入关键词，逗号分隔
     */
    private String interventionKeywords;

    // ============ GPT互动配置 ============
    /**
     * 是否启用GPT互动
     */
    private Boolean gptInteractionEnabled;

    /**
     * 互动提示词
     */
    private String interactionPrompt;

    // ============ 知识库配置 ============
    /**
     * 是否启用知识库
     */
    private Boolean knowledgeEnabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    // ============ 非数据库字段 ============
    /**
     * 项目名称
     */
    @TableField(exist = false)
    private String projectName;
} 