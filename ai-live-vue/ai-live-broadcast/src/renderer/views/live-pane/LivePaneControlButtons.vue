<!-- 直播控制按钮区 -->
<template>
  <div class="live-pane-control-buttons">
    <!-- 第一行：主要控制按钮 -->
    <div class="button-card">
      <el-row :gutter="12" type="flex" align="middle">
        <el-button size="small" type="primary" :disabled="isPlaying || isStopping" @click="startPlayHandle">语音开播</el-button>
        <!-- <el-button size="small" type="primary">数字人开播</el-button> -->
        <el-button size="small" type="info" :disabled="!isPlaying" @click="stopPlayHandle">停播</el-button>
        <el-button
          size="small"
          :type="isPause ? 'primary' : 'warning'"
          :disabled="!isPlaying"
          @click="togglePauseHandle">
          {{ isPause ? '继续播放' : '暂停播放' }}
        </el-button>
        <el-button size="small" type="success" @click="anchorControlHandle">主播管控</el-button>
        <el-button size="small" type="success" @click="gptModelHandle">GPT大模型</el-button>
        <el-button size="small" type="danger" :disabled="!isPlaying || isStopping" @click="temporarySpeechHandle">临时话术</el-button>
        <el-button size="small" type="danger" @click="scheduledSpeechHandle">定时话术</el-button>
        <el-button size="small" type="danger" @click="homophoneHandle">同音词设置</el-button>
      </el-row>
    </div>

    <!-- 第二行：扩展功能按钮（待开发） -->
    <div class="button-card second-row-buttons">
      <!-- 视频播放器：开播时选择视频时才可启用 -->
      <el-tooltip content="在开播的时选择视频时才可启用" placement="top">
        <el-button size="small" disabled>视频播放器</el-button>
      </el-tooltip>

      <!-- 纯互动式：勾选后AI不播报话术，仅触发条件时播报 -->
      <el-tooltip
        content="勾选后去开播,AI将不会语音播报话术,仅在触发条件时进行播报.有效范围: 直播互动,临时话术,定时话术."
        placement="top">
        <el-checkbox v-model="pureInteractionMode" @change="handlePureInteractionModeChange">纯互动式</el-checkbox>
      </el-tooltip>

      <!-- GPT实时润色改写话术 -->
      <el-tooltip
        content="勾选后开播时每句话术会先通过GPT润色改写，让话术更自然流畅"
        placement="top">
        <el-checkbox v-model="gptRewriteMode" @change="handleGptRewriteModeChange">GPT实时润色改写话术</el-checkbox>
      </el-tooltip>

      <el-button size="small" type="text" disabled style="color: #909399;">[生成片段结构]</el-button>
      <el-button size="small" type="text" disabled style="color: #909399;">[音画同步-片段关联视频]</el-button>
      <el-button size="small" type="text" disabled style="color: #909399;">[音画同步-话术关联视频]</el-button>
      <el-button size="small" type="text" disabled style="color: #909399;">[导入商品]</el-button>
      <el-button size="small" type="text" disabled style="color: #909399;">[商品模板]</el-button>
      <el-button size="small" type="text" disabled style="color: #909399;">[清空关联]</el-button>
    </div>

    <!-- 主播管控组组件 -->
    <AnchorControlDialog
      ref="anchorControlDialog"
      @config-updated="handleConfigUpdated"
      @play-bg-music="handlePlayBgMusic"
      @stop-bg-music="handleStopBgMusic"></AnchorControlDialog>

    <!--  GPT大模型  -->
    <GptModelDialog
      ref="gptModelDialog"
      :visible="gptModelDialogVisible"
      :isPlaying="isPlaying"
      @close="gptModelDialogVisible = false"
      @settings-saved="handleGptSettingsSaved">
    </GptModelDialog>
    <!-- 开播确认对话框 -->
    <StartPlayDialog ref="startPlayDialog" @confirm="confirmStartPlay"></StartPlayDialog>

    <!-- 音频加载进度对话框 -->
    <AudioLoadingDialog
      :dialogVisible="showAudioLoading"
      :loadedCount="loadedCount"
      :totalCount="totalCount"
      @close="closeAudioLoadingDialog"></AudioLoadingDialog>

    <!-- 临时话术对话框 -->
    <TemporarySpeechDialog
      ref="temporarySpeechDialog"
      @insert="handleTemporarySpeechInsert"></TemporarySpeechDialog>

    <!-- 定时话术对话框 -->
    <ScheduledSpeechDialog
      ref="scheduledSpeechDialog"
      @insert="handleScheduledSpeechInsert"
      @config-updated="handleScheduledSpeechConfigUpdate"></ScheduledSpeechDialog>

    <!-- 同音词设置对话框 -->
    <HomophoneDialog ref="homophoneDialog"></HomophoneDialog>
  </div>
