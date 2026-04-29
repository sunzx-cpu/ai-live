package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 交互动作
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@Data
@TableName("tb_live_interact_action")
public class LiveInteractActionEntity implements Serializable {
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
	 * 脚本id
	 */
	private Long scriptId;
	/**
	 * 项目id
	 */
	private Long projectId;
	/**
	 * 行为类型
	 */
	private String actionType;
	/**
	 * 回复
	 */
	private String content;
	/**
	 * 创建时间
	 */
	private Date createTime;

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
