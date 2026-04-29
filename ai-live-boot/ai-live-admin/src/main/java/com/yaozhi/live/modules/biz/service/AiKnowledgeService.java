package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.AiKnowledgeEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * AI知识库服务
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
public interface AiKnowledgeService extends IService<AiKnowledgeEntity> {

    /**
     * 分页查询知识库
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取项目的所有知识库记录
     */
    List<AiKnowledgeEntity> getListByProjectId(Long projectId);

    /**
     * 根据关键词搜索知识库记录
     */
    List<AiKnowledgeEntity> searchByKeyword(Long projectId, String keyword);

    /**
     * 根据问题内容搜索知识库记录
     */
    List<AiKnowledgeEntity> searchByQuestion(Long projectId, String question);

    /**
     * 导入Excel文件
     */
    R importExcel(Long projectId, Long userId, MultipartFile file);

    /**
     * 导出Excel文件
     */
    void exportExcel(Long projectId, HttpServletResponse response);
} 