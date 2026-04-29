package com.yaozhi.live.modules.biz.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.service.LicenseOrderService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * License订单管理Controller
 * @author sunzx
 */
@RestController
@RequestMapping("/biz/license-order")
public class LicenseOrderController {

    @Autowired
    private LicenseOrderService licenseOrderService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:licenseorder:list")
    public R list(@RequestParam Map<String, Object> params){
        return R.ok().put("page", licenseOrderService.queryPage(params));
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        LicenseOrderEntity licenseOrder = licenseOrderService.getById(id);
        return R.ok().put("licenseOrder", licenseOrder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:licenseorder:save")
    public R save(@RequestBody LicenseOrderEntity licenseOrder){
        licenseOrderService.saveOrder(licenseOrder);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody LicenseOrderEntity licenseOrder){
        licenseOrderService.updateById(licenseOrder);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:licenseorder:delete")
    public R delete(@RequestBody Long[] ids){
        licenseOrderService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 更新订单状态
     */
    @RequestMapping("/updateStatus")
    public R updateStatus(@RequestParam Long orderId, @RequestParam Integer status){
        licenseOrderService.updateOrderStatus(orderId, status);
        return R.ok();
    }
}