</template> 
<script>
import AnchorControlDialog from './components/anchorControlDialog.vue'
import GptModelDialog from './components/gptModelDialog.vue'
import StartPlayDialog from './components/startPlayDialog.vue'
import AudioLoadingDialog from './components/audioLoadingDialog.vue'
import TemporarySpeechDialog from './components/temporarySpeechDialog.vue'
import ScheduledSpeechDialog from './components/scheduledSpeechDialog.vue'
import HomophoneDialog from './components/homophoneDialog.vue'

export default {
  name: 'LivePaneControlButtons',
  components:{
    AnchorControlDialog,
    GptModelDialog,
    StartPlayDialog,
    AudioLoadingDialog,
    TemporarySpeechDialog,
    ScheduledSpeechDialog,
    HomophoneDialog
  },
  data() {
    return {
      isPlaying: false,
      isPause: false,
      autoPopup: false,
      autoPopupWithProduct: false,
      productBigModel: false,
      liveContext: null,
      showAudioLoading: false,
      loadedCount: 0,
      totalCount: 0,
      // 第二行功能开关
      pureInteractionMode: false,    // 纯互动式模式
      gptRewriteMode: false,          // GPT实时润色改写话术
      // 对话框显示控制
      gptModelDialogVisible: false    // GPT大模型对话框
    }
  },
  computed: {
    // 获取停播状态
    isStopping() {
      return this.liveContext && this.liveContext.$refs.livePaneSegmentTable
        ? this.liveContext.$refs.livePaneSegmentTable.isStopping
        : false
    },
    // 获取播放状态
    isPlaying() {
      return this.liveContext && this.liveContext.$refs.livePaneSegmentTable
        ? this.liveContext.$refs.livePaneSegmentTable.isPlayingSession
        : false
    }
  },
  mounted() {
    // 从localStorage加载纯互动式配置
    const savedMode = localStorage.getItem('pureInteractionMode')
    if (savedMode !== null) {
      this.pureInteractionMode = savedMode === 'true'
    }

    // 从localStorage加载GPT实时润色配置
    const savedGptRewriteMode = localStorage.getItem('gptPolishEnabled')
    if (savedGptRewriteMode !== null) {
      this.gptRewriteMode = JSON.parse(savedGptRewriteMode)
    }
  },
  methods: {
    // 初始化
    init(liveContext) {
      this.liveContext = liveContext
    },
    // 开播（弹出确认对话框）
    async startPlayHandle() {
      if (!this.liveContext.$refs.livePaneHeader.scriptId) {
        this.$message.warning('请先选择脚本')
        return
      }

      // 检查 VSA 服务是否已安装
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('check-vsa-installed')
        console.log('[LivePane] VSA 安装检查结果:', result)

        if (!result.installed) {
          // VSA 未安装，显示友好提示
          this.$confirm(
            result.message || 'VSA 服务未安装，请将 vsa.rar 解压到 extra/vsa/ 文件夹',
            'VSA 服务未安装',
            {
              confirmButtonText: '打开安装目录',
              cancelButtonText: '取消开播',
              type: 'warning',
              dangerouslyUseHTMLString: true,
              message: `
                <div style="line-height: 1.8;">
                  <p><strong>VSA 服务未安装</strong></p>
                  <p style="color: #606266; margin: 10px 0;">
                    ${result.message || 'VSA 服务未安装，请将 vsa.rar 解压到 extra/vsa/ 文件夹'}
                  </p>
                  <p style="color: #909399; font-size: 13px;">
                    <strong>安装步骤：</strong><br/>
                    1. 解压 vsa.rar 到 extra/vsa/ 目录<br/>
                    2. 确保 vsa.exe 和 app.py 文件存在<br/>
                    3. 重新点击开播
                  </p>
                  <p style="color: #909399; font-size: 13px; margin-top: 10px;">
                    <strong>安装路径：</strong><br/>
                    <code style="background: #f5f7fa; padding: 2px 6px; border-radius: 3px;">
                      ${result.vsaPath || 'extra/vsa/'}
                    </code>
                  </p>
                </div>
              `
            }
          ).then(() => {
            // 用户点击"打开安装目录"
            ipcRenderer.invoke('open-models-folder', { folderType: 'extra' })
          }).catch(() => {
            // 用户点击"取消开播"
            console.log('[LivePane] 用户取消开播')
          })
          return
        }

        // VSA 已安装，继续开播流程
        console.log('[LivePane] VSA 已安装，显示开播确认对话框')
      } catch (error) {
        console.error('[LivePane] 检查 VSA 安装状态失败:', error)
        this.$message.error('检查 VSA 服务状态失败')
        return
      }

      // 显示开播确认对话框
      this.$refs.startPlayDialog.dialogVisible = true
    },
    // 确认开播
    confirmStartPlay(config) {
      const scriptId = this.liveContext.$refs.livePaneHeader.scriptId

      // 显示加载进度对话框
      this.showAudioLoading = true
      this.loadedCount = 0
      this.totalCount = 0

      // 定义加载进度回调
      const onLoadProgress = (loaded, total) => {
        this.loadedCount = loaded
        this.totalCount = total
      }

      // 定义加载完成回调
      const onLoadComplete = () => {
        this.showAudioLoading = false
        this.isPlaying = true
        this.isPause = false
        this.$message.success('开播成功')
      }

      // 传递纯互动式模式给播放器
      if (this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.updatePureInteractionMode(this.pureInteractionMode)
      }

      // 开启直播，传入进度回调
      this.liveContext.$refs.livePaneSegmentTable.startPlayHandle(scriptId, onLoadProgress, onLoadComplete)
      // 重置开播时长
      this.liveContext.$refs.livePaneHeader.resetPlayDuration()
    },
    // 停播
    stopPlayHandle() {
      // 调用 LivePaneSegmentTable 的停播方法
      this.liveContext.$refs.livePaneSegmentTable.stopPlayHandle()

      // 注意：不要立即设置 isPlaying = false
      // 因为停播是延迟响应的，需要等待当前音频播放完
      // 停播完成后会通过回调来更新状态
    },
    // 切换暂停/继续播放
    togglePauseHandle() {
      if (this.isPause) {
        // 当前是暂停状态，点击后继续播放
        this.liveContext.$refs.livePaneSegmentTable.resumePlayHandle()
        this.isPause = false
      } else {
        // 当前是播放状态，点击后暂停播放（延迟响应，等待当前音频完成）
        const onPauseComplete = () => {
          this.isPause = true
        }
        this.liveContext.$refs.livePaneSegmentTable.pausePlayHandle(onPauseComplete)
      }
    },
    // 主播管控
    anchorControlHandle(){
      this.$refs.anchorControlDialog.dialogVisible = true
    },
    // 处理播放背景音乐事件
    handlePlayBgMusic(config) {
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.playBackgroundMusic(config)
      }
    },
    // 处理停止背景音乐事件
    handleStopBgMusic() {
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.stopBackgroundMusic()
      }
    },
    // 处理配置更新事件
    handleConfigUpdated(config) {
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.updateAnchorControlConfig(config)
      }
    },
    // GPT大模型
    gptModelHandle(){
      this.gptModelDialogVisible = true
    },
    // GPT设置保存后的回调
    handleGptSettingsSaved(settings) {
      // 同步 GPT 润色开关状态
      this.gptRewriteMode = settings.polishEnabled

      // 通知 LivePaneSegmentTable 重新加载 GPT 配置
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.loadGPTConfig()
        console.log('[LivePaneControlButtons] GPT配置已更新，已通知 LivePaneSegmentTable 重新加载')
      }
    },
    // GPT实时润色开关变化
    handleGptRewriteModeChange(value) {
      // 保存到 localStorage
      localStorage.setItem('gptPolishEnabled', JSON.stringify(value))

      // 通知 LivePaneSegmentTable 重新加载 GPT 配置
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.loadGPTConfig()
        this.$message.success(value ? 'GPT实时润色已启用' : 'GPT实时润色已关闭')
      }
    },
    // 临时话术
    temporarySpeechHandle() {
      this.$refs.temporarySpeechDialog.dialogVisible = true
    },
    // 定时话术
    scheduledSpeechHandle() {
      this.$refs.scheduledSpeechDialog.dialogVisible = true
    },
    // 同音词设置
    homophoneHandle() {
      this.$refs.homophoneDialog.dialogVisible = true
    },
    // 处理临时话术插入
    async handleTemporarySpeechInsert(data) {
      if (!this.liveContext || !this.liveContext.$refs.livePaneSegmentTable) {
        this.$message.warning('播放器未初始化')
        return
      }

      try {
        // 调用 LivePaneSegmentTable 的插入临时话术方法
        await this.liveContext.$refs.livePaneSegmentTable.insertTemporarySpeech(data.text, data.type)
      } catch (error) {
        console.error('[LivePaneControlButtons] 临时话术插入失败:', error)
        this.$message.error('插入临时话术失败: ' + error.message)
      }
    },
    // 处理定时话术插入
    async handleScheduledSpeechInsert(data) {
      if (!this.liveContext || !this.liveContext.$refs.livePaneSegmentTable) {
        console.warn('[LivePaneControlButtons] 播放器未初始化，跳过定时话术插入')
        return
      }

      try {
        // 调用 LivePaneSegmentTable 的插入定时话术方法
        await this.liveContext.$refs.livePaneSegmentTable.insertScheduledSpeech(data.text, data.type)
      } catch (error) {
        console.error('[LivePaneControlButtons] 定时话术插入失败:', error)
      }
    },
    // 处理定时话术配置更新
    handleScheduledSpeechConfigUpdate(config) {
      // 配置已在 scheduledSpeechDialog 组件内部保存到 localStorage
      // 这里可以添加额外的处理逻辑（如果需要）
    },
    // 处理纯互动式模式变化
    handlePureInteractionModeChange(value) {
      // 保存到 localStorage
      localStorage.setItem('pureInteractionMode', value)
      // 如果播放器已初始化，更新播放器配置
      if (this.liveContext && this.liveContext.$refs.livePaneSegmentTable) {
        this.liveContext.$refs.livePaneSegmentTable.updatePureInteractionMode(value)
      }
      this.$message.success(value ? '已启用纯互动式模式' : '已关闭纯互动式模式')
    },
    // 关闭音频加载对话框
    closeAudioLoadingDialog() {
      this.showAudioLoading = false
    }
  }
}
</script>
<style scoped lang="scss">
.live-pane-control-buttons {
  width: 100%;
  margin-bottom: 12px;

  // 卡片样式（应用到每一行）
  .button-card {
    background: #fff;
    border-radius: 4px;
    padding: 12px 16px;
    box-sizing: border-box;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  // 第二行按钮容器 - 支持自动换行
  .second-row-buttons {
    display: flex;
    flex-wrap: wrap;  // 允许换行
    align-items: center;
    gap: 8px;  // 统一间距

    // 响应式优化：小屏幕下的额外调整
    @media screen and (max-width: 1366px) {
      gap: 6px;  // 缩小间距

      // 按钮字体稍微缩小
      ::v-deep .el-button--small {
        padding: 7px 12px;
        font-size: 13px;
      }

      ::v-deep .el-checkbox {
        font-size: 13px;
      }
    }

    // 极小屏幕优化
    @media screen and (max-width: 1024px) {
      gap: 4px;

      ::v-deep .el-button--small {
        padding: 6px 10px;
        font-size: 12px;
      }

      ::v-deep .el-checkbox {
        font-size: 12px;
      }
    }
  }
}
</style>
