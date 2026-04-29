package com.yaozhi.live.modules.appc.controller;

import com.yaozhi.live.common.utils.*;
import com.yaozhi.live.common.validator.Assert;
import com.yaozhi.live.common.validator.ValidatorUtils;
import com.yaozhi.live.modules.appc.form.LoginForm;
import com.yaozhi.live.modules.appc.form.RegisterForm;
import com.yaozhi.live.modules.appc.form.SmsForm;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 直播端登录接口
 */
@RestController
@RequestMapping("/appc")
public class AppcLoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 账号密码登录
     * @param form
     * @param request
     * @return
     */
    @PostMapping("login")
    public R login(@RequestBody LoginForm form, HttpServletRequest request) {
        //表单校验
        ValidatorUtils.validateEntity(form);

        //用户登录
        form.setLastLoginIp(IPUtils.getIpAddr(request));
        long userId = userService.login(form);

        //生成token
        String token = jwtUtils.generateToken(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());

        return R.ok(map);
    }

    /**
     * 手机号登录
     * @param form
     * @param request
     * @return
     */
    @PostMapping("phoneLogin")
    public R phoneLogin(@RequestBody SmsForm form, HttpServletRequest request) {
        //表单校验
        ValidatorUtils.validateEntity(form);

        UserEntity user = userService.queryByMobile(form.getPhone());
        Assert.isNull(user, "手机号未注册");

        String smcCode = redisUtils.get("SMS_CODE_" + form.getPhone(), String.class);
        Assert.isBlank(smcCode, "验证码已过期或不存在");

        if (!form.getSmsCode().equals(smcCode)) {
            return R.error("验证码错误");
        }

        //生成token
        String token = jwtUtils.generateToken(user.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());

        return R.ok(map);
    }

    /**
     * 注册
     * @param form
     * @return
     */
    @PostMapping("register")
    public R register(@RequestBody RegisterForm form){
        //表单校验
        ValidatorUtils.validateEntity(form);

        UserEntity user = new UserEntity();
        user.setMobile(form.getMobile());
        user.setUsername(form.getMobile());
        user.setSalt(StringUtils.UUID());
        user.setPassword(DigestUtils.sha256Hex(form.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        userService.save(user);

        return R.ok();
    }
}
