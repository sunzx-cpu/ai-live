package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 交互关键词
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@Data
@TableName("tb_live_interact_keyword")
public class LiveInteractKeywordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 脚本id
     */
    private Long scriptId;
    /**
     * 关键词
     */
    private String keyword;
    /**
     * 回复
     */
    private String content;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 商户id
     */
    private Long merchantId;
    /**
     * 项目名称
     */
    @TableField(exist = false)
    private String projectName;
    /**
     * 脚本名称
     */
    @TableField(exist = false)
    private String scriptName;

}
