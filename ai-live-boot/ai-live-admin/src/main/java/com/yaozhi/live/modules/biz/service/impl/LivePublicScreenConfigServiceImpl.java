package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.modules.biz.dao.LivePublicScreenConfigDao;
import com.yaozhi.live.modules.biz.entity.LivePublicScreenConfigEntity;
import com.yaozhi.live.modules.biz.service.LivePublicScreenConfigService;
import org.springframework.stereotype.Service;

@Service("livePublicScreenConfigService")
public class LivePublicScreenConfigServiceImpl extends ServiceImpl<LivePublicScreenConfigDao, LivePublicScreenConfigEntity> implements LivePublicScreenConfigService {

}