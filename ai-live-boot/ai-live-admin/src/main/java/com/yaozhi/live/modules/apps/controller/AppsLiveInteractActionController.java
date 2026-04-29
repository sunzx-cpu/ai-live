package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.LiveInteractActionEntity;
import com.yaozhi.live.modules.biz.entity.MerchantEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 交互动作
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@RestController
@RequestMapping("/apps/live-interact-action")
public class AppsLiveInteractActionController {

    @Autowired
    private LiveInteractActionService liveInteractActionService;

    /**
     * 列表
     */
    @Login
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        PageUtils page = liveInteractActionService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @Login
    @RequestMapping("/all")
    public R all(@RequestParam Map<String, Object> params, @LoginUser UserEntity user) {
        params.put("userId", user.getId().toString());
        List<LiveInteractActionEntity> list = liveInteractActionService.queryList(params);

        return R.ok().put("list", list);
    }

    /**
     * 信息
     */
    @Login
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        LiveInteractActionEntity liveInteractAction = liveInteractActionService.getById(id);

        return R.ok().put("liveInteractAction", liveInteractAction);
    }

    /**
     * 保存
     */
    @Login
    @RequestMapping("/save")
    public R save(@RequestBody LiveInteractActionEntity liveInteractAction, @LoginUser UserEntity user) {
        liveInteractAction.setUserId(user.getId());
        liveInteractAction.setCreateTime(new Date());
        liveInteractActionService.save(liveInteractAction);

        return R.ok();
    }

    /**
     * 修改
     */
    @Login
    @RequestMapping("/update")
    public R update(@RequestBody LiveInteractActionEntity liveInteractAction) {
        liveInteractActionService.updateById(liveInteractAction);

        return R.ok();
    }

    /**
     * 删除
     */
    @Login
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        liveInteractActionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 导出Excel
     */
    @Login
    @RequestMapping("/exportExcel")
    public void exportExcel(@RequestParam Map<String, Object> params,
            HttpServletResponse response, @LoginUser UserEntity user) throws IOException {
        liveInteractActionService.exportToExcel(params, response, user.getId());
    }

    /**
     * 导入Excel
     */
    @Login
    @RequestMapping("/importExcel")
    public R importExcel(@RequestParam("file") MultipartFile file, @LoginUser UserEntity user) {
        try {
            String result = liveInteractActionService.importFromExcel(file, user.getId());
            return R.ok(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
