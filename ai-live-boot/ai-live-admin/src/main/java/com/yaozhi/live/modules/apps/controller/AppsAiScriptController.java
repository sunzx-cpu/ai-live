package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.validator.ValidatorUtils;
import com.yaozhi.live.modules.biz.dto.GenerateScriptRequest;
import com.yaozhi.live.modules.biz.dto.KeywordReplyRequest;
import com.yaozhi.live.modules.biz.dto.KnowledgeQaRequest;
import com.yaozhi.live.modules.biz.service.AiScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * AI话术生成
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@RestController
@RequestMapping("/apps/ai-script")
public class AppsAiScriptController {

    @Autowired
    private AiScriptService aiScriptService;

    /**
     * 生成话术
     */
    @PostMapping("/generate")
    public R generateScript(@Valid @RequestBody GenerateScriptRequest request) {
        ValidatorUtils.validateEntity(request);

        String content = aiScriptService.generateScript(request);

        return R.ok().put("data", content);

    }

    /**
     * 关键词回复
     */
    @PostMapping("/keyword-reply")
    public R keywordReply(@Valid @RequestBody KeywordReplyRequest request) {
        ValidatorUtils.validateEntity(request);
        return aiScriptService.keywordReply(request);
    }

    /**
     * 知识库问答
     */
    @PostMapping("/knowledge-qa")
    public R knowledgeQa(@Valid @RequestBody KnowledgeQaRequest request) {
        ValidatorUtils.validateEntity(request);
        return aiScriptService.knowledgeQa(request);
    }

    /**
     * 构建完整提示词（用于预览）
     */
    @PostMapping("/build-prompt")
    public R buildPrompt(@RequestParam("projectId") Long projectId,
                        @RequestParam("userInput") String userInput,
                        @RequestParam(value = "context", required = false) String context) {
        try {
            String completePrompt = aiScriptService.buildCompletePrompt(projectId, userInput, context);
            return R.ok().put("prompt", completePrompt);
        } catch (Exception e) {
            return R.error("构建提示词失败：" + e.getMessage());
        }
    }

    /**
     * 测试AI连接
     */
    @GetMapping("/test-connection")
    public R testConnection(@RequestParam("projectId") Long projectId) {
        try {
            GenerateScriptRequest testRequest = new GenerateScriptRequest();
            testRequest.setProjectId(projectId);
            testRequest.setInput("测试");
            testRequest.setContext("这是一个连接测试");

            String text = aiScriptService.generateScript(testRequest);
            return R.ok("AI连接测试成功");
        } catch (Exception e) {
            return R.error("AI连接测试失败：" + e.getMessage());
        }
    }

    /**
     * 获取支持的话术类型
     */
    @GetMapping("/script-types")
    public R getScriptTypes() {
        return R.ok().put("scriptTypes", new String[]{
            "开播话术", "互动话术", "营销话术", "感谢话术", "下播话术", "商品介绍", "活动宣传", "回复话术"
        });
    }

    /**
     * 获取支持的语调风格
     */
    @GetMapping("/tones")
    public R getTones() {
        return R.ok().put("tones", new String[]{
            "亲切友好", "专业正式", "活泼幽默", "温暖贴心", "自信有力", "轻松随和", "热情洋溢", "简洁明了"
        });
    }


    /**
     * 智能改写话术
     */
    @PostMapping("/rewrite")
    public R rewriteScript(@RequestParam("projectId") Long projectId,
                          @RequestParam("originalScript") String originalScript,
                          @RequestParam(value = "rewriteStyle", required = false) String rewriteStyle,
                          @RequestParam(value = "targetLength", required = false) Integer targetLength) {
        try {
            GenerateScriptRequest request = new GenerateScriptRequest();
            request.setProjectId(projectId);
            request.setInput("请将以下话术进行改写：" + originalScript);
            request.setContext("改写要求：" + (rewriteStyle != null ? rewriteStyle : "保持原意，语言更流畅"));
            request.setMaxLength(targetLength);

            String text = aiScriptService.generateScript(request);

            return R.ok().put("data", text);
        } catch (Exception e) {
            return R.error("改写失败：" + e.getMessage());
        }
    }

    /**
     * 智能变量：将话术中的通用词汇替换为同义词组
     */
    @PostMapping("/add-smart-variables")
    public R addSmartVariables(@Valid @RequestBody GenerateScriptRequest request) {
        ValidatorUtils.validateEntity(request);

        try {
            String result = aiScriptService.addSmartVariables(request);
            return R.ok().put("data", result);
        } catch (Exception e) {
            return R.error("智能变量生成失败：" + e.getMessage());
        }
    }
}
