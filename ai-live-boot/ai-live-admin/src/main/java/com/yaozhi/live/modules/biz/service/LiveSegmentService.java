package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.entity.LiveSegmentEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 直播片段
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
public interface LiveSegmentService extends IService<LiveSegmentEntity> {

    /**
     * 分页查询
     * @param params 查询参数
     * @return 查询结果
     */
    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询列表
     * @param params 查询参数
     * @return 查询结果
     */
    List<LiveSegmentEntity> queryList(Map<String, Object> params);

    /**
     * 判断是否存在
     * @param segmentId 片段ID
     * @param scriptId 脚本ID
     * @param name 片段名称
     * @return 是否存在
     */
    Boolean isExist(Long segmentId, Long scriptId, String name);

    /**
     * 导出到Excel
     * @param params 查询参数
     * @param response HTTP响应
     * @param userId 用户ID
     * @throws IOException IO异常
     */
    void exportToExcel(Map<String, Object> params, HttpServletResponse response, Long userId) throws IOException;

    /**
     * 从Excel导入
     * @param file Excel文件
     * @param userId 用户ID
     * @return 导入结果
     * @throws Exception 异常
     */
    String importFromExcel(MultipartFile file, Long userId) throws Exception;

    /**
     * 根据脚本ID获取片段列表
     * @param scriptId 脚本ID
     * @return 查询结果
     */
    R listByScriptId(Long scriptId);

}

