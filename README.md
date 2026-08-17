# AI直播管理系统 (AI Live Boot)

## 项目概述

这是一个基于 **Spring Boot 2.6.6** 的AI直播管理系统，采用前后端分离架构，专门针对无人直播模式设计，提供直播项目的全流程管理功能。系统基于 renren-fast 快速开发框架构建，支持脚本化管理、智能互动和多租户架构。

## 技术栈

- **后端框架**：Spring Boot 2.6.6
- **ORM框架**：MyBatis-Plus 3.3.1  
- **数据库**：MySQL 8.0.28
- **安全框架**：Apache Shiro 1.9.0
- **认证方式**：JWT Token
- **连接池**：Druid 1.1.13
- **缓存**：Redis（可选）
- **构建工具**：Maven 3.x
- **Java版本**：JDK 1.8+

## 核心业务模块

### 1. **直播项目管理** (`LiveProject`)
- 项目创建、编辑、删除
- 用户与项目的关联关系
- 项目列表查询和分页
- 支持多项目并行管理

### 2. **直播脚本管理** (`LiveScript`)
- 脚本内容编辑和管理
- 脚本与项目的关联
- 脚本名称唯一性验证
- 支持富文本内容编辑

### 3. **直播片段管理** (`LiveSegment`)
- 片段内容管理
- 片段与脚本的关联
- 支持脚本的细粒度分解
- 便于内容模块化管理

### 4. **直播互动功能**
   - **关键词互动** (`LiveInteractKeyword`)：设置特定关键词触发的自动回复
   - **动作互动** (`LiveInteractAction`)：定义各种互动动作类型和对应的回复内容
   - 智能化互动响应机制

### 5. **用户管理** (`User`)
- 用户注册、登录认证
- 基于JWT的安全认证
- 多租户数据隔离
- 权限管理和访问控制

## 系统架构

### 1. **分层架构**
```
Controller层（接口层）
├── Web端管理接口（/biz/*）- 完整的后台管理功能
└── APP端移动接口（/app/*）- 精简的移动端API

Service层（业务逻辑层）
├── 接口定义 - 业务规范定义
└── 实现类（impl包）- 具体业务逻辑

DAO层（数据访问层）
├── MyBatis-Plus自动生成 - 基础CRUD操作
└── 自定义SQL映射 - 复杂查询逻辑
```

### 2. **权限控制**
- **Web端**：基于Shiro的权限注解（`@RequiresPermissions`）
- **APP端**：基于JWT的登录验证（`@Login`注解）
- **数据隔离**：通过`userId`实现多租户数据隔离
- **接口安全**：统一的认证和授权机制

### 3. **API设计**
- **RESTful风格**：统一的增删改查接口
- **统一响应格式**：使用`R`类封装统一的响应结构
- **分页支持**：集成`PageUtils`实现分页查询
- **参数验证**：完善的入参校验机制

## 核心功能流程

### 1. **项目创建流程**
```
用户登录 → 创建项目 → 创建脚本 → 添加片段 → 设置互动规则 → 启动直播
```

### 2. **互动管理**
- **关键词触发**：用户输入特定关键词时自动回复
- **动作响应**：定义各种直播互动动作的响应内容
- **智能化处理**：支持复杂的互动逻辑配置

### 3. **多端支持**
- **Web管理端**：完整的后台管理功能
- **移动APP端**：精简的移动端API接口
- **统一数据源**：多端数据同步

## 数据库设计

基于实体类分析，数据库包含以下核心表：
- `tb_live_project` - 直播项目表
- `tb_live_script` - 直播脚本表  
- `tb_live_segment` - 直播片段表
- `tb_live_interact_keyword` - 互动关键词表
- `tb_live_interact_action` - 互动动作表
- `tb_user` - 用户表

## 项目特色

1. **AI直播场景**：专门针对无人直播模式设计
2. **脚本化管理**：支持直播内容的结构化管理
3. **智能互动**：基于关键词和动作的自动化互动
4. **多租户架构**：支持多用户独立使用
5. **前后端分离**：便于不同端的开发和维护
6. **模块化设计**：清晰的业务模块划分
7. **可扩展性**：良好的系统架构支持功能扩展

## 项目结构

```
ai-live-boot/
├── src/main/java/io/renren/
│   ├── AiLiveApplication.java          # 项目启动类
│   ├── config/                         # 配置信息
│   │   ├── ShiroConfig.java           # 权限配置
│   │   └── ...                        # 其他配置
│   ├── common/                         # 公共模块
│   │   ├── utils/                     # 工具类
│   │   ├── exception/                 # 异常处理
│   │   ├── validator/                 # 数据校验
│   │   └── xss/                       # XSS过滤
│   └── modules/                        # 功能模块
│       ├── app/                       # APP接口模块
│       │   ├── controller/            # 移动端控制器
│       │   ├── annotation/            # 注解定义
│       │   └── ...
│       ├── biz/                       # 业务模块 (核心)
│       │   ├── controller/            # 业务控制器
│       │   ├── service/               # 业务服务
│       │   ├── entity/                # 实体类
│       │   └── dao/                   # 数据访问
│       ├── sys/                       # 系统模块
│       ├── oss/                       # 文件服务模块
│       └── job/                       # 定时任务模块
└── src/main/resources/
    ├── application.yml                 # 主配置文件
    ├── application-dev.yml             # 开发环境配置
    ├── mapper/                         # SQL映射文件
    └── static/                         # 静态资源
```

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.x
- MySQL 8.0+
- Redis (可选)

### 部署步骤

1. **克隆项目**
```bash
git clone [项目地址]
cd ai-live-boot
```

2. **数据库配置**
- 创建数据库 `ai-live`
- 执行初始化SQL脚本
- 修改 `application-dev.yml` 中的数据库连接信息

3. **启动项目**
```bash
mvn spring-boot:run
```

4. **访问系统**
- 应用地址：http://localhost:8080/ai-live-api
- API文档：http://localhost:8080/ai-live-api/swagger/index.html

## API接口说明

### Web端接口 (需要权限验证)
- `GET /biz/liveproject/list` - 获取项目列表
- `POST /biz/liveproject/save` - 创建项目
- `POST /biz/livescript/save` - 创建脚本
- `GET /biz/livescript/list` - 获取脚本列表

### APP端接口 (需要登录验证)
- `GET /app/live-project/list` - 获取项目列表
- `GET /app/live-project/all` - 获取所有项目
- `POST /app/live-script/save` - 创建脚本
- `GET /app/live-script/list` - 获取脚本列表

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai-live?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### JWT配置
```yaml
renren:
  jwt:
    secret: your_jwt_secret
    expire: 604800  # 7天有效期
    header: token
```

## 开发指南

1. 新增业务模块时，建议按照现有的分层架构进行开发
2. 实体类统一使用 `@Data` 注解和 `@TableName` 注解
3. 控制器统一返回 `R` 类型的响应结果
4. 服务层接口继承 `IService<T>`，实现类继承 `ServiceImpl<M, T>`
5. 权限控制使用 `@RequiresPermissions` 或 `@Login` 注解

## 贡献指南

1. Fork 本项目
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 许可证

本项目只能学习，不能商用。

*基于 renren-fast 快速开发框架构建的AI直播管理系统*
