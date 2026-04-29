package com.yaozhi.live.modules.biz.service.impl;

import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.service.LicenseOrderService;
import com.yaozhi.live.modules.biz.service.PaymentCallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付回调处理Service实现类
 * @author Administrator
 */
@Service("paymentCallbackService")
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    @Autowired
    private LicenseOrderService licenseOrderService;

    @Override
    public boolean handlePaymentSuccess(String orderNo, java.math.BigDecimal payAmount, String payChannel, java.util.Date payTime) {
        try {
            // 根据订单号获取订单
            LicenseOrderEntity order = licenseOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return false;
            }
            
            // 更新订单状态为已支付
            order.setStatus(2); // 已支付
            order.setAmount(payAmount);
            order.setPayChannel(payChannel);
            order.setPayTime(payTime);
            
            // 保存更新后的订单
            return licenseOrderService.updateById(order);
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }

    @Override
    public boolean handlePaymentFailure(String orderNo) {
        try {
            // 根据订单号获取订单
            LicenseOrderEntity order = licenseOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return false;
            }
            
            // 更新订单状态为已取消
            order.setStatus(3); // 已取消
            
            // 保存更新后的订单
            return licenseOrderService.updateById(order);
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }

    @Override
    public boolean handleOrderTimeout(String orderNo) {
        try {
            // 根据订单号获取订单
            LicenseOrderEntity order = licenseOrderService.getByOrderNo(orderNo);
            if (order == null) {
                return false;
            }
            
            // 更新订单状态为已取消
            order.setStatus(3); // 已取消
            
            // 保存更新后的订单
            return licenseOrderService.updateById(order);
        } catch (Exception e) {
            // 记录日志
            return false;
        }
    }
}