package com.yaozhi.live.modules.biz.controller;

import com.yaozhi.live.common.enums.SecretKeyStatusEnum;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.SecretKeyEntity;
import com.yaozhi.live.modules.biz.service.SecretKeyService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 密钥
 *
 * @author
 * @email
 * @date 2025-09-30 13:56:51
 */
@RestController
@RequestMapping("biz/secretkey")
public class SecretKeyController {
    @Autowired
    private SecretKeyService secretKeyService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:secretkey:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = secretKeyService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:secretkey:info")
    public R info(@PathVariable("id") Long id) {
        SecretKeyEntity secretKey = secretKeyService.getById(id);

        return R.ok().put("secretKey", secretKey);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:secretkey:save")
    public R save(@RequestBody SecretKeyEntity secretKey) {
        secretKey.setStatus(SecretKeyStatusEnum.UNUSED.getValue());
        secretKeyService.save(secretKey);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:secretkey:update")
    public R update(@RequestBody SecretKeyEntity secretKey) {
        secretKeyService.updateById(secretKey);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:secretkey:delete")
    public R delete(@RequestBody Long[] ids) {
        secretKeyService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
