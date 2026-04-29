package com.yaozhi.live.modules.biz.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.exception.RRException;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.common.validator.Assert;
import com.yaozhi.live.modules.appc.form.LoginForm;
import com.yaozhi.live.modules.biz.dao.UserDao;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.MerchantService;
import com.yaozhi.live.modules.biz.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Autowired
    @Lazy
    private MerchantService merchantService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String username = (String) params.get("username");
        String mobile = (String) params.get("mobile");
        String nickname = (String) params.get("nickname");
        IPage<UserEntity> page = this.page(new Query<UserEntity>().getPage(params),
                new LambdaQueryWrapper<UserEntity>()
                        .eq(StringUtils.isNotBlank(username), UserEntity::getUsername, username)
                        .eq(StringUtils.isNotBlank(mobile), UserEntity::getMobile, mobile)
                        .eq(StringUtils.isNotBlank(nickname), UserEntity::getNickname, nickname)
                        .orderByDesc(UserEntity::getId)
        );
        // 关联查询商户名称
        page.getRecords().forEach(user -> {
            MerchantEntity merchant = merchantService.getById(user.getMerchantId());
            user.setMerchantName(merchant != null ? merchant.getNickname() : "");
            user.setStatus(merchant != null ? merchant.getStatus() : null);
        });

        return new PageUtils(page);
    }


    @Override
    public UserEntity queryByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
        );
    }

    @Override
    public UserEntity queryByMobile(String mobile) {
        return baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getMobile, mobile)
        );
    }

    @Override
    public long login(LoginForm form) {
        UserEntity user = queryByUsername(form.getUsername());
        Assert.isNull(user, "用户名错误");

        // 检查用户是否到期
        MerchantEntity merchant = merchantService.getById(user.getMerchantId());
        if (merchant.getExpirationTime().before(new Date())) {
            throw new RRException("商户已到期");
        }

        //密码错误
        if (!user.getPassword().equals(DigestUtils.sha256Hex(form.getPassword() + user.getSalt()))) {
            throw new RRException("密码错误");
        }

        // 保存登录信息
        merchantService.update(Wrappers.<MerchantEntity>lambdaUpdate()
                .set(MerchantEntity::getLastLoginTime, new Date())
                .set(MerchantEntity::getLastLoginIp, form.getLastLoginIp())
                .eq(MerchantEntity::getId, user.getMerchantId()));

        return user.getId();
    }

    @Override
    public void register(UserEntity user) {
        if (queryByUsername(user.getUsername()) != null) {
            throw new RRException("用户名已存在");
        }
        user.setSalt(StringUtils.UUID());
        user.setPassword(DigestUtils.sha256Hex(user.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        this.save(user);
    }

    @Override
    public UserEntity queryByMerchantId(Long merchantId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getMerchantId, merchantId)
        );
    }
}
