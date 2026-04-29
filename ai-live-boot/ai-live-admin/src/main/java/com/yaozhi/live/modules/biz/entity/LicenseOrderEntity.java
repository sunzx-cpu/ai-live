package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * license订单信息表
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_license_order")
public class LicenseOrderEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 关联套餐ID
     */
    private Long packageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 订单状态：1-待支付 2-已支付 3-已取消
     */
    private Integer status;

    /**
     * 支付渠道
     */
    private String payChannel;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 商户id
     */
    private Long merchantId;

}