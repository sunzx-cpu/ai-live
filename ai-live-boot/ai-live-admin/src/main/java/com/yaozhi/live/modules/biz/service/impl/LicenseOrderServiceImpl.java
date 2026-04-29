package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.modules.biz.dao.LicenseOrderDao;
import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import com.yaozhi.live.modules.biz.service.LicenseOrderService;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * license订单信息表Service实现类
 * @author Administrator
 */
@Service("licenseOrderService")
public class LicenseOrderServiceImpl extends ServiceImpl<LicenseOrderDao, LicenseOrderEntity> implements LicenseOrderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String orderNo = (String)params.get("orderNo");
        String userId = (String)params.get("userId");
        String status = (String)params.get("status");

        QueryWrapper<LicenseOrderEntity> queryWrapper = new QueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            queryWrapper.like("order_no", orderNo);
        }
        if (userId != null && !userId.isEmpty()) {
            queryWrapper.eq("user_id", userId);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");

        IPage<LicenseOrderEntity> page = this.page(
                new Query<LicenseOrderEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean saveOrder(LicenseOrderEntity entity) {
        return this.save(entity);
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        LicenseOrderEntity entity = new LicenseOrderEntity();
        entity.setId(orderId);
        entity.setStatus(status);
        return this.updateById(entity);
    }

    @Override
    public LicenseOrderEntity getByOrderNo(String orderNo) {
        QueryWrapper<LicenseOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        return this.getOne(queryWrapper);
    }
}