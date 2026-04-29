package com.yaozhi.live.modules.biz.dto;

import com.yaozhi.live.modules.biz.entity.LicenseOrderEntity;
import lombok.Data;
import java.math.BigDecimal;

/**
 * License订单详情DTO
 * @author Administrator
 */
@Data
public class LicenseOrderDetailDTO {
    
    /**
     * 订单基本信息
     */
    private LicenseOrderEntity orderInfo;
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 套餐价格
     */
    private BigDecimal price;
}