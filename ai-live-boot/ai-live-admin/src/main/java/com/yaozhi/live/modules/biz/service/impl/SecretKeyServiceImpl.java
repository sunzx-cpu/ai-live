package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.modules.biz.dao.SecretKeyDao;
import com.yaozhi.live.modules.biz.entity.SecretKeyEntity;
import com.yaozhi.live.modules.biz.service.SecretKeyService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("secretKeyService")
public class SecretKeyServiceImpl extends ServiceImpl<SecretKeyDao, SecretKeyEntity> implements SecretKeyService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SecretKeyEntity> page = this.page(
                new Query<SecretKeyEntity>().getPage(params),
                new QueryWrapper<SecretKeyEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public SecretKeyEntity queryBySecretKey(String secretKey) {
        return getOne(new LambdaQueryWrapper<SecretKeyEntity>()
                .eq(SecretKeyEntity::getSecretKey, secretKey));
    }

}