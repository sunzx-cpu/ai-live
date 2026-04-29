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

import com.yaozhi.live.modules.biz.entity.LiveInteractKeywordEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractKeywordService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;



/**
 * 交互关键词
 *
 * @author 
 * @email 
 * @date 2025-06-20 21:39:34
 */
@RestController
@RequestMapping("biz/liveinteractkeyword")
public class LiveInteractKeywordController {
    @Autowired
    private LiveInteractKeywordService liveInteractKeywordService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("biz:liveinteractkeyword:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = liveInteractKeywordService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("biz:liveinteractkeyword:info")
    public R info(@PathVariable("id") Long id){
		LiveInteractKeywordEntity liveInteractKeyword = liveInteractKeywordService.getById(id);

        return R.ok().put("liveInteractKeyword", liveInteractKeyword);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("biz:liveinteractkeyword:save")
    public R save(@RequestBody LiveInteractKeywordEntity liveInteractKeyword){
		liveInteractKeywordService.save(liveInteractKeyword);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("biz:liveinteractkeyword:update")
    public R update(@RequestBody LiveInteractKeywordEntity liveInteractKeyword){
		liveInteractKeywordService.updateById(liveInteractKeyword);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("biz:liveinteractkeyword:delete")
    public R delete(@RequestBody Long[] ids){
		liveInteractKeywordService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
