package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 直播片段
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@Data
@TableName("tb_live_segment")
public class LiveSegmentEntity implements Serializable {
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
	 * 片段名称
	 */
	private String name;
	/**
	 * 片段内容
	 */
	private String content;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 脚本名称
	 */
	@TableField(exist = false)
	private String scriptName;

}
