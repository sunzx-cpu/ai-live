package com.yaozhi.live.modules.appc.controller;

import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.RedisUtils;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.modules.appc.form.SmsForm;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.utils.SMSUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直播端用户接口
 */
@RestController
@RequestMapping("/appc/user")
@Slf4j
public class AppcUserController {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    public SMSUtils smsUtils;

    @Login
    @PostMapping("info")
    public R info(@LoginUser UserEntity user) {
        return R.ok().put("user", user);
    }

    @PostMapping("/sendSms")
    public R sendSms(@RequestBody SmsForm req) {
        if (StringUtils.isNotEmpty(redisUtils.get("SMS_CODE_" + req.getPhone()))) {
            throw new RuntimeException("不允许重复发送");
        }
        String code = RandomStringUtils.randomNumeric(4);
        log.info("发送的手机号：{}，验证码：{}", req.getPhone(), code);
        smsUtils.sendSms(req.getPhone(), code);
        redisUtils.set("SMS_CODE_:" + req.getPhone(), code, 60);
        return R.ok();
    }
}
