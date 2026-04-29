package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yaozhi.live.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;

import com.yaozhi.live.modules.biz.dao.LiveProjectDao;
import com.yaozhi.live.modules.biz.entity.LiveProjectEntity;
import com.yaozhi.live.modules.biz.service.LiveProjectService;


@Service("liveProjectService")
public class LiveProjectServiceImpl extends ServiceImpl<LiveProjectDao, LiveProjectEntity> implements LiveProjectService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String userId = (String)params.get("userId");
        String name = (String)params.get("name");
        IPage<LiveProjectEntity> page = this.page(new Query<LiveProjectEntity>().getPage(params),
                new LambdaQueryWrapper<LiveProjectEntity>()
                        .eq(StringUtils.isNotBlank(userId), LiveProjectEntity::getUserId, userId)
                        .like(StringUtils.isNotBlank(name), LiveProjectEntity::getName, name)
                        .orderByDesc(LiveProjectEntity::getId)
        );

        return new PageUtils(page);
    }

    @Override
    public List<LiveProjectEntity> queryList(Map<String, Object> params) {
        String userId = (String)params.get("userId");
        String name = (String)params.get("name");
        List<LiveProjectEntity> list = this.list(new LambdaQueryWrapper<LiveProjectEntity>()
                .eq(StringUtils.isNotBlank(userId), LiveProjectEntity::getUserId, userId)
                .like(StringUtils.isNotBlank(name), LiveProjectEntity::getName, name)
                .orderByDesc(LiveProjectEntity::getId)
        );

        return list;
    }
}
