-- AI互动功能数据库表结构
-- Created: 2024-01-23
-- Description: 包含AI配置、知识库和脚本日志三张表

-- 1. AI配置表 (合并了提示词配置)
CREATE TABLE tb_ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    
    -- AI供应商配置
    provider_type VARCHAR(50) NOT NULL COMMENT '供应商类型：openai,qianwen,ernie',
    base_url VARCHAR(255) COMMENT 'API基础URL',
    model_name VARCHAR(100) COMMENT '模型名称',
    api_key VARCHAR(255) COMMENT 'API密钥',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    
    -- 提示词配置
    prompt_name VARCHAR(100) NOT NULL COMMENT '提示词名称',
    system_prompt TEXT COMMENT '系统提示词',
    user_prompt TEXT COMMENT '用户提示词',
    assistant_prompt TEXT COMMENT '助手提示词',
    
    -- 自动话术配置
    auto_script_enabled TINYINT(1) DEFAULT 0 COMMENT '是否启用自动话术',
    auto_script_prompt TEXT COMMENT '自动话术提示词',
    
    -- GPT介入互动配置
    gpt_intervention_enabled TINYINT(1) DEFAULT 0 COMMENT '是否启用GPT介入',
    intervention_keywords TEXT COMMENT '介入关键词，逗号分隔',
    
    -- GPT互动配置
    gpt_interaction_enabled TINYINT(1) DEFAULT 0 COMMENT '是否启用GPT互动',
    interaction_prompt TEXT COMMENT '互动提示词',
    
    -- 知识库配置
    knowledge_enabled TINYINT(1) DEFAULT 0 COMMENT '是否启用知识库',
    
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_project (user_id, project_id),
    UNIQUE KEY uk_user_project_prompt (user_id, project_id, prompt_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- 2. 知识库表 (简化设计)
CREATE TABLE tb_ai_knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    question TEXT NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    keywords VARCHAR(500) COMMENT '关键词，逗号分隔',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_project (user_id, project_id),
    INDEX idx_keywords (keywords),
    FULLTEXT idx_question_answer (question, answer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 3. 话术生成记录表
CREATE TABLE tb_ai_script_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    script_type VARCHAR(50) NOT NULL COMMENT '话术类型：auto_script,keyword_reply,knowledge_qa,batch_generate',
    input_content TEXT COMMENT '输入内容',
    output_content TEXT COMMENT '输出内容',
    context_info TEXT COMMENT '上下文信息',
    prompt_name VARCHAR(100) COMMENT '使用的提示词名称',
    generation_time INT COMMENT '生成耗时(毫秒)',
    token_usage INT COMMENT 'Token使用量',
    cost DECIMAL(10, 4) COMMENT '成本',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：1成功，0失败',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_project (user_id, project_id),
    INDEX idx_config_id (config_id),
    INDEX idx_script_type (script_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话术生成记录表';

-- 初始化数据
INSERT INTO tb_ai_config (user_id, project_id, provider_type, base_url, model_name, api_key, is_enabled, prompt_name, system_prompt, user_prompt, assistant_prompt, auto_script_enabled, gpt_interaction_enabled, knowledge_enabled) VALUES
(1, 1, 'openai', 'https://api.openai.com/v1', 'gpt-3.5-turbo', 'your-api-key-here', 1, '默认提示词', '你是一个专业的直播助手，能够帮助主播生成有趣、专业的直播话术。', '请根据以下需求生成直播话术：{input}', '我将为您生成专业的直播话术。', 1, 1, 1);

-- 创建知识库示例数据
INSERT INTO tb_ai_knowledge (user_id, project_id, question, answer, keywords, sort_order) VALUES
(1, 1, '这个产品的保修期是多长？', '我们的产品提供1年免费保修服务，保修期内免费维修或更换。', '保修,保修期,维修', 1),
(1, 1, '产品的价格是多少？', '我们的产品价格非常优惠，现在有特别优惠活动，具体价格请咨询客服。', '价格,优惠,活动', 2),
(1, 1, '如何使用这个产品？', '产品使用非常简单，我们提供详细的使用说明书和视频教程。', '使用,说明书,教程', 3); 