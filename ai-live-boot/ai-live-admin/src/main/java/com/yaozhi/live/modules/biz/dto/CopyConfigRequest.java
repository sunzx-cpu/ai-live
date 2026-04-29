package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 复制配置请求DTO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
public class CopyConfigRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 源提示词名称
     */
    private String fromPromptName;

    /**
     * 目标提示词名称
     */
    private String toPromptName;
} 