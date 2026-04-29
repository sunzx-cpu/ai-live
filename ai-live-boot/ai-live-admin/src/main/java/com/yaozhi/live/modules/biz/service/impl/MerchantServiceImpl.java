package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.exception.RRException;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.common.validator.Assert;
import com.yaozhi.live.modules.apps.form.LoginForm;
import com.yaozhi.live.modules.biz.dao.MerchantDao;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.MerchantService;
import com.yaozhi.live.modules.biz.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("merchantService")
public class MerchantServiceImpl extends ServiceImpl<MerchantDao, MerchantEntity> implements MerchantService {

    @Autowired
    private UserService userService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String nickname = (String) params.get("nickname");
        IPage<MerchantEntity> page = this.page(new Query<MerchantEntity>().getPage(params),
                new LambdaQueryWrapper<MerchantEntity>()
                        .like(StringUtils.isNotBlank(nickname), MerchantEntity::getNickname, nickname)
                        .orderByDesc(MerchantEntity::getId)
        );

        return new PageUtils(page);
    }

    @Override
    public MerchantEntity queryByNickname(String nickname) {
        return baseMapper.selectOne(new LambdaQueryWrapper<MerchantEntity>()
                .eq(MerchantEntity::getNickname, nickname)
        );
    }

    @Override
    public long login(LoginForm form) {
        UserEntity user = userService.queryByUsername(form.getUsername());
        Assert.isNull(user, "用户名错误");

        //密码错误
        if(!user.getPassword().equals(DigestUtils.sha256Hex(form.getPassword() + user.getSalt()))){
            throw new RRException("密码错误");
        }

        return user.getId();
    }

    @Override
    public Long getMerchantId(Long userId) {
        return userService.getById(userId).getMerchantId();
    }
}
