package com.yaozhi.live.modules.biz.service;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.dto.GenerateScriptRequest;
import com.yaozhi.live.modules.biz.dto.KeywordReplyRequest;
import com.yaozhi.live.modules.biz.dto.KnowledgeQaRequest;

/**
 * AI话术生成服务
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
public interface AiScriptService {

    /**
     * 生成话术
     */
    String generateScript(GenerateScriptRequest request);

    /**
     * 关键词回复
     */
    R keywordReply(KeywordReplyRequest request);

    /**
     * 知识库问答
     */
    R knowledgeQa(KnowledgeQaRequest request);


    /**
     * 构建完整的提示词
     */
    String buildCompletePrompt(Long projectId, String userInput, String context);

    /**
     * 智能变量：将话术中的通用词汇替换为同义词组
     */
    String addSmartVariables(GenerateScriptRequest request);
}
