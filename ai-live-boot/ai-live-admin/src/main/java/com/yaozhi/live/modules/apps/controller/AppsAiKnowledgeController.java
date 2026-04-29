package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.common.annotation.SysLog;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.validator.ValidatorUtils;
import com.yaozhi.live.modules.biz.entity.AiKnowledgeEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.AiKnowledgeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * AI知识库管理
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@RestController
@RequestMapping("/apps/ai-knowledge")
public class AppsAiKnowledgeController {

    @Autowired
    private AiKnowledgeService aiKnowledgeService;

    /**
     * 知识库列表
     */
    @GetMapping("/list")
    @RequiresPermissions("biz:ai-knowledge:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = aiKnowledgeService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 根据项目ID获取知识库列表
     */
    @GetMapping("/list/{projectId}")
    @RequiresPermissions("biz:ai-knowledge:list")
    public R listByProjectId(@PathVariable("projectId") Long projectId) {
        List<AiKnowledgeEntity> list = aiKnowledgeService.getListByProjectId(projectId);
        return R.ok().put("list", list);
    }

    /**
     * 根据关键词搜索知识库
     */
    @GetMapping("/search/keyword")
    @RequiresPermissions("biz:ai-knowledge:list")
    public R searchByKeyword(@RequestParam("projectId") Long projectId,
                             @RequestParam("keyword") String keyword) {
        List<AiKnowledgeEntity> list = aiKnowledgeService.searchByKeyword(projectId, keyword);
        return R.ok().put("list", list);
    }

    /**
     * 根据问题搜索知识库
     */
    @GetMapping("/search/question")
    @RequiresPermissions("biz:ai-knowledge:list")
    public R searchByQuestion(@RequestParam("projectId") Long projectId,
                              @RequestParam("question") String question) {
        List<AiKnowledgeEntity> list = aiKnowledgeService.searchByQuestion(projectId, question);
        return R.ok().put("list", list);
    }

    /**
     * 知识库详情
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("biz:ai-knowledge:info")
    public R info(@PathVariable("id") Long id) {
        AiKnowledgeEntity aiKnowledge = aiKnowledgeService.getById(id);
        return R.ok().put("aiKnowledge", aiKnowledge);
    }

    /**
     * 保存知识库
     */
    @PostMapping("/save")
    @RequiresPermissions("biz:ai-knowledge:save")
    @SysLog("保存知识库")
    public R save(@LoginUser UserEntity user, @RequestBody AiKnowledgeEntity aiKnowledge) {
        ValidatorUtils.validateEntity(aiKnowledge);

        aiKnowledge.setUserId(user.getId());
        aiKnowledgeService.save(aiKnowledge);

        return R.ok();
    }

    /**
     * 修改知识库
     */
    @PostMapping("/update")
    @RequiresPermissions("biz:ai-knowledge:update")
    @SysLog("修改知识库")
    public R update(@RequestBody AiKnowledgeEntity aiKnowledge) {
        ValidatorUtils.validateEntity(aiKnowledge);

        aiKnowledgeService.updateById(aiKnowledge);

        return R.ok();
    }

    /**
     * 删除知识库
     */
    @PostMapping("/delete")
    @RequiresPermissions("biz:ai-knowledge:delete")
    @SysLog("删除知识库")
    public R delete(@RequestBody Long[] ids) {
        aiKnowledgeService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 导入Excel
     */
    @PostMapping("/import")
    @RequiresPermissions("biz:ai-knowledge:import")
    @SysLog("导入知识库")
    public R importExcel(@LoginUser UserEntity user,
                         @RequestParam("projectId") Long projectId,
                         @RequestParam("file") MultipartFile file) {
        return aiKnowledgeService.importExcel(projectId, user.getId(), file);
    }

    /**
     * 导出Excel
     */
    @GetMapping("/export")
    @RequiresPermissions("biz:ai-knowledge:export")
    @SysLog("导出知识库")
    public void exportExcel(@RequestParam("projectId") Long projectId,
                            HttpServletResponse response) {
        aiKnowledgeService.exportExcel(projectId, response);
    }

    /**
     * 批量保存知识库
     */
    @PostMapping("/batch-save")
    @RequiresPermissions("biz:ai-knowledge:save")
    @SysLog("批量保存知识库")
    public R batchSave(@LoginUser UserEntity user, @RequestBody List<AiKnowledgeEntity> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return R.error("知识库列表不能为空");
        }

        Long userId = user.getId();
        for (AiKnowledgeEntity knowledge : knowledgeList) {
            ValidatorUtils.validateEntity(knowledge);
            knowledge.setUserId(userId);
        }

        aiKnowledgeService.saveBatch(knowledgeList);
        return R.ok();
    }

    /**
     * 批量更新知识库
     */
    @PostMapping("/batch-update")
    @RequiresPermissions("biz:ai-knowledge:update")
    @SysLog("批量更新知识库")
    public R batchUpdate(@RequestBody List<AiKnowledgeEntity> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return R.error("知识库列表不能为空");
        }

        for (AiKnowledgeEntity knowledge : knowledgeList) {
            ValidatorUtils.validateEntity(knowledge);
        }

        aiKnowledgeService.updateBatchById(knowledgeList);
        return R.ok();
    }

    /**
     * 批量删除知识库
     */
    @PostMapping("/batch-delete")
    @RequiresPermissions("biz:ai-knowledge:delete")
    @SysLog("批量删除知识库")
    public R batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return R.error("ID列表不能为空");
        }

        aiKnowledgeService.removeByIds(ids);
        return R.ok();
    }

    /**
     * 获取知识库统计信息
     */
    @GetMapping("/stats/{projectId}")
    @RequiresPermissions("biz:ai-knowledge:list")
    public R getStats(@PathVariable("projectId") Long projectId) {
        List<AiKnowledgeEntity> allKnowledge = aiKnowledgeService.getListByProjectId(projectId);

        int totalCount = allKnowledge.size();
        int avgKeywordCount = (int) allKnowledge.stream()
                .mapToInt(k -> k.getKeywords() != null ? k.getKeywords().split(",").length : 0)
                .average().orElse(0);

        return R.ok()
                .put("totalCount", totalCount)
                .put("avgKeywordCount", avgKeywordCount);
    }
}
