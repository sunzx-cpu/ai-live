package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.modules.apps.form.LoginForm;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;

import java.util.Map;

/**
 * 商户
 */
public interface MerchantService extends IService<MerchantEntity> {

	/**
	 * 查询商户
	 * 
	 * @param params 查询参数
	 * @return 商户列表
	 */
	PageUtils queryPage(Map<String, Object> params);

	/**
	 * 通过手机号查询商户
	 * 
	 * @param mobile 手机号
	 * @return 商户
	 */
	MerchantEntity queryByNickname(String mobile);

	/**
	 * 商户登录
	 * 
	 * @param form 登录表单
	 * @return 返回商户ID
	 */
	long login(LoginForm form);

	/**
	 * 获取商户id
	 * @param userId
	 * @return
	 */
	Long getMerchantId(Long userId);
}
