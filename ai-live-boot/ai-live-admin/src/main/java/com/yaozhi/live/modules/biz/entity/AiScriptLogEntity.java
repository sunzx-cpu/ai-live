package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * AI话术生成记录实体类
 * 记录所有话术生成的历史
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Data
@TableName("tb_ai_script_log")
public class AiScriptLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 话术类型：auto_script,keyword_reply,knowledge_qa,batch_generate
     */
    private String scriptType;

    /**
     * 输入内容
     */
    private String inputContent;

    /**
     * 输出内容
     */
    private String outputContent;

    /**
     * 上下文信息
     */
    private String contextInfo;

    /**
     * 使用的提示词名称
     */
    private String promptName;

    /**
     * 生成耗时(毫秒)
     */
    private Integer generationTime;

    /**
     * Token使用量
     */
    private Integer tokenUsage;

    /**
     * 成本
     */
    private BigDecimal cost;

    /**
     * 状态：1成功，0失败
     */
    private Boolean status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    // ============ 非数据库字段 ============
    /**
     * 项目名称
     */
    @TableField(exist = false)
    private String projectName;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String userName;
} 