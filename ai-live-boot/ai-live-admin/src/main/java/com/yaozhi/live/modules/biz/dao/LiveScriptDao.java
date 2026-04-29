package com.yaozhi.live.modules.biz.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yaozhi.live.modules.biz.entity.LiveScriptEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 直播脚本
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@Mapper
public interface LiveScriptDao extends BaseMapper<LiveScriptEntity> {

    /**
     * 分页查询
     * @param page 分页参数
     * @param params 查询参数
     * @return List<LiveScriptEntity>
     */
    IPage<LiveScriptEntity> queryPage(IPage<LiveScriptEntity> page, @Param("ew") Map<String, Object> params);

    /**
     * 查询列表
     * @param params 查询参数
     * @return List<LiveScriptEntity>
     */
    List<LiveScriptEntity> queryList(@Param("ew") Map<String, Object> params);


}
