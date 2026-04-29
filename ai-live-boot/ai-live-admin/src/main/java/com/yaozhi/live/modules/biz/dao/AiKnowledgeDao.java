package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yaozhi.live.modules.biz.entity.AiKnowledgeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI知识库DAO
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Mapper
public interface AiKnowledgeDao extends BaseMapper<AiKnowledgeEntity> {

    /**
     * 获取项目的所有知识库记录
     */
    List<AiKnowledgeEntity> selectListByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据关键词搜索知识库记录
     */
    List<AiKnowledgeEntity> selectByKeyword(@Param("projectId") Long projectId, @Param("keyword") String keyword);

    /**
     * 根据问题内容搜索知识库记录（全文检索）
     */
    List<AiKnowledgeEntity> selectByQuestion(@Param("projectId") Long projectId, @Param("question") String question);

    /**
     * 批量插入知识库记录
     */
    int insertBatch(@Param("list") List<AiKnowledgeEntity> list);
} 