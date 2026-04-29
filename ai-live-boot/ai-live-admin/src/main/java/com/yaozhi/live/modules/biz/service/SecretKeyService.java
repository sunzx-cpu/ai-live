package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.modules.biz.entity.SecretKeyEntity;

import java.util.Map;

/**
 * 密钥
 *
 * @author 
 * @email 
 * @date 2025-09-30 13:56:51
 */
public interface SecretKeyService extends IService<SecretKeyEntity> {

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 密钥是否存在
     *
     * @param secretKey
     * @return
     */
    SecretKeyEntity queryBySecretKey(String secretKey);
}

