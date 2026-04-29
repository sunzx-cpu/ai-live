package com.yaozhi.live.modules.biz.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.UserService;
import com.yaozhi.live.modules.biz.service.impl.MerchantServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;


/**
 * 用户
 */
@RestController
@RequestMapping("biz/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private MerchantServiceImpl merchantService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:user:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = userService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{userId}")
    @RequiresPermissions("biz:user:info")
    public R info(@PathVariable("userId") Long userId) {
        UserEntity user = userService.getById(userId);
        user.setPassword("");
        MerchantEntity merchant = merchantService.getById(user.getMerchantId());
        if (merchant != null) {
            user.setMerchantName(merchant.getNickname());
            user.setStatus(merchant.getStatus());
        }
        return R.ok().put("user", user);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:user:save")
    public R save(@RequestBody UserEntity user) {
        if (userService.queryByUsername(user.getUsername()) != null) {
            return R.error("用户名已存在");
        }

        user.setSalt(StringUtils.UUID());
        user.setPassword(DigestUtils.sha256Hex(user.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        userService.save(user);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:user:update")
    public R update(@RequestBody UserEntity user) {

        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(DigestUtils.sha256Hex(user.getPassword() + user.getSalt()));
        }

        userService.updateById(user);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:user:delete")
    public R delete(@RequestBody Long[] userIds) {
        userService.removeByIds(Arrays.asList(userIds));

        return R.ok();
    }

}
