package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户
 * 
 * @author 
 * @email 
 * @date 2025-09-30 14:04:45
 */
@Data
@TableName("tb_merchant")
public class MerchantEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 状态 0-未激活 1-激活 2-到期 3-禁用
	 */
	private Integer status;
	/**
	 * 激活密钥
	 */
	private String activateSecretKey;
	/**
	 * 到期时间
	 */
	private Date expirationTime;
	/**
	 * 注册时间
	 */
	private Date registerTime;
	/**
	 * 注册IP
	 */
	private String registerIp;
	/**
	 * 最后登录时间
	 */
	private Date lastLoginTime;
	/**
	 * 最后登录IP
	 */
	private String lastLoginIp;

//-------------用户信息-----------------
	/**
	 * 手机号
	 */
	@TableField(exist = false)
	private String mobile;
	/**
	 * 账号
	 */
	@TableField(exist = false)
	private String username;
	/**
	 * 密码
	 */
	@TableField(exist = false)
	private String password;
}
