<template>
  <div id="wrapper">
    <!-- 背景效果 -->
    <div class="background-container">
      <div class="wave wave-1"></div>
      <div class="wave wave-2"></div>
      <div class="wave wave-3"></div>
      <div class="wave wave-4"></div>
    </div>

    <!-- 主内容区域 -->
    <div class="content-container">
      <!-- Electron Logo -->
      <div class="electron-logo">
        <div class="electron-orbit orbit-1"></div>
        <div class="electron-orbit orbit-2"></div>
        <div class="electron-orbit orbit-3"></div>
        <div class="electron-particle particle-1"></div>
        <div class="electron-particle particle-2"></div>
        <div class="electron-particle particle-3"></div>
      </div>

      <!-- 应用标题 -->
      <h1 class="app-title">
        AI直播 <span class="react-text">数字人</span> 和 <span class="typescript-text">AI大模型</span>
      </h1>

      <!-- 提示文字 -->
      <p class="hint-text">AI智能主播为你提供个性化服务体验</p>

      <!-- 检测版本更新按钮 -->
      <div class="update-button">
        <el-button round type="primary" @click="checkUpdate">检测版本更新</el-button>
      </div>
    </div>

    <!-- 更新提示对话框 -->
    <el-dialog
        :before-close="handleClose"
        :visible.sync="dialogVisible"
        width="480px"
        custom-class="update-dialog"
        center
        :close-on-click-modal="false"
        :close-on-press-escape="false"
    >
      <!-- 自定义标题区域 -->
      <template slot="title">
        <div class="dialog-header">
          <i class="el-icon-download download-icon"></i>
          <span class="dialog-title">更新提示</span>
        </div>
      </template>

      <!-- 对话框内容 -->
      <div class="dialog-content">
        <p class="downloading-text">正在下载新版本安装包...</p>
        <div class="progress-section">
          <el-progress
              :percentage="percentage"
              :stroke-width="8"
              :color="colors"
              :status="progressStatus"
          ></el-progress>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ipcRenderer } from "electron";

export default {
  name: "landing-page",
  data() {
    return {
      percentage: 0,
      colors: [
        {color: "#f56c6c", percentage: 20},
        {color: "#e6a23c", percentage: 40},
        {color: "#6f7ad3", percentage: 60},
        {color: "#1989fa", percentage: 80},
        {color: "#5cb87a", percentage: 100},
      ],
      dialogVisible: false,
      progressStatus: null,
    };
  },
  created() {
    console.log("环境打印示例");
    console.log("__lib路径", __lib);
    console.log("环境变量", process.env.userConfig);

    ipcRenderer.on("update-msg", (event, age) => {
      console.log("update-msg", age);
      switch (age.state) {
        case -1:
          const msgdata = {
            title: "发生错误",
            message: age.msg,
          };
          this.dialogVisible = false;
          ipcRenderer.invoke("open-errorbox", msgdata);
          break;
        case 0:
          this.$message("正在检查更新");
          break;
        case 1:
          this.$confirm(`当前版本：v${age.msg.oldVersion}，最新版本：v${age.msg.version}，是否立即下载并更新？`, '提示', {
            confirmButtonText: '立即更新',
            cancelButtonText: '稍后再说',
            type: 'warning'
          }).then(() => {
            this.dialogVisible = true;
            ipcRenderer.invoke("confirm-downloadUpdate")
          })
          break;
        case 2:
          this.$message({type: "success", message: "无新版本"});
          break;
        case 3:
          this.percentage = age.msg.percent.toFixed(1);
          break;
        case 4:
          this.progressStatus = "success";
          this.$confirm(`更新下载完成！是否现在重启应用？`, '提示', {
            confirmButtonText: '立即重启',
            cancelButtonText: '稍后再说',
            type: 'info'
          }).then(() => {
            ipcRenderer.invoke("confirm-update");
          })
          break;
        default:
          break;
      }
    });
  },
  methods: {
    checkUpdate() {
      ipcRenderer.invoke("check-update").then((res) => {
        console.log("启动检查");
      });
    },
    handleClose() {
      this.dialogVisible = false;
    },
  },
  destroyed() {
    console.log("销毁了哦");
    ipcRenderer.removeAllListeners("update-msg");
  },
};
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

#wrapper {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #0c0e1a 0%, #1a1f3a 100%);
  color: #fff;
  height: calc(100vh - 78px);
  width: calc(100vw - 150px);
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.content-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 2;
}

.background-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.electron-logo {
  position: relative;
  width: 150px;
  height: 150px;
  margin-bottom: 30px;
  z-index: 2;
}

.electron-orbit {
  position: absolute;
  border: 2px solid #64b5f6;
  border-radius: 50%;
}

.orbit-1 {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.orbit-2 {
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
  transform: rotate(60deg);
}

.orbit-3 {
  width: 40%;
  height: 40%;
  top: 30%;
  left: 30%;
  transform: rotate(120deg);
}

.electron-particle {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #64b5f6;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  margin-top: -6px;
  margin-left: -6px;
  box-shadow: 0 0 10px #64b5f6;
}

.particle-1 {
  transform: translateX(75px);
  animation: pulse 2s infinite;
}

.particle-2 {
  transform: rotate(60deg) translateX(52.5px) rotate(-60deg);
  animation: pulse 2s infinite 0.66s;
}

.particle-3 {
  transform: rotate(120deg) translateX(30px) rotate(-120deg);
  animation: pulse 2s infinite 1.33s;
}

.app-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 15px;
  text-align: center;
  z-index: 2;
  line-height: 1.4;
}

.react-text {
  color: #61dafb;
}

.typescript-text {
  color: #3178c6;
}

.hint-text {
  font-size: 14px;
  color: #aaa;
  margin-bottom: 40px;
  z-index: 2;
  text-align: center;
}

.update-button {
  z-index: 2;
  position: relative;
  margin-bottom: 20px;
}

/* 背景效果 */
.wave {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.wave-1 {
  width: 800px;
  height: 800px;
  border: 1px solid #0d47a1;
  top: -300px;
  right: -200px;
  animation: pulse 8s infinite ease-in-out;
}

.wave-2 {
  width: 600px;
  height: 600px;
  border: 1px solid #1565c0;
  top: -200px;
  right: -100px;
  animation: pulse 7s infinite ease-in-out 1s;
}

.wave-3 {
  width: 400px;
  height: 400px;
  border: 1px solid #1976d2;
  top: -100px;
  right: 0;
  animation: pulse 6s infinite ease-in-out 2s;
}

.wave-4 {
  width: 300px;
  height: 300px;
  border: 1px solid #2196f3;
  top: 0;
  right: 100px;
  animation: pulse 5s infinite ease-in-out 3s;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.15;
  }
}

/* 更新对话框自定义样式 */
.update-dialog .el-dialog {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.update-dialog .el-dialog__header {
  padding: 16px 20px 12px;
  border-bottom: 1px solid #e8e8e8;
}

.dialog-header {
  display: flex;
  align-items: center;
}

.download-icon {
  font-size: 20px;
  margin-right: 8px;
  background: linear-gradient(135deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.update-dialog .el-dialog__body {
  padding: 20px;
}

.dialog-content {
  padding: 0 8px;
}

.downloading-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
  line-height: 1.5;
}

.progress-section {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.progress-section .el-progress {
  flex: 1;
}

.update-dialog .el-dialog__footer {
  padding: 12px 20px 16px;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}
</style>