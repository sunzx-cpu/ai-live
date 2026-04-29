package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.dto.CopyConfigRequest;
import com.yaozhi.live.modules.biz.entity.AiConfigEntity;
import com.yaozhi.live.modules.biz.service.AiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI配置管理
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@RestController
@RequestMapping("/apps/ai-config")
public class AppsAiConfigController {

    @Autowired
    private AiConfigService aiConfigService;

    /**
     * 获取AI配置列表
     */
    @Login
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        PageUtils page = aiConfigService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 获取项目的默认AI配置
     */
    @Login
    @GetMapping("/info/{projectId}")
    public R info(@PathVariable Long projectId) {
        AiConfigEntity config = aiConfigService.getByProjectId(projectId);
        return R.ok().put("config", config);
    }

    /**
     * 根据提示词名称获取配置
     */
    @Login
    @GetMapping("/info/{projectId}/{promptName}")
    public R infoByPromptName(@PathVariable Long projectId, @PathVariable String promptName) {
        AiConfigEntity config = aiConfigService.getByProjectIdAndPromptName(projectId, promptName);
        return R.ok().put("config", config);
    }

    /**
     * 获取项目的所有配置
     */
    @Login
    @GetMapping("/all/{projectId}")
    public R all(@PathVariable Long projectId) {
        List<AiConfigEntity> list = aiConfigService.getListByProjectId(projectId);
        return R.ok().put("list", list);
    }

    /**
     * 获取项目的所有提示词名称
     */
    @Login
    @GetMapping("/prompt-names/{projectId}")
    public R promptNames(@PathVariable Long projectId) {
        List<String> promptNames = aiConfigService.getPromptNamesByProjectId(projectId);
        return R.ok().put("promptNames", promptNames);
    }

    /**
     * 保存AI配置
     */
    @Login
    @PostMapping("/save")
    public R save(@RequestBody AiConfigEntity config, @LoginUser UserEntity user) {
        config.setUserId(user.getId());
        return aiConfigService.saveOrUpdateConfig(config);
    }

    /**
     * 复制配置
     */
    @Login
    @PostMapping("/copy")
    public R copy(@RequestBody CopyConfigRequest request) {
        return aiConfigService.copyConfig(
                request.getProjectId(),
                request.getFromPromptName(),
                request.getToPromptName()
        );
    }

    /**
     * 删除配置
     */
    @Login
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        aiConfigService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 测试连接
     */
    @Login
    @PostMapping("/test")
    public R test(@RequestBody AiConfigEntity config) {
        return aiConfigService.testConnection(config);
    }
}
