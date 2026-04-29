<!-- 顶部脚本管理区 -->
<template>
  <div class="live-pane-header">
    <!-- 三列布局 -->
    <div class="three-column-layout">
      <!-- 第一列 -->
      <div class="column column-1">
        <!-- 脚本管理 -->
        <div class="item-row">
          <span class="item-label">脚本管理</span>
          <el-button
            type="success"
            size="small"
            round
            @click="openScriptManagement">
            [脚本]创建/编排/管理
          </el-button>
          <el-button
            type="success"
            size="small"
            round
            @click="openSegmentManagement">
            [片段]话术编写
          </el-button>
        </div>

        <!-- 直播状态 -->
        <div class="item-row">
          <span class="item-label">直播状态</span>
          <el-tag size="small">{{ liveStatusText }}</el-tag>
        </div>
      </div>

      <!-- 第二列 -->
      <div class="column column-2">
        <!-- 脚本选择 -->
        <div class="item-row">
          <span class="item-label">脚本选择</span>
          <el-select v-model="scriptId" placeholder="请选择脚本" size="small" style="width: 120px" @change="scriptChangeHandle">
            <el-option v-for="item in scriptList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-refresh"
            @click="refreshSegmentList"
            :loading="isRefreshing">
            刷新/切换
          </el-button>
        </div>

        <!-- 公屏弹幕 -->
        <div class="item-row">
          <span class="item-label">公屏弹幕</span>
          <el-tag size="small">未启用</el-tag>
        </div>
      </div>

      <!-- 第三列 -->
      <div class="column column-3">
        <!-- 开播时间 -->
        <div class="item-row">
          <span class="item-label">开播时间</span>
          <el-tag size="small">{{ startTimeText }}</el-tag>
        </div>

        <!-- AI服务器 -->
        <div class="item-row">
          <span class="item-label">AI服务器</span>
          <el-tag size="small" @click="openAiServerDialog" style="cursor: pointer">当前：自建AI服务器 - [点击设置]</el-tag>
        </div>
      </div>
    </div>

    <!-- 实时信息（单独一行） -->
    <div class="info-row">
      <span class="item-label">实时信息</span>
      <el-tag size="small" type="danger">无</el-tag>
    </div>

    <!-- 主播选择（同一行） -->
    <div class="host-row">
      <div class="host-item">
        <span class="item-label">主讲主播</span>
        <el-select v-model="mainHost" placeholder="请选择主讲主播" size="small" style="width: 150px" @change="onMainHostChange">
          <el-option
            v-for="model in hostModelList"
            :key="model.value"
            :label="model.label"
            :value="model.value" />
        </el-select>
      </div>
      <div class="host-item">
        <span class="item-label">互动主播</span>
        <el-select v-model="interactHost" placeholder="请选择互动主播" size="small" style="width: 150px" @change="onInteractHostChange">
          <el-option
            v-for="model in hostModelList"
            :key="model.value"
            :label="model.label"
            :value="model.value" />
        </el-select>
      </div>
      <div class="host-item">
        <span class="item-label">助理主播</span>
        <el-select v-model="assistantHost" placeholder="请选择助理主播" size="small" style="width: 150px" @change="onAssistantHostChange">
          <el-option
            v-for="model in hostModelList"
            :key="model.value"
            :label="model.label"
            :value="model.value" />
        </el-select>
      </div>
    </div>

    <!-- AI服务器设置对话框 -->
    <AiServerDialog :dialogVisible="showAiServerDialog" @close="closeAiServerDialog" @save="saveAiServerConfig"></AiServerDialog>

  </div>
</template>
<script>
import AiServerDialog from './components/aiServerDialog.vue'
import {ipcRenderer} from "electron";

