package com.yaozhi.live.modules.biz.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.modules.appc.form.LoginForm;
import com.yaozhi.live.modules.biz.entity.UserEntity;

import java.util.Map;

/**
 * 用户
 */
public interface UserService extends IService<UserEntity> {

	/**
	 * 查询用户
	 * @param params 查询参数
	 * @return 用户列表
	 */
	PageUtils queryPage(Map<String, Object> params);

	/**
	 * 通过用户名查询用户
	 * @param username 手机号
	 * @return 用户
	 */
	UserEntity queryByUsername(String username);

	/**
	 * 通过手机号查询用户
	 * @param mobile 手机号
	 * @return 用户
	 */
	UserEntity queryByMobile(String mobile);

	/**
	 * 用户登录
	 * @param form    登录表单
	 * @return        返回用户ID
	 */
	long login(LoginForm form);

	/**
	 * 注册用户
	 * @param user 用户信息
	 */
	void register(UserEntity user);

	/**
	 * 通过商户id查询用户
	 * @param merchantId 商户id
	 * @return 用户
	 */
	UserEntity queryByMerchantId(Long merchantId);
}
