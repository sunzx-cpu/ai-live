package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

/**
 * License套餐信息表
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_license_package")
public class LicensePackageEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 套餐价格
     */
    private BigDecimal price;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

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

}