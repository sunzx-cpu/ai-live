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

import com.yaozhi.live.modules.biz.entity.LiveScriptEntity;
import com.yaozhi.live.modules.biz.service.LiveScriptService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;



/**
 * 直播脚本
 *
 * @author 
 * @email 
 * @date 2025-06-14 08:36:01
 */
@RestController
@RequestMapping("biz/livescript")
public class LiveScriptController {
    @Autowired
    private LiveScriptService liveScriptService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:livescript:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = liveScriptService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:livescript:info")
    public R info(@PathVariable("id") Long id){
		LiveScriptEntity liveScript = liveScriptService.getById(id);

        return R.ok().put("liveScript", liveScript);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:livescript:save")
    public R save(@RequestBody LiveScriptEntity liveScript){
		liveScriptService.save(liveScript);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:livescript:update")
    public R update(@RequestBody LiveScriptEntity liveScript){
		liveScriptService.updateById(liveScript);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:livescript:delete")
    public R delete(@RequestBody Long[] ids){
		liveScriptService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
