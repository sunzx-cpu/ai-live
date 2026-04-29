package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 密钥
 * 
 * @author 
 * @email 
 * @date 2025-09-30 13:56:51
 */
@Data
@TableName("tb_secret_key")
public class SecretKeyEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 密钥
	 */
	private String secretKey;
	/**
	 * 状态 0-未使用 1-已使用
	 */
	private Integer status;
	/**
	 * 到期时间
	 */
	private Date expirationTime;
	/**
	 * 商户id
	 */
	private Long merchantId;

}
