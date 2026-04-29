package com.yaozhi.live.modules.apps.controller;

import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.annotation.Login;
import com.yaozhi.live.common.annotation.LoginUser;
import com.yaozhi.live.modules.biz.entity.LiveInteractKeywordEntity;
import com.yaozhi.live.modules.biz.entity.UserEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractKeywordService;
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
 * 交互关键词
 *
 * @author
 * @email
 * @date 2025-06-20 21:39:34
 */
@RestController
@RequestMapping("/apps/live-interact-keyword")
public class AppsLiveInteractKeywordController {

    @Autowired
    private LiveInteractKeywordService liveInteractKeywordService;

    /**
     * 列表
     */
    @Login
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, @LoginUser UserEntity user){
        params.put("userId", user.getId().toString());
        PageUtils page = liveInteractKeywordService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @Login
    @RequestMapping("/all")
    public R all(@RequestParam Map<String, Object> params, @LoginUser UserEntity user){
        params.put("userId", user.getId().toString());
        List<LiveInteractKeywordEntity> list = liveInteractKeywordService.queryList(params);

        return R.ok().put("list", list);
    }

    /**
     * 信息
     */
    @Login
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		LiveInteractKeywordEntity liveInteractKeyword = liveInteractKeywordService.getById(id);

        return R.ok().put("liveInteractKeyword", liveInteractKeyword);
    }

    /**
     * 保存
     */
    @Login
    @RequestMapping("/save")
    public R save(@RequestBody LiveInteractKeywordEntity liveInteractKeyword, @LoginUser UserEntity user){
        liveInteractKeyword.setUserId(user.getId());
        liveInteractKeyword.setCreateTime(new Date());
		liveInteractKeywordService.save(liveInteractKeyword);

        return R.ok();
    }

    /**
     * 修改
     */
    @Login
    @RequestMapping("/update")
    public R update(@RequestBody LiveInteractKeywordEntity liveInteractKeyword){
		liveInteractKeywordService.updateById(liveInteractKeyword);

        return R.ok();
    }

    /**
     * 删除
     */
    @Login
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		liveInteractKeywordService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 导出Excel
     */
    @Login
    @RequestMapping("/exportExcel")
    public void exportExcel(@RequestParam Map<String, Object> params,
            HttpServletResponse response, @LoginUser UserEntity user) throws IOException {
        liveInteractKeywordService.exportToExcel(params, response, user.getId());
    }

    /**
     * 导入Excel
     */
    @Login
    @RequestMapping("/importExcel")
    public R importExcel(@RequestParam("file") MultipartFile file, @LoginUser UserEntity user) {
        try {
            String result = liveInteractKeywordService.importFromExcel(file, user.getId());
            return R.ok(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

}
