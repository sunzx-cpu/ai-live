# Electron-Vue-template

## 介绍

- 这是一个基于 webpack5 和 vue2/3 的 electron 快速上手框架
- 简单的 demo 示例，包含以上代码示例具体详见文档：[中文在线文档](https://umbrella22.github.io/electron-vue-template-doc/)，[国内访问地址](https://zh-sky.gitee.io/electron-vue-template-doc/)。

> ### **请确保您的 node 环境是大于或等于 14**

#### 如何安装

```bash
npm config edit
# 该命令会打开npm的配置文件，请在空白处添加，此操作是配置淘宝镜像。
# registry=https://registry.npmmirror.com
# electron_mirror=https://cdn.npmmirror.com/binaries/electron/
# electron_builder_binaries_mirror=https://npmmirror.com/mirrors/electron-builder-binaries/
# 然后关闭该窗口，重启命令行.

# npm 启动
npm install
npm run dev

# 使用yarn安装
yarn or yarn install

# 启动之后，会在9080端口监听
yarn dev

# build命令在不同系统环境中，需要的的不一样，需要自己根据自身环境进行配置
yarn build

```

---

