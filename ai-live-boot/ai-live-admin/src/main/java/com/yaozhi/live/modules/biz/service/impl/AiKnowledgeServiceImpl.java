package com.yaozhi.live.modules.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yaozhi.live.common.utils.PageUtils;
import com.yaozhi.live.common.utils.Query;
import com.yaozhi.live.common.utils.R;
import com.yaozhi.live.common.utils.StringUtils;
import com.yaozhi.live.modules.biz.dao.AiKnowledgeDao;
import com.yaozhi.live.modules.biz.entity.AiKnowledgeEntity;
import com.yaozhi.live.modules.biz.service.AiKnowledgeService;
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

/**
 * AI知识库服务实现
 *
 * @author renren
 * @email renren@renren.io
 * @date 2024-01-23 15:23:23
 */
@Service("aiKnowledgeService")
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeDao, AiKnowledgeEntity> implements AiKnowledgeService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String projectId = (String) params.get("projectId");
        String question = (String) params.get("question");
        String keyword = (String) params.get("keyword");

        IPage<AiKnowledgeEntity> page = this.page(new Query<AiKnowledgeEntity>().getPage(params),
                new LambdaQueryWrapper<AiKnowledgeEntity>()
                        .eq(StringUtils.isNotBlank(projectId), AiKnowledgeEntity::getProjectId, projectId)
                        .like(StringUtils.isNotBlank(question), AiKnowledgeEntity::getQuestion, question)
                        .like(StringUtils.isNotBlank(keyword), AiKnowledgeEntity::getKeywords, keyword)
                        .orderByAsc(AiKnowledgeEntity::getSortOrder)
                        .orderByDesc(AiKnowledgeEntity::getCreateTime)
        );

        return new PageUtils(page);
    }

    @Override
    public List<AiKnowledgeEntity> getListByProjectId(Long projectId) {
        return baseMapper.selectListByProjectId(projectId);
    }

    @Override
    public List<AiKnowledgeEntity> searchByKeyword(Long projectId, String keyword) {
        return baseMapper.selectByKeyword(projectId, keyword);
    }

    @Override
    public List<AiKnowledgeEntity> searchByQuestion(Long projectId, String question) {
        return baseMapper.selectByQuestion(projectId, question);
    }

    @Override
    public R importExcel(Long projectId, Long userId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return R.error("上传文件为空");
            }

            // 验证文件格式
            String fileName = file.getOriginalFilename();
            if (!ExcelUtils.isValidExcelFile(fileName)) {
                return R.error("只支持Excel文件格式(.xlsx或.xls)");
            }

            List<AiKnowledgeEntity> knowledgeList = parseExcelFile(file, projectId, userId);
            if (knowledgeList.isEmpty()) {
                return R.error("Excel文件中没有有效数据");
            }

            // 批量插入
            int batchSize = 1000;
            for (int i = 0; i < knowledgeList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, knowledgeList.size());
                List<AiKnowledgeEntity> batch = knowledgeList.subList(i, end);
                baseMapper.insertBatch(batch);
            }

            return R.ok("导入成功，共导入" + knowledgeList.size() + "条记录");
        } catch (Exception e) {
            return R.error("导入失败：" + e.getMessage());
        }
    }

    @Override
    public void exportExcel(Long projectId, HttpServletResponse response) {
        try {
            List<AiKnowledgeEntity> dataList = getListByProjectId(projectId);
            String[] headers = {"问题", "答案", "关键词", "排序"};

            ExcelUtils.exportToExcel(dataList, headers, "知识库数据", response, "知识库数据.xlsx",
                    (row, entity, cellStyle) -> {
                        ExcelUtils.createCell(row, 0, entity.getQuestion(), cellStyle);
                        ExcelUtils.createCell(row, 1, entity.getAnswer(), cellStyle);
                        ExcelUtils.createCell(row, 2, entity.getKeywords(), cellStyle);
                        ExcelUtils.createCell(row, 3, entity.getSortOrder(), cellStyle);
                    });

        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

    /**
     * 解析Excel文件
     */
    private List<AiKnowledgeEntity> parseExcelFile(MultipartFile file, Long projectId, Long userId) throws IOException {
        List<AiKnowledgeEntity> result = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // 跳过标题行，从第二行开始读取
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || ExcelUtils.isEmptyRow(row)) {
                continue;
            }

            String question = ExcelUtils.getCellStringValue(row.getCell(0));
            String answer = ExcelUtils.getCellStringValue(row.getCell(1));
            String keywords = ExcelUtils.getCellStringValue(row.getCell(2));
            Integer sortOrder = ExcelUtils.getCellIntValue(row.getCell(3));

            // 验证必填字段
            if (StringUtils.isBlank(question) || StringUtils.isBlank(answer)) {
                continue;
            }

            AiKnowledgeEntity entity = new AiKnowledgeEntity();
            entity.setUserId(userId);
            entity.setProjectId(projectId);
            entity.setQuestion(question.trim());
            entity.setAnswer(answer.trim());
            entity.setKeywords(StringUtils.isNotBlank(keywords) ? keywords.trim() : "");
            entity.setSortOrder(sortOrder != null ? sortOrder : 0);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());

            result.add(entity);
        }

        workbook.close();
        return result;
    }


} 