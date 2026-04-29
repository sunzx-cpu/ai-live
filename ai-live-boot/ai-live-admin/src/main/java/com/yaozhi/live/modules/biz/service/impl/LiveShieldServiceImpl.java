package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.modules.biz.dao.LiveShieldDao;
import com.yaozhi.live.modules.biz.entity.LiveShieldEntity;
import com.yaozhi.live.modules.biz.service.LiveShieldService;
import org.springframework.stereotype.Service;

/**
 * 屏蔽自己&敏感词
 */
@Service("liveShieldService")
public class LiveShieldServiceImpl extends ServiceImpl<LiveShieldDao, LiveShieldEntity> implements LiveShieldService {

}