package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yaozhi.live.modules.biz.entity.LiveInteractKeywordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交互关键词
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@Mapper
public interface LiveInteractKeywordDao extends BaseMapper<LiveInteractKeywordEntity> {

    /**
     * 分页查询
     * @param page 分页参数
     * @param params 查询参数
     * @return IPage<LiveInteractKeywordEntity>
     */
    IPage<LiveInteractKeywordEntity> queryPage(IPage<LiveInteractKeywordEntity> page, @Param("ew") Map<String, Object> params);

    /**
     * 查询列表
     * @param params 查询参数
     * @return List<LiveInteractKeywordEntity>
     */
    List<LiveInteractKeywordEntity> queryList(@Param("ew") Map<String, Object> params);

}
