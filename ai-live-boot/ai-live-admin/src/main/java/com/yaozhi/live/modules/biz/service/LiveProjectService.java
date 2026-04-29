package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.modules.biz.entity.LiveProjectEntity;

import java.util.List;
import java.util.Map;

/**
 * 直播项目
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
public interface LiveProjectService extends IService<LiveProjectEntity> {

    /**
     * 分页查询
     * @param params 查询参数
     * @return 查询结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询列表
     * @param params 查询参数
     * @return 查询结果
     */
    List<LiveProjectEntity> queryList(Map<String, Object> params);

}

