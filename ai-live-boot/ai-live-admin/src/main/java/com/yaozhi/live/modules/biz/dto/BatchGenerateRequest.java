package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量生成请求DTO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
public class BatchGenerateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 输入内容列表
     */
    private List<String> inputs;

    /**
     * 提示词名称，可选
     */
    private String promptName;

    /**
     * 上下文信息
     */
    private String context;

    /**
     * 话术类型：opening,interaction,closing
     */
    private String scriptType;

    /**
     * 最大长度
     */
    private Integer maxLength;

    /**
     * 语调：formal,casual,friendly
     */
    private String tone;
} 