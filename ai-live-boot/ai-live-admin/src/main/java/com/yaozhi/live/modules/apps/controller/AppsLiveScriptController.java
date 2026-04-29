package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.LiveScriptEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 直播脚本
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
@RestController
@RequestMapping("/apps/live-script")
public class AppsLiveScriptController {

    @Autowired
    private LiveScriptService liveScriptService;

    /**
     * 列表
     */
    @Login
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {

        params.put("userId", user.getId().toString());
        PageUtils page = liveScriptService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @Login
    @RequestMapping("/all")
    public R all(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {

        params.put("userId", user.getId().toString());
        List<LiveScriptEntity> list = liveScriptService.queryList(params);

        return R.ok().put("list", list);
    }

    /**
     * 信息
     */
    @Login
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        LiveScriptEntity liveScript = liveScriptService.getById(id);

        return R.ok().put("liveScript", liveScript);
    }

    /**
     * 保存
     */
    @Login
    @RequestMapping("/save")
    public R save(@RequestBody LiveScriptEntity liveScript, @LoginUser UserEntity user) {

        if (liveScriptService.isExist(null, user.getId(), liveScript.getName())) {
            return R.error("当前用户下已存在脚本名称：" + liveScript.getName());
        }

        liveScript.setUserId(user.getId());
        liveScript.setCreateTime(new Date());
        liveScriptService.save(liveScript);

        return R.ok();
    }

    /**
     * 修改
     */
    @Login
    @RequestMapping("/update")
    public R update(@RequestBody LiveScriptEntity liveScript, @LoginUser UserEntity user) {
        // 获取原脚本的userId
        LiveScriptEntity oldScript = liveScriptService.getById(liveScript.getId());
        if (oldScript == null) {
            return R.error("脚本不存在");
        }

        if (liveScriptService.isExist(liveScript.getId(), oldScript.getUserId(), liveScript.getName())) {
            return R.error("当前用户下已存在脚本名称：" + liveScript.getName());
        }

        liveScriptService.updateById(liveScript);

        return R.ok();
    }

    /**
     * 删除
     */
    @Login
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        liveScriptService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 导出Excel
     */
    @Login
    @RequestMapping("/exportExcel")
    public void exportExcel(@RequestParam Map<String, Object> params,
            HttpServletResponse response, @LoginUser UserEntity user) throws IOException {
        liveScriptService.exportToExcel(params, response, user.getId());
    }

    /**
     * 导入Excel
     */
    @Login
    @RequestMapping("/importExcel")
    public R importExcel(@RequestParam("file") MultipartFile file, @LoginUser UserEntity user) {
        try {
            String result = liveScriptService.importFromExcel(file, user.getId());
            return R.ok(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
