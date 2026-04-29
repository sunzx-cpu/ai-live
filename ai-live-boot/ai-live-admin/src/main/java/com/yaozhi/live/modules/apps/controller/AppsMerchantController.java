package com.yaozhi.live.modules.apps.controller;


import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户端信息接口
 */
@RestController
@RequestMapping("/apps/merchant")
public class AppsMerchantController {

    @Login
    @PostMapping("info")
    public R info (@LoginUser UserEntity user){
        return R.ok().put("merchant", user);
    }
}
