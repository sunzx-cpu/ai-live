package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.LiveSegmentEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 直播片段
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@RestController
@RequestMapping("/apps/live-segment")
public class AppsLiveSegmentController {

    @Autowired
    private LiveSegmentService liveSegmentService;

    /**
     * 列表
     */
    @Login
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        PageUtils page = liveSegmentService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 所有列表
     */
    @Login
    @RequestMapping("/all")
    public R all(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        List<LiveSegmentEntity> list = liveSegmentService.queryList(params);

        return R.ok().put("list", list);
    }

    /**
     * 信息
     */
    @Login
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        LiveSegmentEntity liveSegment = liveSegmentService.getById(id);

        return R.ok().put("liveSegment", liveSegment);
    }

    /**
     * 保存
     */
    @Login
    @RequestMapping("/save")
    public R save(@RequestBody LiveSegmentEntity liveSegment, @LoginUser UserEntity user) {

        if (liveSegmentService.isExist(null, liveSegment.getScriptId(), liveSegment.getName())) {
            return R.error("当前脚本下已存在片段名称：" + liveSegment.getName());
        }

        liveSegment.setUserId(user.getId());
        liveSegment.setCreateTime(new Date());
        liveSegmentService.save(liveSegment);

        return R.ok();
    }

    /**
     * 修改
     */
    @Login
    @RequestMapping("/update")
    public R update(@RequestBody LiveSegmentEntity liveSegment) {
        if (liveSegmentService.isExist(liveSegment.getId(), liveSegment.getScriptId(), liveSegment.getName())) {
            return R.error("当前脚本下已存在片段名称：" + liveSegment.getName());
        }

        liveSegmentService.updateById(liveSegment);

        return R.ok();
    }

    /**
     * 删除
     */
    @Login
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        liveSegmentService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 导出Excel
     */
    @Login
    @RequestMapping("/exportExcel")
    public void exportExcel(@RequestParam Map<String, Object> params,
            HttpServletResponse response, @LoginUser UserEntity user) throws IOException {
        liveSegmentService.exportToExcel(params, response, user.getId());
    }

    /**
     * 导入Excel
     */
    @Login
    @RequestMapping("/importExcel")
    public R importExcel(@RequestParam("file") MultipartFile file, @LoginUser UserEntity user) {
        try {
            String result = liveSegmentService.importFromExcel(file, user.getId());
            return R.ok(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
