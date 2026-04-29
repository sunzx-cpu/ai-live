# Windows 打包部署指南（绿色版）

## 📦 打包配置

### 核心原则

1. **绿色版（便携版）**：打包成 zip 文件，解压即用，无需安装
2. **主程序不包含任何 AI 服务**：vsa、f5tts、cosyvoice 由用户自行安装

---

## 🔨 打包命令

```bash
npm run build:win64
```

打包时间：**3-5 分钟**（不包含任何大文件）

---

## 📁 打包输出

打包完成后，在 `build/` 目录下生成：

```
build/
├── ai-live-0.0.1-win.zip          # 主程序 zip 包（50-150MB）
└── win-unpacked/                  # 解压后的文件夹（测试用）
```

**安装包大小**：
- **预期大小**：50MB - 150MB
- **如果超过 500MB**：说明 AI 服务被打包了

---

## 🚀 Windows 部署流程

### 1. 提供给用户的文件

您需要提供两个文件：

```
1. ai-live-0.0.1-win.zip    （主程序，50-150MB）
2. vsa.rar                  （VSA 服务，10GB）
```

### 2. 用户使用流程

#### 步骤 1：解压主程序

用户将 `ai-live-0.0.1-win.zip` 解压到任意位置，例如：

```
D:\ai-live\                 # 用户自定义位置
├── ai-live.exe            # 主程序
├── resources/
└── extra/                 # 不包含 AI 服务
    ├── kill23456.bat
    └── 30s_mute.WAV
```

**解压后的文件夹结构**：
```
ai-live/
├── ai-live.exe             # 双击启动
├── chrome_100_percent.pak
├── chrome_200_percent.pak
├── d3dcompiler_47.dll
├── ffmpeg.dll
├── icudtl.dat
├── libEGL.dll
├── libGLESv2.dll
├── LICENSE.electron.txt
├── LICENSES.chromium.html
├── locales/
├── resources/
│   └── app/
│       ├── dist/
│       ├── node_modules/
│       └── package.json
├── resources.pak
├── snapshot_blob.bin
├── v8_context_snapshot.bin
├── vk_swiftshader.dll
├── vk_swiftshader_icd.json
├── vulkan-1.dll
└── extra/                  # AI 服务文件夹
    ├── kill23456.bat
    └── 30s_mute.WAV
```

#### 步骤 2：安装 VSA 服务

用户将 `vsa.rar` 解压到 `extra\vsa\` 文件夹：

```
ai-live/
└── extra/
    └── vsa/                # 解压 vsa.rar 到此处
        ├── vsa.exe
        ├── app.py
        ├── py310/
        ├── data/
        └── ...
```

**重要**：确保解压后文件直接在 `vsa\` 文件夹下，不要嵌套

#### 步骤 3：启动应用

1. 双击 `ai-live.exe`
2. 点击"AI服务器" → "本地语音服务" → "启动服务"
3. 选择主播和脚本
4. 点击"语音开播"

---

## ✅ 验证打包是否成功

### 方法 1：检查 zip 文件大小

打包后的 zip 文件大小：

- **正常大小**：50MB - 150MB
- **异常大小**：> 500MB（说明 AI 服务被打包了）

### 方法 2：解压后检查

解压 `ai-live-0.0.1-win.zip` 后，`extra/` 文件夹下**不应该有** vsa、f5tts、cosyvoice 文件夹。

---

## 📝 打包配置说明

### package.json 关键配置

```json
{
  "build": {
    "asar": false,
    "extraFiles": [
      {
        "from": "extra",
        "to": "extra",
        "filter": [
          "**/*",
          "!vsa/**",        // 完全排除 vsa
          "!f5tts/**",      // 完全排除 f5tts
          "!cosyvoice/**"   // 完全排除 cosyvoice
        ]
      }
    ],
    "win": {
      "icon": "build/icons/icon.ico",
      "target": [
        {
          "target": "zip",  // 打包成 zip（绿色版）
          "arch": ["x64"]
        }
      ]
    }
  }
}
```

### 为什么选择 zip（绿色版）？

1. **无需安装**
   - 解压即用，无需管理员权限
   - 可放在任意位置（U盘、移动硬盘等）

2. **便于部署**
   - 直接复制文件夹即可
   - 方便批量部署到多台电脑

3. **便于更新**
   - 替换新版本 zip 即可
   - 保留用户的 AI 服务和配置

4. **与 FLYAILIVE 一致**
   - 用户体验完全相同
   - 学习成本低

---

## 🐛 常见问题

### 问题 1：打包后是 exe 而不是 zip

**原因**：package.json 配置错误

**解决**：
1. 检查 `win.target` 是否为 `"zip"`
2. 确保没有 `nsis` 配置
3. 重新打包

### 问题 2：用户找不到安装目录

**原因**：绿色版没有固定安装位置

**解决**：告诉用户，解压后的文件夹就是"安装目录"

### 问题 3：用户双击 exe 没反应

**原因**：可能被杀毒软件拦截

**解决**：
1. 添加到杀毒软件白名单
2. 检查是否缺少必要的 DLL 文件

---

## 🎉 部署检查清单

打包前请确认：

- [x] `package.json` 中 `asar: false`
- [x] `package.json` 中 `win.target` 为 `"zip"`
- [x] `extraFiles.filter` 包含 `!vsa/**`、`!f5tts/**`、`!cosyvoice/**`

打包后请确认：

- [x] 生成了 `ai-live-0.0.1-win.zip` 文件
- [x] zip 文件大小：50MB - 150MB
- [x] 解压后 `extra/` 下没有 vsa、f5tts、cosyvoice 文件夹

用户部署后请确认：

- [x] 用户能够解压 zip 文件
- [x] 用户能够双击 ai-live.exe 启动
- [x] 用户能够按照说明解压 vsa.rar
- [x] 可以启动 VSA 服务
- [x] 可以正常开播

---

## 💡 用户使用流程总结

```
1. 用户解压 ai-live-0.0.1-win.zip 到任意位置
   ↓
2. 用户将 vsa.rar 解压到 extra\vsa\
   ↓
3. 用户双击 ai-live.exe 启动应用
   ↓
4. 应用自动检测 VSA 服务并启动
   ↓
5. 用户可以使用语音开播功能
```

---

## 📦 与 FLYAILIVE 对比

| 特性 | FLYAILIVE | 当前项目 | 状态 |
|------|-----------|---------|------|
| 打包格式 | zip | zip | ✅ 一致 |
| 部署方式 | 解压即用 | 解压即用 | ✅ 一致 |
| 安装方式 | 无需安装 | 无需安装 | ✅ 一致 |
| AI 服务 | 用户自行解压 | 用户自行解压 | ✅ 一致 |
| 安装包大小 | ~1GB | 50-150MB | ✅ 更小 |

---

## 📚 参考资料

- Electron Builder 文档：https://www.electron.build/
- 项目开发规范：`.claude/CLAUDE.md`

---

**最后更新**：2025-10-14
