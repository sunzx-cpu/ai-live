package com.yaozhi.live.modules.appc.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LiveShieldEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveShieldService;
import com.yaozhi.live.modules.biz.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 直播端-屏蔽自己&敏感词接口
 */
@RestController
@RequestMapping("/appc/live-shield")
public class AppcLiveShieldController {
    @Autowired
    private LiveShieldService liveShieldService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 保存屏蔽自己&敏感词
     */
    @Login
    @PostMapping("/save")
    public R save(@LoginUser UserEntity user, @RequestBody LiveShieldEntity req) {
        LiveShieldEntity one = liveShieldService.getOne(Wrappers.<LiveShieldEntity>lambdaQuery()
                .eq(LiveShieldEntity::getMerchantId, merchantService.getMerchantId(user.getId())));
        if (one == null) {
            req.setMerchantId(merchantService.getMerchantId(user.getId()));
            req.setCreateTime(new Date());
            liveShieldService.save(req);
        } else {
            BeanUtil.copyProperties(req, one);
            liveShieldService.updateById(one);
        }
        return R.ok();
    }

    /**
     * 获取商户屏蔽自己&敏感词
     */
    @Login
    @PostMapping("/get-by-merchant")
    public R getByMerchantId(@LoginUser UserEntity user) {
        return R.ok().put("data", liveShieldService.getOne(Wrappers.<LiveShieldEntity>lambdaQuery()
                .eq(LiveShieldEntity::getMerchantId, merchantService.getMerchantId(user.getId())))
        );
    }
}
