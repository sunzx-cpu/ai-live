package com.yaozhi.live.modules.biz.dto;

import com.yaozhi.live.modules.biz.entity.LicensePackageEntity;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * License套餐详情DTO
 * @author Administrator
 */
@Data
public class LicensePackageDetailDTO {
    
    /**
     * 套餐基本信息
     */
    private LicensePackageEntity packageInfo;
    
    /**
     * 已激活数量
     */
    private Integer activatedCount;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 订单总数
     */
    private Integer orderCount;
    
    /**
     * 最近激活时间
     */
    private Date lastActivateTime;
}