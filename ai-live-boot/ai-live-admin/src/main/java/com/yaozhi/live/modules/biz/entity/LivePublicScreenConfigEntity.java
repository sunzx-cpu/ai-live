package com.yaozhi.live.modules.biz.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 直播间公屏配置
 * 
 * @author 
 * @email 
 * @date 2025-11-04 18:34:34
 */
@Data
@TableName("tb_live_public_screen_config")
public class LivePublicScreenConfigEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 直播地址
	 */
	private String liveUrl;
	/**
	 * 弹幕内容
	 */
	private String danmakuContent;
	/**
	 * 弹幕间隔最小值
	 */
	private String intervalMin;
	/**
	 * 弹幕间隔最大值
	 */
	private String intervalMax;
	/**
	 * 弹幕间隔（0-关闭 1-开启）
	 */
	private Integer enabledInterval;
	/**
	 * 文字回复（0-关闭 1-开启）
	 */
	private Integer enabledTextReply;
	/**
	 * 网页静音（0-关闭 1-开启）
	 */
	private Integer enabledMute;
	/**
	 * 启用GPT互动（0-关闭 1-开启）
	 */
	private Integer enableGptInteraction;
	/**
	 * 启用知识库（0-关闭 1-开启）
	 */
	private Integer enableKnowledge;
	/**
	 * 重复用户发言（0-关闭 1-开启）
	 */
	private Integer repeatUserStatements;
	/**
	 * 点用户名字（0-关闭 1-开启）
	 */
	private Integer clickUsername;
	/**
	 * 打断并立即互动（0-关闭 1-开启）
	 */
	private Integer interactNow;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 商户id
	 */
	private Long merchantId;

}
