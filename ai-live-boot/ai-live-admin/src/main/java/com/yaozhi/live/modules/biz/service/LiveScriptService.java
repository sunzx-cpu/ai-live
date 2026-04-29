package com.yaozhi.live.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.modules.biz.entity.LiveScriptEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 直播脚本
 *
 * @author
 * @email
 * @date 2025-06-14 08:36:01
 */
public interface LiveScriptService extends IService<LiveScriptEntity> {

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
    List<LiveScriptEntity> queryList(Map<String, Object> params);

    /**
     * 判断是否存在
     * @param scriptId 脚本ID
     * @param userId 用户ID
     * @param name 名称
     * @return 是否存在
     */
    Boolean isExist(Long scriptId, Long userId, String name);

    /**
     * 导出脚本到Excel
     * @param params 查询参数
     * @param response HTTP响应
     * @param userId 用户ID
     * @throws IOException IO异常
     */
    void exportToExcel(Map<String, Object> params, HttpServletResponse response, Long userId) throws IOException;

    /**
     * 从Excel导入脚本
     * @param file Excel文件
     * @param userId 用户ID
     * @return 导入结果
     * @throws Exception 异常
     */
    String importFromExcel(MultipartFile file, Long userId) throws Exception;
}

