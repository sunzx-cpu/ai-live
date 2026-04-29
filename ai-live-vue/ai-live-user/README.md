# PosVistaPro-离线版

基于 Vue.js 和 Element UI 构建的现代化后台管理系统前端框架。

## 项目介绍

Fast-Vue-User 是一个基于 Vue.js 和 Element UI 构建的后台管理系统前端框架，提供了一套完整的前端解决方案。本项目采用最新的前端技术栈，具有高度的可扩展性和可维护性。

## 功能特性

- 🚀 基于 Vue.js 2.x + Element UI 构建
- 📦 使用 Webpack 3.x 进行构建
- 🌍 支持国际化（i18n）
- 🗺️ 集成百度地图组件
- 📊 集成 ECharts 5.x 图表库
- 🔐 完善的权限管理系统
- 🎨 可自定义主题
- 📱 响应式设计，支持多端适配
- 🔄 支持阿里云 OSS 文件上传
- 📝 代码规范检查（ESLint）

## 技术栈

- Vue.js 2.5.16
- Element UI 2.15.14
- Vuex 3.0.1
- Vue Router 3.0.1
- Axios 0.17.1
- ECharts 5.2.0
- Vue-i18n 8.28.2
- Sass/SCSS
- Webpack 3.6.0

## 环境要求

- Node.js >= 8.11.1
- npm >= 5.6.0

## 安装和使用

1. 克隆项目
```bash
git clone [项目地址]
```

2. 安装依赖
```bash
npm install
```

3. 开发环境运行
```bash
npm run dev
```

4. 生产环境构建
```bash
npm run build
```

## 项目结构

```
├── build                      # 构建相关配置
│   ├── webpack.base.conf.js   # webpack 基础配置
│   ├── webpack.dev.conf.js    # 开发环境配置
│   └── webpack.prod.conf.js   # 生产环境配置
├── config                     # 项目配置文件
│   ├── index.js              # 主配置文件
│   └── dev.env.js            # 开发环境变量
├── src                       # 源代码
│   ├── assets               # 静态资源
│   │   ├── images          # 图片资源
│   │   └── scss           # 样式文件
│   ├── components          # 公共组件
│   │   ├── icon-svg       # SVG 图标组件
│   │   └── table-tree-column # 树形表格组件
│   ├── element-ui         # Element UI 相关
│   │   └── theme         # 主题配置
│   ├── icons              # 图标文件
│   ├── i18n               # 国际化文件
│   │   ├── en            # 英文语言包
│   │   └── zh            # 中文语言包
│   ├── router             # 路由配置
│   │   └── index.js      # 路由主文件
│   ├── store              # Vuex 状态管理
│   │   ├── modules       # 状态模块
│   │   └── index.js      # 状态管理主文件
│   ├── utils              # 工具函数
│   │   ├── httpRequest.js # HTTP 请求封装
│   │   ├── validate.js    # 验证工具
│   │   ├── bmap.js       # 百度地图工具
│   │   └── index.js      # 通用工具函数
│   ├── views              # 页面视图
│   │   ├── common        # 公共页面
│   │   ├── modules       # 业务模块
│   │   │   ├── biz      # 业务相关页面
│   │   │   ├── oss      # 文件存储页面
│   │   │   └── sys      # 系统管理页面
│   │   ├── main.vue     # 主页面
│   │   ├── main-navbar.vue    # 顶部导航
│   │   ├── main-sidebar.vue   # 侧边栏
│   │   └── main-content.vue   # 主内容区
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── static                   # 静态文件
├── test                     # 测试文件
│   ├── unit               # 单元测试
│   └── e2e                # 端到端测试
├── .babelrc                # Babel 配置
├── .eslintrc.js            # ESLint 配置
├── .gitignore              # Git 忽略文件
├── gulpfile.js             # Gulp 配置
├── package.json            # 项目依赖
└── README.md               # 项目说明
```

## 功能模块说明

### 1. 系统管理模块 (sys)
- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 岗位管理

### 2. 文件存储模块 (oss)
- 文件上传
- 文件管理
- 阿里云 OSS 集成

### 3. 业务模块 (biz)
- 业务功能页面
- 数据统计
- 报表管理

### 4. 公共组件
- 树形表格组件
- SVG 图标组件
- 通用表单组件
- 数据展示组件

### 5. 工具类
- HTTP 请求封装
- 数据验证工具
- 百度地图工具
- 通用工具函数

## 开发规范

- 遵循 ESLint 规范
- 使用 Vue.js 官方风格指南
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case
- 目录结构遵循功能模块化原则
- 组件和工具函数需要编写注释
- 提交代码前进行代码格式化

## 浏览器支持

- 现代浏览器
- Chrome >= 64
- Firefox >= 67
- Safari >= 11
- Edge >= 79

## 注意事项

1. 开发时请确保 Node.js 和 npm 版本符合要求
2. 使用百度地图功能时需要配置有效的 AK（Access Key）
3. 生产环境部署前请确保已正确配置相关环境变量
4. 注意代码提交前的格式化和测试
5. 遵循项目的目录结构和命名规范

## 贡献指南

1. Fork 本仓库
2. 创建新的功能分支
3. 提交你的更改
4. 发起 Pull Request

## 许可证

[MIT License](LICENSE)
