package com.yaozhi.live.modules.biz.service.impl;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import com.yaozhi.live.modules.biz.service.AiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 动态AI服务工厂
 * 根据数据库配置动态创建AI服务
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Component
public class DynamicAiServiceFactory {

    private final AiConfigService aiConfigService;
    private final AiModelCacheManager cacheManager;

    @Autowired
    public DynamicAiServiceFactory(AiConfigService aiConfigService, AiModelCacheManager cacheManager) {
        this.aiConfigService = aiConfigService;
        this.cacheManager = cacheManager;
    }

    /**
     * 根据项目ID获取ChatModel（使用默认提示词）
     */
    public ChatLanguageModel getChatModel(Long projectId) {
        return getChatModel(projectId, null);
    }

    /**
     * 根据项目ID和提示词名称获取ChatModel
     */
    public ChatLanguageModel getChatModel(Long projectId, String promptName) {
        // 生成缓存键
        String cacheKey = promptName != null ? projectId + "_" + promptName : projectId.toString();

        // 先从缓存获取
        ChatLanguageModel cachedModel = cacheManager.getCachedModel(cacheKey);
        if (cachedModel != null) {
            return cachedModel;
        }

        // 从数据库获取配置
        AiConfigEntity config;
        if (promptName != null) {
            config = aiConfigService.getByProjectIdAndPromptName(projectId, promptName);
        } else {
            config = aiConfigService.getByProjectId(projectId);
        }

        if (config == null || !config.getIsEnabled()) {
            throw new IllegalArgumentException("AI配置不存在或未启用: " + projectId + ", promptName: " + promptName);
        }

        // 根据配置创建ChatModel
        ChatLanguageModel chatModel = createChatModel(config);

        // 缓存模型
        cacheManager.cacheModel(cacheKey, chatModel, config);

        return chatModel;
    }

    /**
     * 根据配置创建ChatModel
     */
    private ChatLanguageModel createChatModel(AiConfigEntity config) {
        switch (config.getProviderType().toLowerCase()) {
            case "openai":
                // OpenAI实现
                return createOpenAiChatModel(config);
            case "qianwen":
                // 通义千问实现
                return createQianwenChatModel(config);
            case "ernie":
                // 文心一言实现
                return createErnieChatModel(config);
            case "ollama":
                // Ollama实现
                return createOllamaChatModel(config);
            default:
                throw new IllegalArgumentException("不支持的AI供应商: " + config.getProviderType());
        }
    }

    /**
     * 生成响应
     */
    public String generateResponse(Long projectId, String prompt) {
        return generateResponse(projectId, prompt, null);
    }

    /**
     * 生成响应（指定提示词）
     */
    public String generateResponse(Long projectId, String prompt, String promptName) {
        if (projectId == null) {
            throw new IllegalArgumentException("ProjectId不能为空");
        }
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt不能为空");
        }

        ChatLanguageModel chatModel = getChatModel(projectId, promptName);

        if (chatModel == null) {
            throw new IllegalStateException("ChatModel为null");
        }

        String response = chatModel.generate(prompt);

        if (response == null) {
            return "";
        }

        return response;
    }

    /**
     * 生成响应（带消息列表）
     */
    public Response<AiMessage> generateResponseWithMessages(Long projectId, List<ChatMessage> messages) {
        return generateResponseWithMessages(projectId, messages, null);
    }

    /**
     * 生成响应（带消息列表，指定提示词）
     */
    public Response<AiMessage> generateResponseWithMessages(Long projectId, List<ChatMessage> messages, String promptName) {
        ChatLanguageModel chatModel = getChatModel(projectId, promptName);
        return chatModel.generate(messages);
    }

    /**
     * 清除项目的模型缓存
     */
    public void clearCache(Long projectId) {
        cacheManager.removeCachedModelsByProjectId(projectId);
    }

    /**
     * 清除指定配置的缓存
     */
    public void clearCache(Long projectId, String promptName) {
        String cacheKey = promptName != null ? projectId + "_" + promptName : projectId.toString();
        cacheManager.removeCachedModel(cacheKey);
    }

    // 其他供应商的创建方法
    private ChatLanguageModel createOpenAiChatModel(AiConfigEntity config) {
        // 验证必需的配置
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("OpenAI配置错误：BaseURL不能为空");
        }
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("OpenAI配置错误：API Key不能为空");
        }
        if (config.getModelName() == null || config.getModelName().trim().isEmpty()) {
            throw new IllegalArgumentException("OpenAI配置错误：模型名称不能为空");
        }

        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl(config.getBaseUrl().trim())
                    .apiKey(config.getApiKey().trim())
                    .modelName(config.getModelName().trim())
                    .build();

            if (model == null) {
                throw new IllegalStateException("ChatModel创建失败：返回null");
            }

            return model;
        } catch (Exception e) {
            throw new IllegalStateException("创建OpenAI ChatModel失败: " + e.getMessage(), e);
        }
    }
    private ChatLanguageModel createQianwenChatModel(AiConfigEntity config) {
        // TODO: 实现通义千问
        throw new UnsupportedOperationException("通义千问暂未实现");
    }

    private ChatLanguageModel createErnieChatModel(AiConfigEntity config) {
        // TODO: 实现文心一言
        throw new UnsupportedOperationException("文心一言暂未实现");
    }

    private ChatLanguageModel createOllamaChatModel(AiConfigEntity config) {
        // TODO: 实现Ollama
        throw new UnsupportedOperationException("Ollama暂未实现");
    }
}
