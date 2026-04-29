package com.yaozhi.live.modules.biz.service.impl;

import com.yaozhi.live.common.exception.RRException;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.modules.biz.dao.AiScriptLogDao;
import com.yaozhi.live.modules.biz.dto.GenerateScriptRequest;
import com.yaozhi.live.modules.biz.dto.KeywordReplyRequest;
import com.yaozhi.live.modules.biz.dto.KnowledgeQaRequest;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import com.yaozhi.live.modules.biz.entity.AiKnowledgeEntity;
import com.yaozhi.live.modules.biz.entity.AiScriptLogEntity;
import com.yaozhi.live.modules.biz.service.AiConfigService;
import com.yaozhi.live.modules.biz.service.AiKnowledgeService;
import com.yaozhi.live.modules.biz.service.AiScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * AI话术生成服务实现
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Service("aiScriptService")
public class AiScriptServiceImpl implements AiScriptService {

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AiKnowledgeService aiKnowledgeService;

    @Autowired
    private DynamicAiServiceFactory aiServiceFactory;

    @Autowired
    private AiScriptLogDao aiScriptLogDao;

    /**
     * 直播片段管理的默认系统提示词
     */
    private static final String DEFAULT_SEGMENT_SYSTEM_PROMPT =
        "你是一个专业的直播话术改写助手。你的核心任务是：\n" +
        "1. 严格基于用户提供的原始内容进行改写\n" +
        "2. 保留原文中所有的商品信息、价格、数量等关键要素\n" +
        "3. 不要添加原文中没有的商品、价格或功能\n" +
        "4. 保留原文中的变量格式，如 {大概|大约} 这样的格式\n" +
        "5. 不要出现任何语气词例如: 哦,呀,啦,呢,哟,咯,哈等\n\n" +
        "输出格式要求：\n" +
        "- 生成多个不同版本的话术\n" +
        "- 每个版本单独用一对[]符号括起来\n" +
        "- 每个版本之间空一行\n" +
        "- 不要添加版本号、序号或其他标记\n" +
        "- 直接输出话术内容\n\n" +
        "输出示例格式：\n" +
        "[第一个版本的完整话术内容]\n\n" +
        "[第二个版本的完整话术内容]\n\n" +
        "记住：你是在\"改写\"用户的内容，不是\"创作\"新内容！";

    /**
     * 智能变量的系统提示词
     */
    private static final String SMART_VARIABLES_PROMPT =
        "你是一个专业的直播话术智能变量助手。你的任务是将话术中的通用词汇替换为同义词组，格式为 {词1|词2|词3}。\n\n" +
        "核心规则：\n" +
        "1. 只替换以下类型的词汇：\n" +
        "   - 常见动词：如 进入/进来/来/到、看/观看、买/购买、欢迎等\n" +
        "   - 称呼词：如 姐妹们/家人们/宝宝们/朋友们、你们/大家等\n" +
        "   - 代词：如 我们/咱们、现在/目前、这个/这款等\n" +
        "   - 通用形容词：如 好的/不错的、便宜/实惠等\n" +
        "2. 必须保留的词汇（不要替换）：\n" +
        "   - 专有名词：地名、品牌名（如 四季青、杭州等）\n" +
        "   - 产品类型：女装、男装、鞋子等核心品类\n" +
        "   - 数字、价格、规格等关键信息\n" +
        "   - 业务专用词：批发、档口、直播、甩货等行业术语\n" +
        "3. 同义词要求：\n" +
        "   - 每个词提供 4-8 个同义词\n" +
        "   - 确保所有同义词在语境中都合理\n" +
        "   - 保持口语化、自然流畅\n" +
        "   - 符合直播带货的语言风格\n\n" +
        "输出格式：\n" +
        "- 直接输出处理后的完整话术，不要添加任何说明或解释\n" +
        "- 使用 {同义词1|同义词2|同义词3} 格式\n" +
        "- 保持原文的标点符号和换行\n\n" +
        "示例：\n" +
        "输入：欢迎刚进入直播间的姐妹们，咱们是在四季青做女装批发的\n" +
        "输出：欢迎{刚进入|刚进|刚来|新进|新来|新进入|刚刚进入}直播间的{姐妹们|姐姐们|家人们|宝宝们}，{咱们|我们}是在四季青做女装批发的";

