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

import com.yaozhi.live.modules.biz.entity.LiveSegmentEntity;
import com.yaozhi.live.modules.biz.service.LiveSegmentService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;



/**
 * 直播片段
 *
 * @author 
 * @email 
 * @date 2025-06-14 08:36:01
 */
@RestController
@RequestMapping("biz/livesegment")
public class LiveSegmentController {
    @Autowired
    private LiveSegmentService liveSegmentService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:livesegment:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = liveSegmentService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:livesegment:info")
    public R info(@PathVariable("id") Long id){
		LiveSegmentEntity liveSegment = liveSegmentService.getById(id);

        return R.ok().put("liveSegment", liveSegment);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:livesegment:save")
    public R save(@RequestBody LiveSegmentEntity liveSegment){
		liveSegmentService.save(liveSegment);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:livesegment:update")
    public R update(@RequestBody LiveSegmentEntity liveSegment){
		liveSegmentService.updateById(liveSegment);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:livesegment:delete")
    public R delete(@RequestBody Long[] ids){
		liveSegmentService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
