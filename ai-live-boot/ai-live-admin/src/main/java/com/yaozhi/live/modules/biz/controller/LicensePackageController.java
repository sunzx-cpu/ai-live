package com.yaozhi.live.modules.biz.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LicensePackageEntity;
import com.yaozhi.live.modules.biz.service.LicensePackageService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * License套餐管理Controller
 * @author sunzx
 */
@RestController
@RequestMapping("/biz/license-package")
public class LicensePackageController {

    @Autowired
    private LicensePackageService licensePackageService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:licensepackage:list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok().put("page", licensePackageService.queryPage(params));
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        LicensePackageEntity licensePackage = licensePackageService.getById(id);
        return R.ok().put("licensePackage", licensePackage);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:licensepackage:save")
    public R save(@RequestBody LicensePackageEntity licensePackage){
        licensePackageService.savePackage(licensePackage);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody LicensePackageEntity licensePackage){
        licensePackageService.updatePackage(licensePackage);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:licensepackage:delete")
    public R delete(@RequestBody Long[] ids){
        licensePackageService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}