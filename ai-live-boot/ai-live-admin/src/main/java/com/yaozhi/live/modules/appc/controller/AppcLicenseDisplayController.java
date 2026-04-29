package com.yaozhi.live.modules.appc.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.dto.LicensePackageDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseOrderDetailDTO;
import com.yaozhi.live.modules.biz.dto.LicenseActivationValidateDTO;
import com.yaozhi.live.modules.biz.entity.LicenseActivationEntity;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.service.LicenseService;
import com.yaozhi.live.modules.biz.service.LicensePackageService;
import com.yaozhi.live.modules.biz.service.LicenseOrderService;
import com.yaozhi.live.modules.biz.service.LicenseActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * License展示Controller - 用于前端展示和用户交互
 * @author sunzx
 */
@RestController
@RequestMapping("/appc/license-display")
public class AppcLicenseDisplayController {

    @Autowired
    private LicensePackageService licensePackageService;
    
    @Autowired
    private LicenseOrderService licenseOrderService;
    
    @Autowired
    private LicenseActivationService licenseActivationService;
    
    @Autowired
    private LicenseService licenseService;

    /**
     * 获取所有可用的License套餐列表
     */
    @RequestMapping("/packages")
    public R getAvailablePackages(@RequestParam Map<String, Object> params){
        // 默认只返回启用状态的套餐
        params.put("status", "1");
        return R.ok().put("page", licensePackageService.queryPage(params));
    }

    /**
     * 获取套餐详情（包含统计信息）
     */
    @RequestMapping("/package/detail/{packageId}")
    public R getPackageDetail(@PathVariable("packageId") Long packageId){
        LicensePackageDetailDTO detail = licenseService.getPackageDetail(packageId);
        return R.ok().put("packageDetail", detail);
    }

    /**
     * 获取用户所有订单
     */
    @RequestMapping("/user/orders")
    public R getUserOrders(@RequestParam Long userId){
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

    /**
     * 验证激活码
     */
    @RequestMapping("/activation/validate")
    public R validateActivationCode(@RequestParam String activationCode){
        LicenseActivationValidateDTO validateResult = licenseService.validateActivationCode(activationCode);
        return R.ok().put("validateResult", validateResult);
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
     * 生成订单（模拟接口）
     */
    @RequestMapping("/generateOrder")
    public R generateOrder(@RequestParam Long packageId, 
                          @RequestParam Long userId,
                          @RequestParam String packageName,
                          @RequestParam java.math.BigDecimal amount){
        // 创建订单
        LicenseOrderEntity order = new LicenseOrderEntity();
        order.setPackageId(packageId);
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus(1); // 待支付
        order.setOrderNo("ORDER_" + System.currentTimeMillis()); // 简单生成订单号
        
        boolean result = licenseOrderService.saveOrder(order);
        if(result) {
            return R.ok("订单生成成功").put("orderId", order.getId());
        } else {
            return R.error("订单生成失败");
        }
    }
}