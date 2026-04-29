package com.yaozhi.live.modules.appc.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LivePublicScreenConfigEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LivePublicScreenConfigService;
import com.yaozhi.live.modules.biz.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 直播间公屏配置
 *
 * @author
 * @email
 * @date 2025-11-04 18:34:34
 */
@RestController
@RequestMapping("/appc/live-public-screen-config")
public class AppcLivePublicScreenConfigController {
    @Autowired
    private LivePublicScreenConfigService livePublicScreenConfigService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 查询商户直播间公屏配置
     */
    @Login
    @PostMapping("/get-by-merchant")
    public R list(@LoginUser UserEntity user) {
        return R.ok().put("data", livePublicScreenConfigService.getOne(Wrappers.<LivePublicScreenConfigEntity>lambdaQuery()
                .eq(LivePublicScreenConfigEntity::getMerchantId, merchantService.getMerchantId(user.getId())))
        );
    }

    /**
     * 保存直播间公屏配置
     */
    @Login
    @PostMapping("/save")
    public R save(@LoginUser UserEntity user, @RequestBody LivePublicScreenConfigEntity req) {
        LivePublicScreenConfigEntity one = livePublicScreenConfigService.getOne(Wrappers.<LivePublicScreenConfigEntity>lambdaQuery()
                .eq(LivePublicScreenConfigEntity::getMerchantId, merchantService.getMerchantId(user.getId())));
        if (one == null) {
            req.setMerchantId(merchantService.getMerchantId(user.getId()));
            req.setCreateTime(new Date());
            livePublicScreenConfigService.save(req);
        } else {
            BeanUtil.copyProperties(req, one);
            livePublicScreenConfigService.updateById(one);
        }
        return R.ok();
    }
}
