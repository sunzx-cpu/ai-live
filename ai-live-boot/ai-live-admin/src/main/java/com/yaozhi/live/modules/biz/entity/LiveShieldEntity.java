package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 屏蔽自己&敏感词
 */
@Data
@TableName("tb_live_shield")
public class LiveShieldEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 屏蔽用户名
     */
    private String username;
    /**
     * 移除敏感词
     */
    private String sensitiveWords;
    /**
     * 屏蔽整句
     */
    private String wholeSentence;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 商户id
     */
    private Long merchantId;

}
