package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.IPUtils;
import com.yaozhi.live.common.utils.JwtUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.common.validator.ValidatorUtils;
import com.yaozhi.live.modules.appc.form.LoginForm;
import com.yaozhi.live.modules.appc.form.RegisterForm;
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
 * 商户端登录接口
 */
@RestController
@RequestMapping("/apps")
public class AppsLoginController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录
     */
    @PostMapping("login")
    public R login(@RequestBody LoginForm form, HttpServletRequest request){
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
