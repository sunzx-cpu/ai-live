package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yaozhi.live.modules.biz.entity.LiveInteractActionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 交互动作
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@Mapper
public interface LiveInteractActionDao extends BaseMapper<LiveInteractActionEntity> {

    /**
     * 分页查询
     * @param page 分页参数
     * @param params 查询参数
     * @return IPage<LiveInteractActionEntity>
     */
    IPage<LiveInteractActionEntity> queryPage(IPage<LiveInteractActionEntity> page, @Param("ew") Map<String, Object> params);

    /**
     * 查询列表
     * @param params 查询参数
     * @return List<LiveSegmentEntity>
     */
    List<LiveInteractActionEntity> queryList(@Param("ew") Map<String, Object> params);

}
