package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * License激活信息表
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_license_activation")
public class LicenseActivationEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 激活码
     */
    private String activationCode;

    /**
     * 关联套餐ID
     */
    private Long packageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 是否已使用：0-未使用 1-已使用
     */
    private Integer isUsed;

    /**
     * 激活时间
     */
    private Date activateTime;

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