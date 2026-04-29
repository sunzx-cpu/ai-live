package com.yaozhi.live.modules.biz.controller;

import java.util.Arrays;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yaozhi.live.modules.biz.entity.LiveInteractActionEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractActionService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;



/**
 * 交互动作
 *
 * @author 
 * @email 
 * @date 2025-06-20 21:39:34
 */
@RestController
@RequestMapping("biz/liveinteractaction")
public class LiveInteractActionController {
    @Autowired
    private LiveInteractActionService liveInteractActionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:liveinteractaction:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = liveInteractActionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:liveinteractaction:info")
    public R info(@PathVariable("id") Long id){
		LiveInteractActionEntity liveInteractAction = liveInteractActionService.getById(id);

        return R.ok().put("liveInteractAction", liveInteractAction);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:liveinteractaction:save")
    public R save(@RequestBody LiveInteractActionEntity liveInteractAction){
		liveInteractActionService.save(liveInteractAction);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:liveinteractaction:update")
    public R update(@RequestBody LiveInteractActionEntity liveInteractAction){
		liveInteractActionService.updateById(liveInteractAction);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:liveinteractaction:delete")
    public R delete(@RequestBody Long[] ids){
		liveInteractActionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
