package com.yaozhi.live.modules.job.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yaozhi.live.common.enums.MerchantStatusEnum;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 检测商户是否到期
 */
@Slf4j
@Component("checkMerchantExpirationTask")
public class CheckMerchantExpirationTask implements ITask {

    @Autowired
    private MerchantService merchantService;

    /**
     * 检测商户是否到期（每分钟执行一次）
     *
     * @param params 参数，多参数使用JSON数据
     */
    @Override
    public void run(String params) {
        log.info("开始检测商户是否到期");
        List<MerchantEntity> list = merchantService.list(Wrappers.<MerchantEntity>lambdaQuery()
                .in(MerchantEntity::getStatus, MerchantStatusEnum.ACTIVATE.getValue(), MerchantStatusEnum.DISABLE.getValue()));
        for (MerchantEntity merchant : list) {
            if (merchant.getExpirationTime().before(new Date())) {
                merchant.setStatus(MerchantStatusEnum.EXPIRE.getValue());
                merchantService.updateById(merchant);
            }
        }

        log.info("结束检测商户是否到期");
    }
}
