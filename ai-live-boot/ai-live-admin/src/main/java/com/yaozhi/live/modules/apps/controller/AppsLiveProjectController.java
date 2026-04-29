package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.LiveProjectEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveProjectService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 直播项目
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@Tag(name = "直播项目")
@RestController
@RequestMapping("/apps/live-project")
public class AppsLiveProjectController {

    @Autowired
    private LiveProjectService liveProjectService;

    /**
     * 列表
     */
    @Login
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        PageUtils page = liveProjectService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @Login
    @RequestMapping("/all")
    public R all(@LoginUser UserEntity user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getId().toString());
        List<LiveProjectEntity> list = liveProjectService.queryList(params);

        return R.ok().put("list", list);
    }

    /**
     * 信息
     */
    @Login
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id) {
        LiveProjectEntity liveProject = liveProjectService.getById(id);

        return R.ok().put("liveProject", liveProject);
    }

    /**
     * 保存
     */
    @Login
    @RequestMapping("/save")
    public R save(@RequestBody LiveProjectEntity liveProject, @LoginUser UserEntity user) {
        liveProject.setUserId(user.getId());
        liveProject.setCreateTime(new Date());
        liveProjectService.save(liveProject);

        return R.ok();
    }

    /**
     * 修改
     */
    @Login
    @RequestMapping("/update")
    public R update(@RequestBody LiveProjectEntity liveProject) {
        liveProjectService.updateById(liveProject);

        return R.ok();
    }

    /**
     * 删除
     */
    @Login
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids) {
        liveProjectService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
