package com.yaozhi.live.modules.biz.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yaozhi.live.common.annotation.SysLog;
import com.yaozhi.live.common.enums.MerchantStatusEnum;
import com.yaozhi.live.common.enums.SecretKeyStatusEnum;
import com.yaozhi.live.common.exception.RRException;
import com.yaozhi.live.common.utils.IPUtils;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.validator.Assert;
import com.yaozhi.live.modules.biz.dto.ActivateSecretKeyDto;
import com.yaozhi.live.modules.biz.dto.ResetPasswordDto;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.entity.SecretKeyEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.MerchantService;
import com.yaozhi.live.modules.biz.service.SecretKeyService;
import com.yaozhi.live.modules.biz.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 商户
 *
 * @author
 * @email
 * @date 2025-09-30 13:56:50
 */
@RestController
@RequestMapping("biz/merchant")
public class MerchantController {
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private UserService userService;
    @Autowired
    private SecretKeyService secretKeyService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:merchant:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = merchantService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:merchant:info")
    public R info(@PathVariable("id") Long id) {
        MerchantEntity merchant = merchantService.getById(id);

        return R.ok().put("merchant", merchant);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:merchant:save")
    @Transactional(rollbackFor = Exception.class)
    public R save(@RequestBody MerchantEntity merchant, HttpServletRequest request) {
        merchant.setStatus(MerchantStatusEnum.INACTIVE.getValue());
        merchant.setRegisterTime(new Date());
        merchant.setRegisterIp(IPUtils.getIpAddr(request));
        merchantService.save(merchant);

        // 保存账号信息
        UserEntity user = new UserEntity();
        user.setUsername(merchant.getNickname());
        user.setMobile(merchant.getMobile());
        user.setUsername(merchant.getUsername());
        user.setPassword(merchant.getPassword());
        user.setMerchantId(merchant.getId());
        userService.register(user);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:merchant:update")
    public R update(@RequestBody MerchantEntity merchant) {
        merchantService.updateById(merchant);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:merchant:delete")
    public R delete(@RequestBody Long[] ids) {
        merchantService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 禁用
     */
    @PostMapping("/disable")
    public R disable(@RequestBody Long id) {
        merchantService.update(Wrappers.<MerchantEntity>lambdaUpdate()
                .set(MerchantEntity::getStatus, MerchantStatusEnum.DISABLE.getValue())
                .eq(MerchantEntity::getId, id)
        );
        return R.ok();
    }

    /**
     * 启用
     */
    @PostMapping("/enable")
    public R enable(@RequestBody Long id) {
        merchantService.update(Wrappers.<MerchantEntity>lambdaUpdate()
                .set(MerchantEntity::getStatus, MerchantStatusEnum.ACTIVATE.getValue())
                .eq(MerchantEntity::getId, id)
        );
        return R.ok();
    }

    /**
     * 重置密码
     */
    @SysLog("重置密码")
    @PostMapping("/resetPassword")
    public R resetPassword(@RequestBody ResetPasswordDto form) {
        Assert.isBlank(form.getPassword(), "密码不为能空");

        MerchantEntity merchant = merchantService.getById(form.getId());
        Assert.isNull(merchant, "商户不存在");

        UserEntity user = userService.queryByMerchantId(form.getId());
        Assert.isNull(user, "用户不存在");

        //sha256加密
        String password = new Sha256Hash(form.getPassword(), user.getSalt()).toHex();
        //更新密码
        user.setPassword(password);
        userService.updateById(user);

        return R.ok();
    }

    /**
     * 激活卡密
     */
    @SysLog("激活卡密")
    @PostMapping("/activateSecretKey")
    public R activateSecretKey(@RequestBody ActivateSecretKeyDto form) {
        Assert.isBlank(form.getSecretKey(), "密钥不为能空");

        MerchantEntity merchant = merchantService.getById(form.getId());
        Assert.isNull(merchant, "商户不存在");

        SecretKeyEntity secretKey = secretKeyService.queryBySecretKey(form.getSecretKey());
        Assert.isNull(secretKey, "密钥不存在");
        if (secretKey.getExpirationTime().before(new Date())) {
            throw new RRException("密钥已被使用或到期");
        }

        // 更新卡密状态
        secretKey.setStatus(SecretKeyStatusEnum.USED.getValue());
        secretKeyService.updateById(secretKey);
        // 更新商户相关信息
        merchant.setActivateSecretKey(form.getSecretKey());
        merchant.setStatus(MerchantStatusEnum.ACTIVATE.getValue());
        merchant.setExpirationTime(secretKey.getExpirationTime());
        merchantService.updateById(merchant);

        return R.ok();
    }

}
