package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.common.utils.PageUtils;
import java.util.Map;

/**
 * license订单信息表Service
 * @author Administrator
 */
public interface LicenseOrderService extends IService<LicenseOrderEntity> {

    /**
     * 分页查询订单列表
     * @param params 查询参数
     * @return 分页结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 创建订单
     * @param entity 订单实体
     * @return 是否成功
     */
    boolean saveOrder(LicenseOrderEntity entity);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 根据订单号查询订单
     * @param orderNo 订单编号
     * @return 订单实体
     */
    LicenseOrderEntity getByOrderNo(String orderNo);
}