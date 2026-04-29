package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.modules.biz.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;

import com.yaozhi.live.modules.biz.dao.LiveSegmentDao;
import com.yaozhi.live.modules.biz.entity.LiveSegmentEntity;
import com.yaozhi.live.modules.biz.service.LiveSegmentService;


@Service("liveSegmentService")
public class LiveSegmentServiceImpl extends ServiceImpl<LiveSegmentDao, LiveSegmentEntity> implements LiveSegmentService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<LiveSegmentEntity> page = this.baseMapper.queryPage(new Query<LiveSegmentEntity>().getPage(params), params);
        return new PageUtils(page);
    }

    @Override
    public List<LiveSegmentEntity> queryList(Map<String, Object> params) {
        List<LiveSegmentEntity> list = this.baseMapper.queryList(params);
        return list;
    }

    @Override
    public Boolean isExist(Long segmentId, Long scriptId, String name) {
        return this.count(new LambdaQueryWrapper<LiveSegmentEntity>()
                .eq(LiveSegmentEntity::getScriptId, scriptId)
                .eq(LiveSegmentEntity::getName, name)
                .ne(segmentId != null, LiveSegmentEntity::getId, segmentId)
        ) > 0;
    }

    @Override
    public void exportToExcel(Map<String, Object> params, HttpServletResponse response, Long userId) throws IOException {
        List<LiveSegmentEntity> segmentList = new ArrayList<>();

        try {
            // 获取数据
            String idsParam = (String) params.get("ids");

            if (idsParam != null && !idsParam.trim().isEmpty()) {
                // 根据ID获取数据
                String[] idArray = idsParam.split(",");
                List<Long> ids = new ArrayList<>();
                for (String idStr : idArray) {
                    try {
                        ids.add(Long.valueOf(idStr.trim()));
                    } catch (NumberFormatException e) {
                        // 忽略无效的ID
                    }
                }
                if (!ids.isEmpty()) {
                    segmentList = this.listByIds(ids);
                }
            }

            // 设置响应头 - 使用.xls格式
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");

            String fileName = "直播片段_" + System.currentTimeMillis() + ".xls";
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 使用HSSFWorkbook创建.xls格式的Excel文件
            org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
            org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("直播片段");

            // 创建标题行
            org.apache.poi.hssf.usermodel.HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("片段编号");
            headerRow.createCell(1).setCellValue("片段名称");
            headerRow.createCell(2).setCellValue("片段内容");
            headerRow.createCell(3).setCellValue("创建时间");

            // 写入数据
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < segmentList.size(); i++) {
                org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(i + 1);
                LiveSegmentEntity segment = segmentList.get(i);

                row.createCell(0).setCellValue(segment.getId() != null ? segment.getId().toString() : "");
                row.createCell(1).setCellValue(segment.getName() != null ? segment.getName() : "");
                row.createCell(2).setCellValue(segment.getContent() != null ? segment.getContent() : "");
                row.createCell(3).setCellValue(segment.getCreateTime() != null ?
                        dateFormat.format(segment.getCreateTime()) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // 直接写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            // 如果出错，返回JSON错误信息
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"导出失败: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public String importFromExcel(MultipartFile file, Long userId) throws Exception {
        // 验证文件格式
        if (!ExcelUtils.isValidExcelFile(file.getOriginalFilename())) {
            throw new RuntimeException("请上传Excel格式文件(.xlsx或.xls)");
        }

        // 读取Excel文件
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        List<String> errorMessages = new ArrayList<>();

        // 从第二行开始读取数据（第一行是表头）
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (ExcelUtils.isEmptyRow(row)) {
                continue;
            }

            try {
                // 读取单元格数据
                String name = ExcelUtils.getCellStringValue(row.getCell(1));
                String content = ExcelUtils.getCellStringValue(row.getCell(2));
                // 第3列现在是创建时间，不需要读取

                // 验证必填字段
                if (name == null || name.trim().isEmpty()) {
                    errorMessages.add("第" + (i + 1) + "行：片段名称不能为空");
                    errorCount++;
                    continue;
                }

                // 创建片段实体
                LiveSegmentEntity segment = new LiveSegmentEntity();
                segment.setName(name.trim());
                segment.setContent(content != null ? content.trim() : "");
                segment.setUserId(userId);
                segment.setCreateTime(new Date());

                // 设置脚本ID为空（需要手动关联）
                segment.setScriptId(null);

                // 检查是否存在同名片段
                if (!isExist(null, segment.getScriptId(), segment.getName())) {
                    this.save(segment);
                    successCount++;
                } else {
                    skipCount++;
                }

            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行：数据格式错误 - " + e.getMessage());
                errorCount++;
            }
        }

        workbook.close();

        // 生成结果消息
        StringBuilder result = new StringBuilder();
        result.append("导入完成！");
        result.append("成功导入 ").append(successCount).append(" 条片段");

        if (skipCount > 0) {
            result.append("，跳过 ").append(skipCount).append(" 条重复片段");
        }

        if (errorCount > 0) {
            result.append("，").append(errorCount).append(" 条数据导入失败");
            if (!errorMessages.isEmpty()) {
                result.append("：").append(String.join("；", errorMessages.subList(0, Math.min(3, errorMessages.size()))));
                if (errorMessages.size() > 3) {
                    result.append("等");
                }
            }
        }

        return result.toString();
    }

    @Override
    public R listByScriptId(Long scriptId) {
        List<LiveSegmentEntity> list = this.list(new LambdaQueryWrapper<LiveSegmentEntity>()
                .eq(LiveSegmentEntity::getScriptId, scriptId)
                .orderByAsc(LiveSegmentEntity::getId)
        );
        return R.ok().put("list", list);
    }
}
