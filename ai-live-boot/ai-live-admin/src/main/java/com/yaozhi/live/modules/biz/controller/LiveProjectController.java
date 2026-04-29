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

import com.yaozhi.live.modules.biz.entity.LiveProjectEntity;
import com.yaozhi.live.modules.biz.service.LiveProjectService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;



/**
 * 直播项目
 *
 * @author 
 * @email 
 * @date 2025-06-14 08:36:01
 */
@RestController
@RequestMapping("biz/liveproject")
public class LiveProjectController {
    @Autowired
    private LiveProjectService liveProjectService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:liveproject:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = liveProjectService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:liveproject:info")
    public R info(@PathVariable("id") Integer id){
		LiveProjectEntity liveProject = liveProjectService.getById(id);

        return R.ok().put("liveProject", liveProject);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:liveproject:save")
    public R save(@RequestBody LiveProjectEntity liveProject){
		liveProjectService.save(liveProject);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:liveproject:update")
    public R update(@RequestBody LiveProjectEntity liveProject){
		liveProjectService.updateById(liveProject);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:liveproject:delete")
    public R delete(@RequestBody Integer[] ids){
		liveProjectService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
