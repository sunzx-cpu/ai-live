package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 知识库问答请求DTO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
public class KnowledgeQaRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 问题
     */
    private String question;

    /**
     * 上下文信息
     */
    private String context;
} 