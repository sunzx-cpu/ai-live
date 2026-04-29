<!-- 调试面板 -->
<template>
  <div class="debug-panel" v-if="visible">
    <div class="debug-header">
      <span>🔧 调试面板</span>
      <el-button type="text" size="mini" @click="togglePanel">{{ collapsed ? '展开' : '收起' }}</el-button>
      <el-button type="text" size="mini" @click="clearLogs">清空</el-button>
    </div>

    <div class="debug-content" v-show="!collapsed">
      <!-- 播放状态 -->
      <div class="status-section">
        <h4>播放状态</h4>
        <div class="status-item">
          <span class="label">正在播放:</span>
          <span class="value" :class="status.isPlaying ? 'playing' : 'stopped'">
            {{ status.isPlaying ? '是' : '否' }}
          </span>
        </div>
        <div class="status-item">
          <span class="label">队列长度:</span>
          <span class="value">{{ status.queueLength }}</span>
        </div>
        <div class="status-item">
          <span class="label">当前主播:</span>
          <span class="value">{{ status.currentSpeaker || '未选择' }}</span>
        </div>
        <div class="status-item">
          <span class="label">服务状态:</span>
          <span class="value" :class="status.serviceRunning ? 'playing' : 'stopped'">
            {{ status.serviceRunning ? '运行中' : '已停止' }}
          </span>
        </div>
      </div>

      <!-- 日志区域 -->
      <div class="logs-section">
        <h4>实时日志</h4>
        <div class="logs-container" ref="logsContainer">
          <div
            v-for="(log, index) in logs"
            :key="index"
            class="log-item"
            :class="log.type">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DebugPanel',
  data() {
    return {
      visible: true,         // 面板可见性
      collapsed: false,      // 是否收起
      logs: [],              // 日志列表
      maxLogs: 50,           // 最大日志数量
      status: {
        isPlaying: false,
        queueLength: 0,
        currentSpeaker: '',
        serviceRunning: false
      }
    }
  },
  methods: {
    /**
     * 添加日志
     * @param {String} message - 日志消息
     * @param {String} type - 日志类型: info, success, warning, error
     */
    addLog(message, type = 'info') {
      const now = new Date()
      const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`

      this.logs.push({
        time,
        message,
        type
      })

      // 限制日志数量
      if (this.logs.length > this.maxLogs) {
        this.logs.shift()
      }

      // 自动滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },

    /**
     * 更新播放状态
     */
    updateStatus(newStatus) {
      this.status = { ...this.status, ...newStatus }
    },

    /**
     * 清空日志
     */
    clearLogs() {
      this.logs = []
    },

    /**
     * 切换面板展开/收起
     */
    togglePanel() {
      this.collapsed = !this.collapsed
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      if (this.$refs.logsContainer) {
        this.$refs.logsContainer.scrollTop = this.$refs.logsContainer.scrollHeight
      }
    }
  }
}
</script>

<style scoped lang="scss">
.debug-panel {
  position: fixed;
  bottom: 0;
  right: 0;
  width: 400px;
  max-height: 500px;
  background: rgba(0, 0, 0, 0.9);
  border: 1px solid #444;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.3);
  z-index: 9999;
  overflow: hidden;

  .debug-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: #1a1a1a;
    border-bottom: 1px solid #444;
    color: #fff;
    font-size: 13px;
    font-weight: 500;

    ::v-deep .el-button {
      color: #409eff;
      font-size: 12px;
      padding: 0 8px;

      &:hover {
        color: #66b1ff;
      }
    }
  }

  .debug-content {
    max-height: 450px;
    overflow-y: auto;

    // 状态区域
    .status-section {
      padding: 12px;
      border-bottom: 1px solid #333;

      h4 {
        margin: 0 0 8px 0;
        color: #67c23a;
        font-size: 13px;
        font-weight: 500;
      }

      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 4px 0;
        font-size: 12px;

        .label {
          color: #aaa;
        }

        .value {
          color: #fff;
          font-weight: 500;

          &.playing {
            color: #67c23a;
          }

          &.stopped {
            color: #f56c6c;
          }
        }
      }
    }

    // 日志区域
    .logs-section {
      padding: 12px;

      h4 {
        margin: 0 0 8px 0;
        color: #409eff;
        font-size: 13px;
        font-weight: 500;
      }

      .logs-container {
        max-height: 250px;
        overflow-y: auto;
        font-family: 'Courier New', monospace;
        font-size: 11px;

        // 滚动条样式
        &::-webkit-scrollbar {
          width: 4px;
        }

        &::-webkit-scrollbar-track {
          background: #1a1a1a;
        }

        &::-webkit-scrollbar-thumb {
          background: #555;
          border-radius: 2px;

          &:hover {
            background: #777;
          }
        }

        .log-item {
          padding: 4px 8px;
          margin-bottom: 2px;
          border-radius: 3px;
          display: flex;
          gap: 8px;

          .log-time {
            color: #888;
            flex-shrink: 0;
          }

          .log-message {
            flex: 1;
            word-break: break-all;
          }

          &.info {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }

          &.success {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }

          &.warning {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
          }

          &.error {
            background: rgba(245, 108, 108, 0.1);
            color: #f56c6c;
          }
        }
      }
    }
  }
}
</style>
