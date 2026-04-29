package com.yaozhi.live.modules.biz.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 关键词回复请求DTO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
public class KeywordReplyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 上下文信息
     */
    private String context;

    /**
     * 用户消息
     */
    private String userMessage;

    /**
     * 聊天历史
     */
    private String chatHistory;

    /**
     * 提示词名称，可选
     */
    private String promptName;
} 