export default {
  name: 'LivePaneHeader',
  components: {
    AiServerDialog
  },
  data() {
    return {
      liveContext: null,
      isCurrentHost: false,
      script: '',
      startTime: '',
      playDuration: 0,
      mainHost: '',          // 主讲主播
      interactHost: '',      // 互动主播
      assistantHost: '',     // 助理主播
      isPublicScreen: false,

      scriptList: [],
      scriptId: '',
      isRefreshing: false,
      showAiServerDialog: false,
      hostModelList: [],     // 主播模型列表

      // 直播状态
      liveStatus: {
        isPlaying: false,
        playedCount: 0,
        loadedCount: 0,
        startTime: null
      },
      statusUpdateTimer: null  // 状态更新定时器
    }
  },
  computed: {
    playDurationText() {
      //需要将playDuration转换为时分秒
      const hours = Math.floor(this.playDuration / 3600)
      const minutes = Math.floor((this.playDuration % 3600) / 60)
      const seconds = this.playDuration % 60
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    },
    liveStatusText() {
      if (this.liveStatus.isPlaying) {
        return `正在开播: 已播放${this.liveStatus.playedCount}条/已加载${this.liveStatus.loadedCount}条`
      }
      return '未开播'
    },
    startTimeText() {
      if (!this.liveStatus.startTime) {
        return '无'
      }
      const date = new Date(this.liveStatus.startTime)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      const seconds = date.getSeconds().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  },
  created() {
    this.getScriptList()
    this.loadHostModels()  // 加载主播模型列表
  },
  mounted() {
    // 启动状态更新定时器（每500ms更新一次）
    this.statusUpdateTimer = setInterval(() => {
      this.updateLiveStatus()
    }, 500)
  },
  beforeDestroy() {
    // 清除定时器
    if (this.statusUpdateTimer) {
      clearInterval(this.statusUpdateTimer)
      this.statusUpdateTimer = null
    }
  },
  methods: {
     // 初始化
     init(liveContext) {
      this.liveContext = liveContext
    },
    // 更新直播状态
    updateLiveStatus() {
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        const status = this.liveContext.$refs.livePaneSegmentTable.getLiveStatus()
        this.liveStatus = status
      }
    },
    // 获取脚本列表
    getScriptList() {
      this.$http.get('/apps/live-script/all').then(({data}) => {
        this.scriptList = data.list
        // 设置默认选中的脚本
        this.setDefaultScript()
      })
    },
    // 加载主播模型列表
    async loadHostModels() {
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('scan-host-models')

        if (result.success) {
          this.hostModelList = result.models || []
          const count = result.models ? result.models.length : 0

          if (count > 0) {
            // 加载完模型后，恢复上次的主播选择
            this.restoreHostSelections()
          } else {
            this.$message.warning('未找到可用的主播模型，请先上传模型文件')
          }
        } else {
          console.error('加载主播模型失败:', result.error)
          this.$message.error('加载主播模型失败: ' + result.error)
        }
      } catch (error) {
        console.error('加载主播模型时出错:', error)
        this.$message.error('加载主播模型失败: ' + error.message)
      }
    },
    // 恢复主播选择
    restoreHostSelections() {
      // 从 localStorage 恢复上次的选择
      const savedMainHost = localStorage.getItem('selectedMainHost')
      const savedInteractHost = localStorage.getItem('selectedInteractHost')
      const savedAssistantHost = localStorage.getItem('selectedAssistantHost')

      // 验证保存的值是否在当前模型列表中
      if (savedMainHost && this.hostModelList.some(m => m.value === savedMainHost)) {
        this.mainHost = savedMainHost
      }
      if (savedInteractHost && this.hostModelList.some(m => m.value === savedInteractHost)) {
        this.interactHost = savedInteractHost
      }
      if (savedAssistantHost && this.hostModelList.some(m => m.value === savedAssistantHost)) {
        this.assistantHost = savedAssistantHost
      }
    },
    // 主讲主播变更
    onMainHostChange(value) {
      localStorage.setItem('selectedMainHost', value)
    },
    // 互动主播变更
    onInteractHostChange(value) {
      localStorage.setItem('selectedInteractHost', value)
    },
    // 助理主播变更
    onAssistantHostChange(value) {
      localStorage.setItem('selectedAssistantHost', value)
    },
    // 设置默认脚本
    setDefaultScript() {
      if (!this.scriptList || this.scriptList.length === 0) {
        return
      }

      // 从localStorage读取上次选择的脚本ID
      const lastScriptId = localStorage.getItem('lastSelectedScriptId')

      if (lastScriptId) {
        // 检查上次选择的脚本是否还存在于列表中
        const exists = this.scriptList.some(item => item.id == lastScriptId)
        if (exists) {
          this.scriptId = parseInt(lastScriptId)
        } else {
          // 如果不存在，选择第一条
          this.scriptId = this.scriptList[0].id
          localStorage.setItem('lastSelectedScriptId', this.scriptId)
        }
      } else {
        // 如果没有历史记录，选择第一条
        this.scriptId = this.scriptList[0].id
        localStorage.setItem('lastSelectedScriptId', this.scriptId)
      }

      // 触发脚本变更事件，加载片段列表
      this.$nextTick(() => {
        this.scriptChangeHandle(this.scriptId)
      })
    },
    // 刷新片段列表
    refreshSegmentList() {
      if (!this.scriptId) {
        this.$message.warning('请先选择脚本')
        return
      }

      this.isRefreshing = true
      // 调用片段列表组件的刷新方法
      this.liveContext.$refs.livePaneSegmentTable.refreshSegmentList(this.scriptId)
        .then(() => {
          this.$message.success('片段列表刷新成功')
        })
        .catch(() => {
          this.$message.error('刷新失败,请重试')
        })
        .finally(() => {
          this.isRefreshing = false
        })
    },
    // 脚本选择
    scriptChangeHandle(scriptId) {
      // 保存当前选择的脚本ID到localStorage
      localStorage.setItem('lastSelectedScriptId', scriptId)

      // 刷新片段列表
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.loadSegmentList(scriptId)
      }
    },

    // 添加开播时长
    addPlayDuration() {
      this.playDuration = this.playDuration + 1
    },
    // 重置开播时长
    resetPlayDuration() {
      this.playDuration = 0
    },
    // 打开脚本管理页面
    openScriptManagement() {
      let data = {
        url: "/live-script",
        resizable: true,
        isExternalUrl: true
      };
      ipcRenderer.invoke("open-win", data);
    },
    // 打开片段管理页面
    openSegmentManagement() {
      let data = {
        url: "/live-segment",
        resizable: true,
        isExternalUrl: true
      };
      ipcRenderer.invoke("open-win", data);
    },
    // 打开AI服务器设置对话框
    openAiServerDialog() {
      this.showAiServerDialog = true
    },
    // 关闭AI服务器设置对话框
    closeAiServerDialog() {
      this.showAiServerDialog = false
    },
    // 保存AI服务器配置
    saveAiServerConfig(config) {
      // 将配置保存到localStorage
      localStorage.setItem('aiServerConfig', JSON.stringify(config))
    }
  }
}
</script>
<style scoped lang="scss">
.live-pane-header {
  width: 100%;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  box-sizing: border-box;

  // 通用标签样式 - 白底边框
  .section-label {
    display: inline-block;
    padding: 4px 12px;
    background: #fff;
    color: #333;
    font-size: 14px;
    font-weight: 500;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    margin-right: 8px;
  }

  // 通用文字标签
  .item-label {
    font-size: 14px;
    color: #333;
    white-space: nowrap;
    font-weight: 500;
    margin-right: 8px;
  }

  // 通用状态文字
  .status-text {
    color: #333;
    font-size: 14px;
  }

  // 三列布局
  .three-column-layout {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    gap: 24px;

    .column {
      display: flex;
      flex-direction: column;
      gap: 12px;
      flex: 1 1 0;
      min-width: 0; // 允许内容缩放

      .item-row {
        display: flex;
        align-items: center;
        gap: 8px;
        min-height: 32px;
      }
    }
  }

  // 实时信息行
  .info-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    min-height: 32px;
  }

  // 主播选择行
  .host-row {
    display: flex;
    align-items: center;
    gap: 24px;

    .host-item {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1 1 0;
      min-width: 0; // 允许内容缩放
    }
  }
}
</style>
