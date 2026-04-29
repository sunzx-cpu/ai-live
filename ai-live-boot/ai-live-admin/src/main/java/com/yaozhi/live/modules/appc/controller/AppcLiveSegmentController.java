package com.yaozhi.live.modules.appc.controller;

import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.service.LiveSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 直播端-片段管理接口
 */
@RestController
@RequestMapping("/appc/live-segment")
public class AppcLiveSegmentController {

    @Autowired
    private LiveSegmentService liveSegmentService;

    /**
     * 根据脚本ID获取片段列表
     */
    @GetMapping("/list-by-script")
    public R listByScript(@RequestParam("scriptId") Long scriptId) {
        return liveSegmentService.listByScriptId(scriptId);
    }
}
