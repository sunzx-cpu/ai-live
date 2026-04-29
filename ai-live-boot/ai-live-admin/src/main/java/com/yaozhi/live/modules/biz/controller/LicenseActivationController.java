package com.yaozhi.live.modules.biz.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.service.LicenseActivationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * License激活管理Controller
 * @author sunzx
 */
@RestController
@RequestMapping("/biz/license-activation")
public class LicenseActivationController {

    @Autowired
    private LicenseActivationService licenseActivationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:licenseactivation:list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok().put("page", licenseActivationService.queryPage(params));
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        LicenseActivationEntity licenseActivation = licenseActivationService.getById(id);
        return R.ok().put("licenseActivation", licenseActivation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:licenseactivation:save")
    public R save(@RequestBody LicenseActivationEntity licenseActivation){
        licenseActivationService.saveActivation(licenseActivation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody LicenseActivationEntity licenseActivation){
        licenseActivationService.updateById(licenseActivation);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:licenseactivation:delete")
    public R delete(@RequestBody Long[] ids){
        licenseActivationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 激活License
     */
    @RequestMapping("/activate")
    public R activate(@RequestParam String activationCode, 
                     @RequestParam Long userId, 
                     @RequestParam String deviceInfo){
        boolean result = licenseActivationService.activateLicense(activationCode, userId, deviceInfo);
        if(result) {
            return R.ok("激活成功");
        } else {
            return R.error("激活失败，激活码无效或已被使用");
        }
    }
}