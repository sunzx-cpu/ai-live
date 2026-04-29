package com.yaozhi.live.modules.appc.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.dto.LicenseActivationValidateDTO;
import com.yaozhi.live.modules.biz.dto.LicenseOrderDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicensePackageDetailDTO;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * License综合管理Controller
 * @author sunzx
 */
@RestController
@RequestMapping("/appc/license")
public class AppcLicenseController {

    @Autowired
    private LicenseService licenseService;

    /**
     * 获取套餐详情
     */
    @RequestMapping("/package/detail/{packageId}")
    public R getPackageDetail(@PathVariable("packageId") Long packageId){
        LicensePackageDetailDTO detail = licenseService.getPackageDetail(packageId);
        return R.ok().put("packageDetail", detail);
    }

    /**
     * 获取订单详情
     */
    @RequestMapping("/order/detail/{orderId}")
    public R getOrderDetail(@PathVariable("orderId") Long orderId){
        LicenseOrderDetailDTO detail = licenseService.getOrderDetail(orderId);
        return R.ok().put("orderDetail", detail);
    }

    /**
     * 验证激活码
     */
    @RequestMapping("/activation/validate")
    public R validateActivationCode(@RequestParam String activationCode){
        LicenseActivationValidateDTO validateResult = licenseService.validateActivationCode(activationCode);
        return R.ok().put("validateResult", validateResult);
    }

    /**
     * 获取用户所有有效订单
     */
    @RequestMapping("/user/orders")
    public R getUserActiveOrders(@RequestParam Long userId){
        List<LicenseOrderEntity> orders = licenseService.getUserActiveOrders(userId);
        return R.ok().put("orders", orders);
    }

    /**
     * 获取用户所有激活记录
     */
    @RequestMapping("/user/activations")
    public R getUserActivations(@RequestParam Long userId){
        List<LicenseActivationEntity> activations = licenseService.getUserActivations(userId);
        return R.ok().put("activations", activations);
    }
}