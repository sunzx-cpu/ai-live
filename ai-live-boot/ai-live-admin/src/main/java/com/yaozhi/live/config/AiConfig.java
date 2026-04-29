package com.yaozhi.live.config;

import com.yaozhi.live.modules.biz.service.AiConfigService;
import com.yaozhi.live.modules.biz.service.impl.AiModelCacheManager;
import com.yaozhi.live.modules.biz.service.impl.DynamicAiServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Configuration
public class AiConfig {

    @Autowired
    private AiConfigService aiConfigService;

    /**
     * AI模型缓存管理器
     */
    @Bean
    public AiModelCacheManager aiModelCacheManager() {
        return new AiModelCacheManager();
    }

    /**
     * 动态AI服务工厂
     * 不再使用静态配置，而是根据数据库配置动态创建
     */
    @Bean
    public DynamicAiServiceFactory dynamicAiServiceFactory() {
        return new DynamicAiServiceFactory(aiConfigService, aiModelCacheManager());
    }
}
