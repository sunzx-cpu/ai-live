package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 直播脚本
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@Data
@TableName("tb_live_script")
public class LiveScriptEntity implements Serializable {
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
	 * 脚本名称
	 */
	private String name;
	/**
	 * 脚本内容
	 */
	private String content;
	/**
	 * 创建时间
	 */
	private Date createTime;

}
