package com.yaozhi.live.modules.biz.service;

/**
 * 支付回调处理Service
 * @author Administrator
 */
public interface PaymentCallbackService {

    /**
     * 处理支付成功回调
     * @param orderNo 订单编号
     * @param payAmount 支付金额
     * @param payChannel 支付渠道
     * @param payTime 支付时间
     * @return 是否处理成功
     */
    boolean handlePaymentSuccess(String orderNo, java.math.BigDecimal payAmount, String payChannel, java.util.Date payTime);

    /**
     * 处理支付失败回调
     * @param orderNo 订单编号
     * @return 是否处理成功
     */
    boolean handlePaymentFailure(String orderNo);

    /**
     * 处理订单超时回调
     * @param orderNo 订单编号
     * @return 是否处理成功
     */
    boolean handleOrderTimeout(String orderNo);
}