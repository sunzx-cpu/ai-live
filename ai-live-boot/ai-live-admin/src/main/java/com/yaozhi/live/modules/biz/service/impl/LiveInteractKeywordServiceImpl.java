package com.yaozhi.live.modules.biz.service.impl;

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

import com.yaozhi.live.modules.biz.dao.LiveInteractKeywordDao;
import com.yaozhi.live.modules.biz.entity.LiveInteractKeywordEntity;
import com.yaozhi.live.modules.biz.service.LiveInteractKeywordService;


@Service("liveInteractKeywordService")
public class LiveInteractKeywordServiceImpl extends ServiceImpl<LiveInteractKeywordDao, LiveInteractKeywordEntity> implements LiveInteractKeywordService {


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        IPage<LiveInteractKeywordEntity> page = this.baseMapper.queryPage(new Query<LiveInteractKeywordEntity>().getPage(params),  params);

        return new PageUtils(page);
    }

    @Override
    public List<LiveInteractKeywordEntity> queryList(Map<String, Object> params) {
        return this.baseMapper.queryList(params);
    }

    @Override
    public void exportToExcel(Map<String, Object> params, HttpServletResponse response, Long userId) throws IOException {
        List<LiveInteractKeywordEntity> keywordList = new ArrayList<>();

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
                    keywordList = this.listByIds(ids);
                }
            }

            // 设置响应头 - 使用.xls格式
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");

            String fileName = "关键词互动_" + System.currentTimeMillis() + ".xls";
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

            // 使用HSSFWorkbook创建.xls格式的Excel文件
            org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
            org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("关键词互动");

            // 创建标题行
            org.apache.poi.hssf.usermodel.HSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("关键词编号");
            headerRow.createCell(1).setCellValue("关键词");
            headerRow.createCell(2).setCellValue("回复内容");
            headerRow.createCell(3).setCellValue("创建时间");

            // 写入数据
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < keywordList.size(); i++) {
                org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(i + 1);
                LiveInteractKeywordEntity keyword = keywordList.get(i);

                row.createCell(0).setCellValue(keyword.getId() != null ? keyword.getId().toString() : "");
                row.createCell(1).setCellValue(keyword.getKeyword() != null ? keyword.getKeyword() : "");
                row.createCell(2).setCellValue(keyword.getContent() != null ? keyword.getContent() : "");
                row.createCell(3).setCellValue(keyword.getCreateTime() != null ?
                        dateFormat.format(keyword.getCreateTime()) : "");
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
                String keyword = ExcelUtils.getCellStringValue(row.getCell(1));
                String content = ExcelUtils.getCellStringValue(row.getCell(2));

                // 验证必填字段
                if (keyword == null || keyword.trim().isEmpty()) {
                    errorMessages.add("第" + (i + 1) + "行：关键词不能为空");
                    errorCount++;
                    continue;
                }

                // 创建关键词实体
                LiveInteractKeywordEntity keywordEntity = new LiveInteractKeywordEntity();
                keywordEntity.setKeyword(keyword.trim());
                keywordEntity.setContent(content != null ? content.trim() : "");
                keywordEntity.setUserId(userId);
                keywordEntity.setCreateTime(new Date());
                keywordEntity.setProjectId(null);
                keywordEntity.setScriptId(null);

                this.save(keywordEntity);
                successCount++;

            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行：数据格式错误 - " + e.getMessage());
                errorCount++;
            }
        }

        workbook.close();

        // 生成结果消息
        StringBuilder result = new StringBuilder();
        result.append("导入完成！");
        result.append("成功导入 ").append(successCount).append(" 条关键词");

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
}