    @Override
    public String generateScript(GenerateScriptRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            // 如果用户没有输入提示词，使用默认描述
            String userInput = request.getInput();
            if (userInput == null || userInput.trim().isEmpty()) {
                userInput = "请基于原始话术生成多段不同的表达方式";
            }

            // 构建完整提示词
            String completePrompt;

            // 对于直播片段类型，使用专用的默认系统提示词
            if ("segment".equals(request.getScriptType())) {
                StringBuilder prompt = new StringBuilder();

                // 使用直播片段专用的系统提示词
                prompt.append(DEFAULT_SEGMENT_SYSTEM_PROMPT).append("\n\n");

                // 添加原始话术（context）
                if (StringUtils.isNotBlank(request.getContext())) {
                    prompt.append("【原始话术】\n").append(request.getContext()).append("\n\n");
                }

                // 添加用户的额外需求（如果有）
                if (StringUtils.isNotBlank(userInput) && !"请基于原始话术生成多段不同的表达方式".equals(userInput)) {
                    prompt.append("【用户需求】\n").append(userInput).append("\n\n");
                }

                completePrompt = prompt.toString().trim();
            } else {
                // 其他类型使用数据库配置
                completePrompt = buildCompletePrompt(
                        request.getProjectId(),
                        userInput,
                        request.getContext()
                );
            }

            // 添加话术类型和语调信息
            if (StringUtils.isNotBlank(request.getScriptType())) {
                completePrompt += "\n话术类型：" + request.getScriptType();
            }
            if (StringUtils.isNotBlank(request.getTone())) {
                completePrompt += "\n语调风格：" + request.getTone();
            }
            if (request.getMaxLength() != null) {
                completePrompt += "\n字数限制：不超过" + request.getMaxLength() + "字";
            }

            // 调用AI服务生成话术
            String response = aiServiceFactory.generateResponse(
                    request.getProjectId(),
                    completePrompt,
                    request.getPromptName()
            );

            // 验证响应
            if (response == null || response.trim().isEmpty()) {
                throw new RRException("AI服务返回内容为空");
            }

            String text = response.trim();
            long endTime = System.currentTimeMillis();

            // 记录生成日志
            saveScriptLog(request.getProjectId(), "auto_script", request.getInput(),
                    text, request.getContext(), request.getPromptName(),
                    (int) (endTime - startTime), response, true, null);

            return text;
        } catch (IllegalArgumentException e) {
            // AI配置错误
            String errorMsg = "AI配置错误：" + (e.getMessage() != null ? e.getMessage() : "未知错误");

            saveScriptLog(request.getProjectId(), "auto_script", request.getInput(),
                    null, request.getContext(), request.getPromptName(),
                    0, null, false, errorMsg);

            throw new RRException(errorMsg, e);
        } catch (RRException e) {
            // 已经是自定义异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常
            String errorMsg = "AI生成失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());

            // 记录错误日志
            saveScriptLog(request.getProjectId(), "auto_script", request.getInput(),
                    null, request.getContext(), request.getPromptName(),
                    0, null, false, errorMsg);

            throw new RRException(errorMsg, e);
        }
    }

    @Override
    public R keywordReply(KeywordReplyRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            // 构建关键词回复的提示词
            String prompt = buildKeywordReplyPrompt(request);

            // 调用AI服务生成回复
            String response = aiServiceFactory.generateResponse(
                    request.getProjectId(),
                    prompt,
                    request.getPromptName()
            );

            String reply = response;
            long endTime = System.currentTimeMillis();

            // 记录生成日志
            saveScriptLog(request.getProjectId(), "keyword_reply", request.getUserMessage(),
                    reply, request.getContext(), request.getPromptName(),
                    (int) (endTime - startTime), response, true, null);

            return R.ok("回复生成成功").put("reply", reply);

        } catch (Exception e) {
            // 记录错误日志
            saveScriptLog(request.getProjectId(), "keyword_reply", request.getUserMessage(),
                    null, request.getContext(), request.getPromptName(),
                    0, null, false, e.getMessage());

            return R.error("回复生成失败：" + e.getMessage());
        }
    }

    @Override
    public R knowledgeQa(KnowledgeQaRequest request) {
        try {
            // 先从知识库搜索相关答案
            List<AiKnowledgeEntity> knowledgeList = aiKnowledgeService.searchByQuestion(
                    request.getProjectId(), request.getQuestion());

            if (!knowledgeList.isEmpty()) {
                // 如果找到匹配的知识库记录，直接返回
                AiKnowledgeEntity knowledge = knowledgeList.get(0);

                // 记录知识库问答日志
                saveScriptLog(request.getProjectId(), "knowledge_qa", request.getQuestion(),
                        knowledge.getAnswer(), request.getContext(), null,
                        0, null, true, null);

                return R.ok("知识库匹配成功").put("answer", knowledge.getAnswer())
                        .put("source", "knowledge_base");
            }

            // 如果知识库没有匹配，则使用AI生成回答
            long startTime = System.currentTimeMillis();

            String prompt = buildKnowledgeQaPrompt(request);

            String response = aiServiceFactory.generateResponse(
                    request.getProjectId(), prompt, null);

            String answer = response;
            long endTime = System.currentTimeMillis();

            // 记录AI生成日志
            saveScriptLog(request.getProjectId(), "knowledge_qa", request.getQuestion(),
                    answer, request.getContext(), null,
                    (int) (endTime - startTime), response, true, null);

            return R.ok("AI回答生成成功").put("answer", answer).put("source", "ai_generated");

        } catch (Exception e) {
            // 记录错误日志
            saveScriptLog(request.getProjectId(), "knowledge_qa", request.getQuestion(),
                    null, request.getContext(), null,
                    0, null, false, e.getMessage());

            return R.error("问答失败：" + e.getMessage());
        }
    }

    @Override
    public String buildCompletePrompt(Long projectId, String userInput, String context) {
        // 获取AI配置
        AiConfigEntity config = aiConfigService.getByProjectId(projectId);

        if (config == null) {
            throw new RuntimeException("未找到AI配置");
        }

        StringBuilder prompt = new StringBuilder();

        // 添加系统提示词
        if (StringUtils.isNotBlank(config.getSystemPrompt())) {
            prompt.append(config.getSystemPrompt()).append("\n\n");
        }

        // 添加用户提示词（替换变量）
        if (StringUtils.isNotBlank(config.getUserPrompt())) {
            String userPrompt = config.getUserPrompt()
                    .replace("{input}", userInput != null ? userInput : "")
                    .replace("{context}", context != null ? context : "");
            prompt.append(userPrompt).append("\n\n");
        }

        // 添加助手提示词
        if (StringUtils.isNotBlank(config.getAssistantPrompt())) {
            prompt.append(config.getAssistantPrompt()).append("\n\n");
        }

        return prompt.toString().trim();
    }

    /**
     * 构建关键词回复提示词
     */
    private String buildKeywordReplyPrompt(KeywordReplyRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的直播助手，需要根据用户的消息和关键词生成合适的回复。\n");
        prompt.append("关键词：").append(request.getKeyword()).append("\n");
        prompt.append("用户消息：").append(request.getUserMessage()).append("\n");

        if (StringUtils.isNotBlank(request.getContext())) {
            prompt.append("上下文：").append(request.getContext()).append("\n");
        }

        if (StringUtils.isNotBlank(request.getChatHistory())) {
            prompt.append("聊天历史：").append(request.getChatHistory()).append("\n");
        }

        prompt.append("\n请生成一个专业、友好且相关的回复：");

        return prompt.toString();
    }

    /**
     * 构建知识库问答提示词
     */
    private String buildKnowledgeQaPrompt(KnowledgeQaRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的客服助手，需要根据用户的问题提供准确、有用的回答。\n");
        prompt.append("用户问题：").append(request.getQuestion()).append("\n");

        if (StringUtils.isNotBlank(request.getContext())) {
            prompt.append("上下文：").append(request.getContext()).append("\n");
        }

        prompt.append("\n请提供一个专业、准确且有帮助的回答：");

        return prompt.toString();
    }

    @Override
    public String addSmartVariables(GenerateScriptRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            // 验证输入
            if (request.getContext() == null || request.getContext().trim().isEmpty()) {
                throw new RRException("原始话术不能为空");
            }

            // 构建提示词
            StringBuilder prompt = new StringBuilder();
            prompt.append(SMART_VARIABLES_PROMPT).append("\n\n");
            prompt.append("【原始话术】\n").append(request.getContext());

            // 如果用户提供了额外要求
            if (StringUtils.isNotBlank(request.getInput())) {
                prompt.append("\n\n【用户要求】\n").append(request.getInput());
            }

            // 调用AI服务生成智能变量
            String response = aiServiceFactory.generateResponse(
                    request.getProjectId(),
                    prompt.toString(),
                    request.getPromptName()
            );

            // 验证响应
            if (response == null || response.trim().isEmpty()) {
                throw new RRException("AI服务返回内容为空");
            }

            String result = response.trim();
            long endTime = System.currentTimeMillis();

            // 记录生成日志
            saveScriptLog(request.getProjectId(), "smart_variables", request.getInput(),
                    result, request.getContext(), request.getPromptName(),
                    (int) (endTime - startTime), response, true, null);

            return result;
        } catch (IllegalArgumentException e) {
            String errorMsg = "AI配置错误：" + (e.getMessage() != null ? e.getMessage() : "未知错误");
            saveScriptLog(request.getProjectId(), "smart_variables", request.getInput(),
                    null, request.getContext(), request.getPromptName(),
                    0, null, false, errorMsg);
            throw new RRException(errorMsg, e);
        } catch (RRException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "智能变量生成失败：" + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            saveScriptLog(request.getProjectId(), "smart_variables", request.getInput(),
                    null, request.getContext(), request.getPromptName(),
                    0, null, false, errorMsg);
            throw new RRException(errorMsg, e);
        }
    }

    /**
     * 保存话术生成日志
     */
    private void saveScriptLog(Long projectId, String scriptType, String inputContent,
                              String outputContent, String contextInfo, String promptName,
                              int generationTime, String response, boolean status, String errorMessage) {
        try {
            AiScriptLogEntity log = new AiScriptLogEntity();
            // 这里需要从上下文获取用户ID，暂时设为null
            log.setUserId(null);  // TODO: 从上下文获取当前用户ID
            log.setProjectId(projectId);
            // 这里需要根据projectId和promptName获取配置ID，暂时设为null
            log.setConfigId(null);  // TODO: 获取配置ID
            log.setScriptType(scriptType);
            log.setInputContent(inputContent);
            log.setOutputContent(outputContent);
            log.setContextInfo(contextInfo);
            log.setPromptName(promptName);
            log.setGenerationTime(generationTime);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setCreateTime(new Date());

            // 对于langchain4j，response是字符串，可以记录响应长度等信息
            if (response != null) {
                // TODO: 根据需要记录token使用量和成本信息
                // log.setTokenUsage(response.length()); // 简单示例
            }

            aiScriptLogDao.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            System.err.println("保存话术日志失败：" + e.getMessage());
        }
    }
}
