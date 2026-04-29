package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户
 */
@Data
@TableName("tb_user")
public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@TableId
	private Long id;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 手机号
	 */
	private String mobile;
	/**
	 * 盐
	 */
	private String salt;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 商户id
	 */
	private Long merchantId;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 简介
	 */
	private String intro;
	/**
	 * 所属商户
	 */
	@TableField(exist = false)
	private String merchantName;
	/**
	 * 状态 0-未激活 1-激活 2-到期 3-禁用
	 */
	@TableField(exist = false)
	private Integer status;
}
