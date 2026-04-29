package com.yaozhi.live.modules.biz.service.impl;

import dev.langchain4j.model.chat.ChatLanguageModel;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import com.yaozhi.live.modules.biz.service.AiConfigService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型缓存管理器
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Component
public class AiModelCacheManager {

    private final Map<String, CachedModel> modelCache = new ConcurrentHashMap<>();
    private final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟

    @Autowired
    private AiConfigService aiConfigService;

    /**
     * 缓存模型
     */
    public void cacheModel(String cacheKey, ChatLanguageModel model, AiConfigEntity config) {
        CachedModel cachedModel = new CachedModel(model, config, System.currentTimeMillis());
        modelCache.put(cacheKey, cachedModel);
    }

    /**
     * 获取缓存的模型
     */
    public ChatLanguageModel getCachedModel(String cacheKey) {
        CachedModel cached = modelCache.get(cacheKey);
        if (cached == null) {
            return null;
        }

        // 检查是否过期
        if (System.currentTimeMillis() - cached.getCacheTime() > CACHE_EXPIRE_TIME) {
            modelCache.remove(cacheKey);
            return null;
        }

        // 检查配置是否有变化（通过更新时间比较）
        Long projectId = extractProjectIdFromCacheKey(cacheKey);
        String promptName = extractPromptNameFromCacheKey(cacheKey);

        AiConfigEntity currentConfig;
        if (promptName != null) {
            currentConfig = aiConfigService.getByProjectIdAndPromptName(projectId, promptName);
        } else {
            currentConfig = aiConfigService.getByProjectId(projectId);
        }

        if (currentConfig == null ||
                !currentConfig.getUpdateTime().equals(cached.getConfig().getUpdateTime())) {
            modelCache.remove(cacheKey);
            return null;
        }

        return cached.getModel();
    }

    /**
     * 移除缓存
     */
    public void removeCachedModel(String cacheKey) {
        modelCache.remove(cacheKey);
    }

    /**
     * 根据项目ID移除所有相关缓存
     */
    public void removeCachedModelsByProjectId(Long projectId) {
        String prefix = projectId + "_";
        modelCache.keySet().removeIf(key -> key.equals(projectId.toString()) || key.startsWith(prefix));
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        modelCache.clear();
    }

    /**
     * 从缓存键中提取项目ID
     */
    private Long extractProjectIdFromCacheKey(String cacheKey) {
        String[] parts = cacheKey.split("_");
        return Long.parseLong(parts[0]);
    }

    /**
     * 从缓存键中提取提示词名称
     */
    private String extractPromptNameFromCacheKey(String cacheKey) {
        String[] parts = cacheKey.split("_", 2);
        return parts.length > 1 ? parts[1] : null;
    }

    /**
     * 缓存模型包装类
     */
    @Data
    @AllArgsConstructor
    private static class CachedModel {
        private ChatLanguageModel model;
        private AiConfigEntity config;
        private long cacheTime;
    }
}
