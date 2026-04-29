# 开发参考图片目录

此目录用于存放开发过程中需要参考的UI设计图、截图等图片资源。

## 使用说明

### 目录结构建议
```
images/
├── ui-design/          # UI设计图
├── screenshots/        # 功能截图
├── reference/          # 参考图片
└── archive/            # 已完成功能的存档图片
```

### 命名规范
- 使用描述性的文件名，如：`login-page-design.png`
- 使用日期前缀便于管理，如：`2025-10-13-dashboard-ui.png`
- 使用中横线分隔单词，如：`live-pane-header-layout.png`

### 注意事项
1. 仅用于开发参考，不要提交到生产环境
2. 定期清理已完成功能的参考图片
3. 图片文件不要过大，建议压缩后再存放
4. 可以在图片上标注关键信息便于理解

## 示例

### 引用方式
在开发文档或任务描述中，可以这样引用：
```
参考图片：@docs/images/screenshots/ai-server-dialog.png
```

### 在 CLAUDE 中使用
```
请参考 @docs/images/ui-design/live-pane-header.png 这张截图实现功能
```

---

**创建时间**: 2025-10-13
**最后更新**: 2025-10-13
