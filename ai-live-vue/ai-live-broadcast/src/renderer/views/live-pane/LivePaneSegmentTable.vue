<!-- 片段列表区 -->
<template>
  <div class="live-pane-segment-table">
    <div class="list-header">
      <h3>片段列表</h3>
    </div>
    <!-- Task3: GPT自动话术模式播放中的提示 -->
    <div v-if="gptConfig.autoScriptEnabled && isPlayingSession" class="gpt-auto-mode-tip">
      <i class="el-icon-loading" style="font-size: 24px; color: #409eff;"></i>
      <p style="margin-top: 16px; font-size: 16px; color: #409eff; font-weight: 500;">正在使用GPT自动生成话术</p>
    </div>
    <el-table
      v-else
      :data="displaySegmentList"
      :row-class-name="getRowClassName"
      border
      style="width: 100%"
      max-height="calc(100vh - 420px)">
      <el-table-column
        type="index"
        label="序号"
        width="60"
        align="center">
      </el-table-column>
      <el-table-column
        prop="name"
        label="片段名称"
        width="150"
        align="center">
      </el-table-column>
      <el-table-column
        prop="productName"
        label="关联商品"
        width="150"
        align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.productName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="videoDir"
        label="视频目录"
        width="150"
        align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.videoDir || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="videoCount"
        label="视频数量"
        width="100"
        align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.videoCount || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="content"
        label="话术文本"
        min-width="200"
        show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ scope.row.content || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="200"
        align="center"
        fixed="right">
        <template slot-scope="scope">
          <el-button
            type="success"
            size="small"
            icon="el-icon-video-play"
            :disabled="isPlayingSession"
            @click="handlePreview(scope.row)">
            试听
          </el-button>
          <el-button
            type="warning"
            size="small"
            icon="el-icon-warning"
            @click="handleShowDialog(scope.row)">
            弹窗
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script>
import AudioPlayer from '../../utils/AudioPlayer'
import { BackgroundMusicPlayer } from '../../utils/BackgroundMusicPlayer'
import gptService from '../../utils/gptService'
import knowledgeBase from '../../utils/knowledgeBase'

export default {
  name: 'LivePaneSegmentTable',
  data() {
    return {
      activeTab: 'digitalHuman',
      liveContext: null,

      // AudioPlayer 实例
      audioPlayer: null,
      queueStatus: {
        isPlaying: false,
        total: 0
      },

      // BackgroundMusicPlayer 实例
      bgMusicPlayer: null,

      // 播放会话标志（从开播到停播/暂停的整个会话）
      isPlayingSession: false,
      isStopping: false, // 正在停播中的标志
      playedAudioCount: 0, // 已播放完成的音频条数
      totalLoadedCount: 0, // 累计已加载的音频总数（只增不减）
      liveStartTime: null, // 开播时间戳

      tableData: [],

      // 脚本信息
      scriptId: '',
      scriptInfo: {},
      scriptContentList: [],
      scriptContentIndex: 0,
      scriptTotalCount: 0,

      // 片段信息
      segmentList: [],
      segmentMap: {},
      randomSelections: {},  // 保存随机片段的选择结果 {索引: 选中的片段名}
      currentPlayingIndex: -1,  // 当前正在播放的片段索引（对应 scriptContentIndex）

      // 播放信息
      playIndex: 1,
      preloadCount: 5,        // 预加载音频数量
      loadingCount: 0,        // 正在加载的音频数量

      // AI 服务器配置
      aiServerConfig: {
        serverUrl: 'http://127.0.0.1:23456/',
        selectedServer: 'local',
        modelType: 'bert-vits2'
      },

      // 主播控制配置
      anchorControlConfig: {
        speed: 50,
        speedRange: 0,
        tone: 50,
        toneRange: 0,
        volume: 100,
        volumeRange: 0,
        resumeScript: "",

        autoInteraction: true,
        chatInteractionProb: 100,
        enterInteractionProb: 100,
        actionInteractionProb: 100,

        autoAvoid: true,
        bgMusic: 20,
        fadeTime: 1000,
        musicDir: "",
        bgVolume: 100,
        musicInterval: 10,

        endSound: true,
        randomPause: true,
        pauseMax: 5,
        scriptRounds: 10
      },

      // 话术定时器
      wordTimer: null,
      wordTimerInterval: 5000,
      wordTimerMaxCount: 3,
      wordTimerIsRunning: false,

      // 播放定时器
      playTimer: null,
      playTimerInterval: 1500,

      // 纯互动式模式（不自动播放脚本，仅触发条件时播放）
      pureInteractionMode: false,

      // ============ GPT 功能配置 ============
      gptConfig: {
        // 话术润色
        polishEnabled: false,
        polishTipWord: '',

        // 自动话术生成
        autoScriptEnabled: false,
        generateSwitch: false,  // false=顺序，true=随机
        autoTipWord: '',
        autoScriptIndex: 0,     // 顺序模式的索引

        // 互动配置
        interactionEnabled: false,
        interactionTipWord: '',
        repeatUserContent: false,
        sayUserName: false,
        useFragmentData: false
      },

      // ============ GPT 自动话术生成 ============
      autoScriptGenerationTimer: null,  // [已废弃] 自动话术生成定时器（已改用 onAudioEnd 触发机制）
      autoScriptPlayCount: 0,           // GPT 自动话术播放计数
      isGeneratingAutoScript: false,    // 正在生成自动话术的标志（防止重复生成）

      // ============ 音频推送到移动端 ============
      isPushMode: true,                 // 推送模式开关（true: 推送到App, false: 本地播放）
      audioPushInitialized: true,       // 音频推送服务是否已初始化

      // ============ 音频流推送（实时推流）============
      useAudioStream: true,             // 是否启用音频流推送（true: 流式推送, false: 文件上传）
      audioStreamConnected: false       // 音频流服务连接状态
    }
  },
  computed: {
    // 根据脚本顺序生成要显示的片段列表
    displaySegmentList() {
      if (!this.scriptContentList || this.scriptContentList.length === 0) {
        // 如果脚本内容为空，返回所有片段
        return this.segmentList
      }

      const displayList = []

      this.scriptContentList.forEach((scriptContent, index) => {
        let segmentName = ''

        if (scriptContent.type === 'random') {
          // 随机片段：如果已有选择，使用已选择的；否则随机选一个
          if (this.randomSelections[index]) {
            segmentName = this.randomSelections[index]
          } else {
            // 随机选择一个
            const options = scriptContent.options
            const randomIndex = Math.floor(Math.random() * options.length)
            segmentName = options[randomIndex]
            // 保存选择（使用 Vue.set 确保响应式）
            this.$set(this.randomSelections, index, segmentName)
          }
        } else if (scriptContent.type === 'segment') {
          segmentName = scriptContent.name
        }

        // 从 segmentList 中查找对应的片段详情
        const segment = this.segmentList.find(s => s.name === segmentName)
        if (segment) {
          displayList.push({
            ...segment,
            scriptIndex: index,
            isRandom: scriptContent.type === 'random',
            randomOptions: scriptContent.type === 'random' ? scriptContent.options : null
          })
        }
      })

      return displayList
    }
  },
  created() {
    // 加载主播控制配置
    this.loadAnchorControlConfig()

    // 加载 GPT 配置
    this.loadGPTConfig()

    // 加载纯互动式模式配置
    const savedMode = localStorage.getItem('pureInteractionMode')
    if (savedMode !== null) {
      this.pureInteractionMode = savedMode === 'true'
    }

    // 初始化 AudioPlayer
    this.audioPlayer = new AudioPlayer()

    // 初始化 BackgroundMusicPlayer
    this.bgMusicPlayer = new BackgroundMusicPlayer()

    // 配置句间结束音和随机停顿
    this.setupEndSoundAndPause()

    // 设置回调
    this.audioPlayer.onPlayStart = (audioObj) => {
      this.debugLog(`开始播放: ${audioObj.text?.substring(0, 20)}...`, 'success')

      // 🎯 在音频开始播放时推送到App端（不管是什么类型的音频）
      if (this.isPushMode) {
        this.pushAudioWhenPlaying(audioObj).catch(err => {
          console.error('[播放时推送] 推送失败:', err)
        })
      }

      // 背景音乐自动回避（主播说话时降低音量）
      if (this.bgMusicPlayer) {
        this.bgMusicPlayer.duck()
      }

      // 更新当前播放的片段索引（用于高亮显示）
      if (audioObj.scriptIndex !== undefined) {
        this.currentPlayingIndex = audioObj.scriptIndex

        // 🎯 更新片段列表表格显示：将实际播放的片段名保存到 randomSelections
        // 这样 displaySegmentList 计算属性会重新计算，表格显示就会更新
        if (audioObj.segmentName) {
          this.$set(this.randomSelections, audioObj.scriptIndex, audioObj.segmentName)
        }
      }

      // 更新当前播放片段显示

      if (this.liveContext && this.liveContext.$refs.livePaneCurrentSegment) {
        this.liveContext.$refs.livePaneCurrentSegment.updateCurrentSegment(audioObj.text, audioObj.speaker)
      }

      // 更新调试状态
      this.updateDebugStatus({
        isPlaying: true,
        currentSpeaker: audioObj.speaker
      })
    }

    this.audioPlayer.onPlayEnd = (audioObj) => {
      this.debugLog(`播放结束: ${audioObj.text?.substring(0, 20)}...`, 'info')

      // 累加已播放完成的音频数量
      if (this.isPlayingSession) {
        this.playedAudioCount++
      }

      // 背景音乐恢复音量（主播说话结束后）
      if (this.bgMusicPlayer) {
        this.bgMusicPlayer.unduck()
      }

      this.updateQueueStatus()

      // 🎯 提前检查队列长度，避免等到完全空才生成（会有停顿）
      // 如果启用了 GPT 自动话术，且队列中剩余少于等于 5 条，立即开始生成
      if (this.gptConfig.autoScriptEnabled && this.isPlayingSession) {
        const queueLength = this.audioPlayer.getQueueLength()
        const threshold = 5  // 阈值：<= 5条就提前生成

        if (queueLength.total <= threshold && !this.isGeneratingAutoScript) {
          this.debugLog(`⚡ 队列仅剩 ${queueLength.total} 条，提前生成新话术`, 'warning')
          this.generateAndAddAutoScript()
        }
      }
    }

    this.audioPlayer.onQueueEmpty = () => {
      this.debugLog('队列已空，正在预加载更多音频...', 'warning')
      // 预加载更多音频
      this.preloadMoreAudios()
    }

    this.audioPlayer.onError = (audioObj, error) => {
      console.error('[LivePane] 播放错误:', error)
      this.debugLog(`播放错误: ${error}`, 'error')
      this.$message.error('音频播放失败: ' + error)
    }

    // 优雅停播完成回调
    this.audioPlayer.onStopComplete = () => {

      // 停止背景音乐
      if (this.bgMusicPlayer) {
        this.bgMusicPlayer.stop()
      }

      // 清除当前播放内容显示
      if (this.liveContext && this.liveContext.$refs.livePaneCurrentSegment) {
        this.liveContext.$refs.livePaneCurrentSegment.clearCurrentSegment()
      }

      // 重置状态
      this.scriptId = ''
      this.scriptContentIndex = 0
      this.currentPlayingIndex = -1
      this.updateQueueStatus()

      this.debugLog('停播完成', 'success')
      this.$message.success('停播完成')
    }

    // 优雅暂停完成回调
    this.audioPlayer.onPauseComplete = () => {
      this.updateQueueStatus()
      this.debugLog('暂停完成', 'success')
      this.$message.success('已暂停播放')
    }

    // 加载 AI 服务器配置
    this.loadAIServerConfig()
  },
  mounted() {
    // 在组件挂载后初始化音频推送服务（文件上传方式）
    // 此时 $store 已经初始化完成
    this.initAudioPushService()

    // 初始化音频流服务（流式推送方式）
    if (this.useAudioStream) {
      this.initAudioStreamService()
    }
  },
  beforeDestroy() {
    // 清理 AudioPlayer
    if (this.audioPlayer) {
      this.audioPlayer.stop()
      this.audioPlayer = null
    }
    // 清理定时器
    if (this.wordTimer) {
      clearInterval(this.wordTimer)
    }
    if (this.playTimer) {
      clearInterval(this.playTimer)
    }
  },
  methods: {
    // 加载主播控制配置
    loadAnchorControlConfig() {
      const savedConfig = localStorage.getItem('anchorControlConfig')
      if (savedConfig) {
        try {
          const config = JSON.parse(savedConfig)
          this.anchorControlConfig = { ...this.anchorControlConfig, ...config }
        } catch (e) {
          console.error('[LivePane] 加载主播控制配置失败:', e)
        }
      }
    },

    // ============ GPT 功能方法 ============

    /**
     * 加载 GPT 配置
     */
    loadGPTConfig() {
      try {
        // 加载话术润色配置
        this.gptConfig.polishEnabled = JSON.parse(localStorage.getItem('gptPolishEnabled') || 'false')
        this.gptConfig.polishTipWord = localStorage.getItem('gptPolishTipWord') || ''

        // 加载自动话术配置
        this.gptConfig.autoScriptEnabled = JSON.parse(localStorage.getItem('gptAutoScriptEnabled') || 'false')
        this.gptConfig.generateSwitch = JSON.parse(localStorage.getItem('gptGenerateSwitch') || 'false')
        this.gptConfig.autoTipWord = localStorage.getItem('gptAutoTipWord') || ''

        // 加载互动配置
        this.gptConfig.interactionEnabled = JSON.parse(localStorage.getItem('gptInteractionEnabled') || 'false')
        this.gptConfig.interactionTipWord = localStorage.getItem('gptInteractionTipWord') || ''
        this.gptConfig.repeatUserContent = JSON.parse(localStorage.getItem('gptRepeatUserContent') || 'false')
        this.gptConfig.sayUserName = JSON.parse(localStorage.getItem('gptSayUserName') || 'false')
        this.gptConfig.useFragmentData = JSON.parse(localStorage.getItem('gptUseFragmentData') || 'false')

        // 详细日志输出
        console.log('[GPT配置] 加载完成:', {
          polishEnabled: this.gptConfig.polishEnabled,
          polishTipWord: this.gptConfig.polishTipWord ? '已设置' : '未设置',
          autoScriptEnabled: this.gptConfig.autoScriptEnabled,
          interactionEnabled: this.gptConfig.interactionEnabled
        })

        this.debugLog(`📋 GPT配置已加载 - 润色: ${this.gptConfig.polishEnabled ? '✅ 已启用' : '❌ 未启用'}`, 'info')
      } catch (error) {
        console.error('[LivePane] 加载GPT配置失败:', error)
        this.debugLog('❌ 加载GPT配置失败: ' + error.message, 'error')
      }
    },

    // ============ 音频推送到移动端 ============

    /**
     * 在音频播放时推送到App端（在 onPlayStart 回调中调用）
     * @param {Object} audioObj - 音频对象 { text, url, speaker, type }
     */
    async pushAudioWhenPlaying(audioObj) {
      if (!audioObj || !audioObj.url) {
        console.warn('[播放时推送] 音频对象无效，跳过推送')
        return
      }

      const text = audioObj.text || ''
      const audioFilePath = audioObj.url

      console.log('[播放时推送] 开始推送音频:', {
        text: text.substring(0, 30),
        url: audioFilePath.substring(audioFilePath.lastIndexOf('/') + 1),
        type: audioObj.type
      })

      try {
        // 使用音频流推送（实时，低延迟）
        await this.streamTTSAudio(text, audioFilePath)
      } catch (err) {
        console.error('[播放时推送] 推送异常:', err)
      }
    },

    /**
     * 初始化音频流服务（实时推流方式）
     */
    async initAudioStreamService() {
      const { ipcRenderer } = require('electron')

      try {
        // 读取用户信息
        const token = localStorage.getItem('token')
        const userId = localStorage.getItem('userId')
        const apiHost = process.env.userConfig?.API_HOST || 'http://localhost:8080/ai-live-api'

        // 解析 JSON 格式
        const parsedUserId = userId ? (userId.startsWith('"') ? JSON.parse(userId) : userId) : null

        if (!parsedUserId) {
          console.warn('[音频流] userId 不存在，跳过初始化')
          return
        }

        // 移除 context path，得到基础 URL
        const baseApiHost = apiHost.replace('/ai-live-api', '')

        console.log('[音频流] 正在连接服务器:', baseApiHost)

        // 连接 WebSocket
        const result = await ipcRenderer.invoke('audio-stream:connect', {
          apiHost: baseApiHost,
          merchantId: parsedUserId,  // merchantId = userId
          userId: parsedUserId
        })

        if (result.success) {
          this.audioStreamConnected = true
          console.log('[音频流] ✓ 服务初始化成功')
          this.debugLog('音频流服务已连接', 'success')
        } else {
          console.error('[音频流] ✗ 连接失败:', result.error)
          this.debugLog('音频流连接失败', 'error')
        }

      } catch (err) {
        console.error('[音频流] 初始化异常:', err)
        this.debugLog('音频流初始化异常', 'error')
      }
    },

    /**
     * 使用音频流推送 TTS 音频（实时推流）
     * @param {string} text - TTS 文本内容
     * @param {string} audioFilePath - 音频文件路径
     */
    async streamTTSAudio(text, audioFilePath) {
      const { ipcRenderer } = require('electron')

      if (!this.audioStreamConnected) {
        console.warn('[音频流] 服务未连接，尝试重新连接...')
        await this.initAudioStreamService()

        if (!this.audioStreamConnected) {
          console.error('[音频流] 无法连接服务')
          this.debugLog('音频流服务未连接', 'error')
          return { success: false, error: '服务未连接' }
        }
      }

      try {
        console.log('[音频流] 开始推送:', text.substring(0, 30))

        const result = await ipcRenderer.invoke('audio-stream:start', {
          text: text,
          audioFilePath: audioFilePath
        })

        if (result.success) {
          console.log('[音频流] ✓ 推送完成:', result.stats)
          this.debugLog(`音频流推送成功: ${text.substring(0, 20)}`, 'success')
        } else {
          console.error('[音频流] ✗ 推送失败:', result.error)
          this.debugLog('音频流推送失败', 'error')
        }

        return result

      } catch (err) {
        console.error('[音频流] 推送异常:', err)
        this.debugLog('音频流推送异常', 'error')
        return { success: false, error: err.message }
      }
    },

    /**
     * 话术润色（使用GPT改写话术）
     * @param {String} text - 原始话术
     * @returns {Promise<String>} - 润色后的话术
     */
    async polishScriptWithGPT(text) {
      if (!this.gptConfig.polishEnabled || !text) {
        return text
      }

      try {
        this.debugLog('正在使用GPT润色话术...', 'info')

        const result = await gptService.polishScript(text, this.gptConfig.polishTipWord)

        console.log('[polishScriptWithGPT] 返回结果:', result)

        if (result.success && result.content) {
          const displayText = result.content.length > 30 ? result.content.substring(0, 30) + '...' : result.content
          this.debugLog(`话术润色成功: ${displayText}`, 'success')
          return result.content
        } else {
          console.error('[LivePane] GPT润色失败:', result.error)
          this.debugLog(`GPT润色失败: ${result.error}`, 'warning')
          return text  // 失败时返回原文
        }
      } catch (error) {
        console.error('[LivePane] GPT润色异常:', error)
        this.debugLog(`GPT润色异常: ${error.message}`, 'error')
        return text  // 异常时返回原文
      }
    },

    /**
     * 自动生成话术（使用GPT和知识库生成500字直播话术）
     * @returns {Promise<String>} - 生成的话术
     */
    async generateAutoScriptWithGPT() {
      if (!this.gptConfig.autoScriptEnabled) {
        return null
      }

      try {
        // 检查知识库是否已加载
        if (!knowledgeBase.isKnowledgeLoaded()) {
          console.warn('[LivePane] 知识库未加载，无法生成自动话术')
          this.debugLog('知识库未加载，无法生成自动话术', 'warning')
          return null
        }

        // 根据配置选择商品（随机或顺序）
        let product = null
        if (this.gptConfig.generateSwitch) {
          // 随机模式
          product = knowledgeBase.getRandomItem()
          this.debugLog('随机选择商品生成话术', 'info')
        } else {
          // 顺序模式
          product = knowledgeBase.getByIndex(this.gptConfig.autoScriptIndex)
          this.gptConfig.autoScriptIndex = (this.gptConfig.autoScriptIndex + 1) % knowledgeBase.getCount()
          this.debugLog(`顺序选择商品生成话术 (索引: ${this.gptConfig.autoScriptIndex - 1})`, 'info')
        }

        if (!product) {
          console.warn('[LivePane] 未获取到商品信息')
          return null
        }

        this.debugLog(`正在为商品【${product.name}】生成话术...`, 'info')

        // 格式化商品信息为提示词
        const productInfo = knowledgeBase.formatForPrompt(product)

        // 调用 GPT 生成话术
        const customPrompt = this.gptConfig.autoTipWord
        if (customPrompt) {
          this.debugLog(`========== 使用自定义 Prompt ==========`, 'info')
          this.debugLog(`Prompt 长度: ${customPrompt.length} 字`, 'info')
          this.debugLog(`Prompt 前100字: ${customPrompt.substring(0, 100)}...`, 'info')

          // 检查是否包含"不少于XXX字"这样的要求
          const wordCountMatch = customPrompt.match(/不少于\s*(\d+)\s*字/)
          if (wordCountMatch) {
            this.debugLog(`⚠️ 提示词要求生成不少于 ${wordCountMatch[1]} 字`, 'warning')
          }
        } else {
          this.debugLog(`========== 未配置提示词，使用极简默认 Prompt ==========`, 'warning')
        }

        const result = await gptService.generateScript(productInfo, customPrompt)

        if (result.success && result.content) {
          this.debugLog(`自动话术生成成功 (${result.content.length}字)`, 'success')

          // 🔍 调试：检查换行符
          const doubleNewlineCount = (result.content.match(/\n\n/g) || []).length
          const singleNewlineCount = (result.content.match(/\n/g) || []).length - doubleNewlineCount * 2
          this.debugLog(`话术中包含: ${doubleNewlineCount} 个双换行, ${singleNewlineCount} 个单换行`, 'info')

          return result.content
        } else {
          console.error('[LivePane] GPT生成话术失败:', result.error)
          this.debugLog(`GPT生成话术失败: ${result.error}`, 'error')
          return null
        }
      } catch (error) {
        console.error('[LivePane] GPT生成话术异常:', error)
        this.debugLog(`GPT生成话术异常: ${error.message}`, 'error')
        return null
      }
    },

    /**
     * 关键词互动（用户发送消息时触发）
     * @param {String} userMessage - 用户消息
     * @param {String} userName - 用户名
     */
    async handleKeywordInteractionWithGPT(userMessage, userName = '') {
      if (!this.gptConfig.interactionEnabled || !userMessage) {
        return
      }

      // ⚠️ 检查播放状态（防止停播后继续处理）
      if (!this.isPlayingSession || this.isStopping) {
        this.debugLog('⚠️ 播放已停止，跳过关键词互动', 'warning')
        return
      }

      try {
        this.debugLog(`收到用户消息: ${userMessage}`, 'info')

        // 构建上下文信息
        let contextInfo = ''

        // 是否使用片段数据辅助回答
        if (this.gptConfig.useFragmentData && this.currentPlayingIndex >= 0) {
          const currentSegment = this.displaySegmentList.find(s => s.scriptIndex === this.currentPlayingIndex)
          if (currentSegment && currentSegment.content) {
            contextInfo += `\n当前播放片段: ${currentSegment.name}\n片段内容: ${currentSegment.content.substring(0, 200)}\n`
          }
        }

        // 是否加载知识库信息
        if (knowledgeBase.isKnowledgeLoaded()) {
          // 尝试从知识库中搜索相关信息
          const searchResults = knowledgeBase.search(userMessage)
          if (searchResults.length > 0) {
            contextInfo += knowledgeBase.formatForPrompt(searchResults.slice(0, 2))  // 最多2个结果
          }
        }

        // 调用 GPT 生成回复
        const result = await gptService.replyToKeyword(userMessage, contextInfo, this.gptConfig.interactionTipWord)

        if (result.success) {
          let replyText = ''

          // 是否重复用户发言
          if (this.gptConfig.repeatUserContent) {
            replyText += userMessage + '，'
          }

          // 是否点用户名字
          if (this.gptConfig.sayUserName && userName) {
            replyText += userName + '，'
          }

          // 加上 GPT 回复
          replyText += result.replyText

          this.debugLog(`GPT互动回复: ${replyText}`, 'success')

          // 插入到播放队列（使用临时话术的方法）
          await this.insertTemporarySpeech(replyText, 'keyword-interaction')

        } else {
          console.error('[LivePane] GPT互动回复失败:', result.error)
          this.debugLog(`GPT互动回复失败: ${result.error}`, 'error')
        }
      } catch (error) {
        console.error('[LivePane] GPT互动回复异常:', error)
        this.debugLog(`GPT互动回复异常: ${error.message}`, 'error')
      }
    },

    /**
     * 行为互动（用户进入、关注、点赞等行为触发）
     * @param {String} userAction - 用户行为类型 (enter, follow, like, gift)
     * @param {String} userName - 用户名
     * @param {Object} extraInfo - 额外信息（如礼物名称、数量等）
     */
    async handleActionInteractionWithGPT(userAction, userName = '', extraInfo = {}) {
      if (!this.gptConfig.interactionEnabled || !userAction) {
        return
      }

      // ⚠️ 检查播放状态（防止停播后继续处理）
      if (!this.isPlayingSession || this.isStopping) {
        this.debugLog('⚠️ 播放已停止，跳过行为互动', 'warning')
        return
      }

      try {
        // 映射行为类型为中文描述
        const actionMap = {
          'enter': '进入直播间',
          'follow': '关注了主播',
          'like': '点赞了',
          'gift': `送了${extraInfo.giftName || '礼物'}`
        }

        const actionDesc = actionMap[userAction] || userAction
        this.debugLog(`用户行为: ${userName} ${actionDesc}`, 'info')

        // 构建上下文信息
        let contextInfo = `用户行为: ${actionDesc}\n`

        // 是否使用片段数据辅助回答
        if (this.gptConfig.useFragmentData && this.currentPlayingIndex >= 0) {
          const currentSegment = this.displaySegmentList.find(s => s.scriptIndex === this.currentPlayingIndex)
          if (currentSegment && currentSegment.content) {
            contextInfo += `当前播放片段: ${currentSegment.name}\n`
          }
        }

        // 调用 GPT 生成回复
        const result = await gptService.replyToAction(actionDesc, userName, contextInfo, this.gptConfig.interactionTipWord)

        if (result.success) {
          let replyText = ''

          // 是否点用户名字
          if (this.gptConfig.sayUserName && userName) {
            replyText += userName + '，'
          }

          // 加上 GPT 回复
          replyText += result.replyText

          this.debugLog(`GPT行为互动回复: ${replyText}`, 'success')

          // 插入到播放队列（使用临时话术的方法）
          await this.insertTemporarySpeech(replyText, 'action-interaction')

        } else {
          console.error('[LivePane] GPT行为互动回复失败:', result.error)
          this.debugLog(`GPT行为互动回复失败: ${result.error}`, 'error')
        }
      } catch (error) {
        console.error('[LivePane] GPT行为互动回复异常:', error)
        this.debugLog(`GPT行为互动回复异常: ${error.message}`, 'error')
      }
    },

    // ============ 结束 GPT 功能方法 ============


    // 配置句间结束音和随机停顿
    async setupEndSoundAndPause() {
      try {
        // 扫描句间音效文件
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('scan-end-sound-files')

        if (result.success) {

          // 配置 AudioPlayer
          this.audioPlayer.setEndSoundConfig(
            this.anchorControlConfig.endSound,
            result.files
          )

          this.audioPlayer.setRandomPauseConfig(
            this.anchorControlConfig.randomPause,
            this.anchorControlConfig.pauseMax
          )

        } else {
          console.warn('[LivePane] 扫描句间音效文件失败:', result.error)
        }
      } catch (error) {
        console.error('[LivePane] 配置句间结束音失败:', error)
      }
    },

    // 更新主播控制配置（当用户在对话框中修改配置时调用）
    updateAnchorControlConfig(config) {
      this.anchorControlConfig = { ...this.anchorControlConfig, ...config }

      // 重新配置 AudioPlayer
      if (this.audioPlayer) {
        this.audioPlayer.setRandomPauseConfig(
          this.anchorControlConfig.randomPause,
          this.anchorControlConfig.pauseMax
        )

        this.audioPlayer.setEndSoundConfig(
          this.anchorControlConfig.endSound,
          this.audioPlayer.endSoundFiles // 使用现有的音效文件列表
        )

      }
    },

    // 更新纯互动式模式
    updatePureInteractionMode(enabled) {
      this.pureInteractionMode = enabled
      this.debugLog(`纯互动式模式已${enabled ? '启用' : '关闭'}`, 'info')
    },

    // 获取表格行的类名（用于高亮当前播放的行）
    getRowClassName({ row, rowIndex }) {
      // 如果当前行的 scriptIndex 等于正在播放的索引，添加高亮类
      if (row.scriptIndex === this.currentPlayingIndex) {
        return 'current-playing-row'
      }
      return ''
    },

    // 初始化
    init(liveContext) {
      this.liveContext = liveContext
      this.debugLog('片段表初始化完成', 'success')
    },

    // 添加调试日志
    debugLog(message, type = 'info') {
      if (this.liveContext && this.liveContext.$refs.debugPanel) {
        this.liveContext.$refs.debugPanel.addLog(message, type)
      }
    },

    // 更新调试面板状态
    updateDebugStatus(status) {
      if (this.liveContext && this.liveContext.$refs.debugPanel) {
        this.liveContext.$refs.debugPanel.updateStatus(status)
      }
    },

    // 加载 AI 服务器配置
    loadAIServerConfig() {
      // 从 localStorage 读取配置
      const savedConfig = localStorage.getItem('aiServerConfig')
      if (savedConfig) {
        try {
          this.aiServerConfig = JSON.parse(savedConfig)
        } catch (e) {
          console.error('解析 AI 服务器配置失败:', e)
        }
      }
    },

    // 更新队列状态
    updateQueueStatus() {
      if (this.audioPlayer) {
        const status = this.audioPlayer.getStatus()
        this.queueStatus = {
          isPlaying: status.isPlaying,
          total: status.queueLength.total
        }

        // 更新调试面板
        this.updateDebugStatus({
          isPlaying: status.isPlaying,
          queueLength: status.queueLength.total
        })
      }
    },

    // 开播主方法 (新实现)
    async startPlayHandle(scriptId, onLoadProgress, onLoadComplete) {
      if (!scriptId) {
        this.$message.warning('请先选择脚本')
        return
      }

      this.debugLog('========== 开始开播 ==========', 'info')
      this.debugLog(`脚本ID: ${scriptId}`, 'info')

      this.scriptId = scriptId

      // 🎯 检查是否启用 GPT 自动话术功能
      if (this.gptConfig.autoScriptEnabled) {
        this.debugLog('🤖 检测到 GPT 自动话术功能已启用', 'info')

        // 检查知识库是否已加载
        if (!knowledgeBase.isKnowledgeLoaded()) {
          this.$message.error('GPT 自动话术功能需要先选择知识库！')
          this.debugLog('❌ 知识库未加载，无法启用自动话术', 'error')
          return
        }

        this.debugLog(`✓ 知识库已加载，共 ${knowledgeBase.getCount()} 条商品`, 'success')
        this.debugLog(`📋 生成模式: ${this.gptConfig.generateSwitch ? '随机' : '顺序'}`, 'info')

        // 启动 GPT 自动话术模式
        await this.startGPTAutoScriptMode(onLoadProgress, onLoadComplete)
        return
      }

      // 检查并启动语音服务
      this.debugLog('正在检查语音服务状态...', 'info')
      const serviceReady = await this.ensureVoiceServiceRunning()
      if (!serviceReady) {
        this.debugLog('语音服务启动失败，无法开播', 'error')
        this.$message.error('语音服务启动失败，无法开播')
        return
      }
      this.debugLog('语音服务已就绪', 'success')

      // 清理音频缓存（像原项目一样，每次开播都清空缓存）
      this.debugLog('正在清理音频缓存...', 'info')
      const { ipcRenderer } = require('electron')
      const clearResult = await ipcRenderer.invoke('clear-audio-cache')
      if (clearResult.success) {
        this.debugLog(`已清理 ${clearResult.count} 个缓存文件`, 'success')
      } else {
        this.debugLog('清理缓存失败: ' + clearResult.error, 'warning')
      }

      // 加载脚本信息和片段列表
      this.debugLog('正在加载脚本信息...', 'info')
      await this.getLiveScriptInfo()
      await this.getLiveSegmentList()
      this.debugLog(`已加载 ${this.segmentList.length} 个片段`, 'success')

      // 计算预加载数量（根据脚本长度和轮数）
      const preloadCount = this.calculatePreloadCount()

      // 预加载音频（传入进度回调）
      this.debugLog(`开始预加载 ${preloadCount} 个音频 (${this.anchorControlConfig.scriptRounds}轮话术)`, 'info')

      // 重置计数器
      this.playedAudioCount = 0
      this.totalLoadedCount = 0

      // 记录开播时间
      this.liveStartTime = new Date()

      // 设置播放会话标志（必须在预加载之前设置，否则预加载会被中止）
      this.isPlayingSession = true

      await this.preloadAudios(preloadCount, onLoadProgress)

      // 开始播放
      this.debugLog('开始播放音频队列', 'success')
      this.audioPlayer.start()
      this.updateQueueStatus()

      // 调用加载完成回调
      if (onLoadComplete) {
        onLoadComplete()
      }

      this.debugLog('========== 开播完成 ==========', 'success')
    },

    /**
     * 🤖 启动 GPT 自动话术模式
     * 特点：不加载片段列表，动态生成话术并播放
     */
    async startGPTAutoScriptMode(onLoadProgress, onLoadComplete) {
      this.debugLog('========== GPT 自动话术模式启动 ==========', 'info')

      // 🎯 重新加载 GPT 配置，确保使用最新的设置
      this.debugLog('重新加载 GPT 配置...', 'info')
      this.loadGPTConfig()
      this.debugLog(`✓ GPT 配置已加载: autoTipWord=${this.gptConfig.autoTipWord ? this.gptConfig.autoTipWord.length + '字' : '未设置'}`, 'info')

      // 检查并启动语音服务
      this.debugLog('正在检查语音服务状态...', 'info')
      const serviceReady = await this.ensureVoiceServiceRunning()
      if (!serviceReady) {
        this.debugLog('语音服务启动失败，无法开播', 'error')
        this.$message.error('语音服务启动失败，无法开播')
        return
      }
      this.debugLog('语音服务已就绪', 'success')

      // 清理音频缓存
      this.debugLog('正在清理音频缓存...', 'info')
      const { ipcRenderer } = require('electron')
      const clearResult = await ipcRenderer.invoke('clear-audio-cache')
      if (clearResult.success) {
        this.debugLog(`已清理 ${clearResult.count} 个缓存文件`, 'success')
      } else {
        this.debugLog('清理缓存失败: ' + clearResult.error, 'warning')
      }

      // 🎯 GPT 自动话术模式：清空片段列表
      this.segmentList = []
      this.displaySegmentList = []
      this.scriptContentList = []
      this.debugLog('✓ 已清空片段列表（GPT 自动话术模式）', 'info')

      // 重置自动话术索引
      this.gptConfig.autoScriptIndex = 0

      // 重置计数器
      this.playedAudioCount = 0
      this.totalLoadedCount = 0

      // 记录开播时间
      this.liveStartTime = new Date()

      // 设置播放会话标志
      this.isPlayingSession = true

      // 🎯 预生成前几条话术并播放
      this.debugLog('开始预生成话术...', 'info')
      const preGenerateCount = 2  // 预生成2条话术
      let generatedCount = 0

      for (let i = 0; i < preGenerateCount; i++) {
        const script = await this.generateAutoScriptWithGPT()
        if (script) {
          this.debugLog(`✓ 预生成第 ${i + 1} 条话术 (${script.length}字)`, 'success')

          // 🎯 按段落拆分话术，生成多个音频
          const paragraphs = this.splitScriptIntoParagraphs(script)
          this.debugLog(`📋 话术拆分为 ${paragraphs.length} 个段落`, 'info')

          // 为每个段落生成音频
          for (let j = 0; j < paragraphs.length; j++) {
            const paragraph = paragraphs[j]
            if (!paragraph.trim()) continue  // 跳过空段落

            this.debugLog(`  生成第 ${j + 1}/${paragraphs.length} 段音频...`, 'info')

            const audioUrl = await this.generateTTSAudio(paragraph, { skipPolish: true })
            if (audioUrl) {
              this.audioPlayer.addToQueue({
                text: paragraph,
                url: audioUrl,
                speaker: this.getCurrentSpeaker(),
                type: 'script',
                segmentName: `商品 ${i + 1}-段落 ${j + 1}`,
                scriptIndex: i * 100 + j  // 唯一索引
              })
              this.totalLoadedCount++  // 累加已加载音频总数
              generatedCount++
            }
          }

          // 更新进度
          if (onLoadProgress) {
            onLoadProgress(i + 1, preGenerateCount)
          }
        } else {
          this.debugLog(`✗ 第 ${i + 1} 条话术生成失败`, 'warning')
        }
      }

      if (generatedCount === 0) {
        this.$message.error('GPT 话术生成失败，无法开播')
        this.debugLog('❌ 没有成功生成任何话术', 'error')
        this.isPlayingSession = false
        return
      }

      // 开始播放
      this.debugLog(`✓ 预生成完成，共 ${generatedCount} 条话术`, 'success')
      this.debugLog('开始播放音频队列', 'success')
      this.audioPlayer.start()
      this.updateQueueStatus()

      // 🎯 不再使用定时循环检查（改用 onAudioEnd 事件触发检查，更及时高效）
      // 原有的 startAutoScriptGenerationLoop 已移除，避免重复生成

      // 调用加载完成回调
      if (onLoadComplete) {
        onLoadComplete()
      }

      this.debugLog('========== GPT 自动话术模式启动完成 ==========', 'success')
      this.$message.success('GPT 自动话术模式已启动')
    },

    /**
     * 📋 将话术按段落拆分
     * @param {String} script - 完整话术
     * @returns {Array<String>} 段落数组
     */
    splitScriptIntoParagraphs(script) {
      if (!script) return []

      this.debugLog('📋 开始拆分话术...', 'info')
      this.debugLog(`原始话术长度: ${script.length}字`, 'info')
      this.debugLog(`原始话术内容:\n${script}`, 'info')

      // 🎯 只按换行符分割（优先双换行，其次单换行）
      const hasDoubleNewline = script.includes('\n\n')
      const hasSingleNewline = script.includes('\n')

      let paragraphs = []

      if (hasDoubleNewline) {
        // 如果有双换行符，按双换行符分割
        this.debugLog('检测到双换行符，按双换行符分割', 'info')
        paragraphs = script.split('\n\n')
          .map(p => p.trim())
          .filter(p => p.length > 0)
        this.debugLog(`按双换行符分割: ${paragraphs.length} 个段落`, 'info')
      } else if (hasSingleNewline) {
        // 如果只有单换行符，按单换行符分割
        this.debugLog('检测到单换行符，按单换行符分割', 'info')
        paragraphs = script.split('\n')
          .map(p => p.trim())
          .filter(p => p.length > 0)
        this.debugLog(`按单换行符分割: ${paragraphs.length} 个段落`, 'info')
      } else {
        // 🎯 没有换行符，作为单条话术，不分割
        this.debugLog('没有换行符，作为单条话术', 'info')
        paragraphs = [script.trim()]
      }

      // 过滤掉动作描述（如"拿起xxx"、"敲击xxx"、"(挥手)"等）
      const beforeFilter = paragraphs.length
      paragraphs = paragraphs.filter(p => {
        const actionPatterns = [
          /^拿起.{0,10}展示/,          // 拿起键盘实物展示
          /^敲击.{0,10}演示/,          // 敲击键盘演示
          /^展示/,                     // 展示产品
          /^演示/,                     // 演示功能
          /^举起/,                     // 举起商品
          /^指向/,                     // 指向屏幕
          /^\(.{0,20}\)$/,             // (挥手) (微笑) (点头)
          /^（.{0,20}）$/,             // （挥手） （微笑） （点头）
          /^[\(（].*[挥招指点摇摆拍打举抬弯].{0,10}[\)）]$/  // 包含动作动词的括号内容
        ]

        // 检查是否匹配任何动作描述模式
        const isAction = actionPatterns.some(pattern => pattern.test(p.trim()))

        // 检查是否只包含括号动作（如段落中间出现的动作）
        const onlyBracketAction = /^[\(（].{1,20}[\)）]$/.test(p.trim())

        if (isAction || onlyBracketAction) {
          this.debugLog(`⚠️ 过滤动作描述: ${p.substring(0, 30)}...`, 'warning')
          return false
        }

        return true
      })

      if (beforeFilter !== paragraphs.length) {
        this.debugLog(`过滤动作描述后: ${paragraphs.length} 个段落 (过滤掉 ${beforeFilter - paragraphs.length} 个)`, 'info')
      }

      // 移除段落内部的动作描述（如"xxx（元气漫漫的挥手）xxx"、"兴奋地展示鼠标xxx"）
      paragraphs = paragraphs.map(p => {
        const original = p

        // 移除开头的动作描述（如"兴奋地展示鼠标"、"指着屏幕"、"开心地比划"）
        let cleaned = p.replace(/^[^\u4e00-\u9fa5]*?[地得的](展示|指着|比划|挥手|微笑|点头|摇头|拿起|举起|指向)[^\u4e00-\u9fa5]*/g, '')

        // 移除括号内的动作描述
        cleaned = cleaned.replace(/[\(（][^()（）]*[挥招指点摇摆拍打举抬弯展示比划][^()（）]*[\)）]/g, '')

        // 移除纯括号动作
        cleaned = cleaned.replace(/[\(（].{1,20}[\)）]/g, '')

        if (cleaned !== original) {
          this.debugLog(`清理段落内动作: "${original.substring(0, 30)}..." -> "${cleaned.substring(0, 30)}..."`, 'info')
        }

        return cleaned.trim()
      }).filter(p => p.length > 0)  // 过滤掉清理后变空的段落

      // 🎯 最终结果 - 不再判断长度，直接使用分割后的段落
      this.debugLog(`最终拆分结果: ${paragraphs.length} 个段落`, 'success')
      paragraphs.forEach((p, i) => {
        this.debugLog(`  段落 ${i + 1}: ${p.substring(0, 30)}... (${p.length}字)`, 'info')
      })

      return paragraphs.length > 0 ? paragraphs : [script]
    },

    /**
     * 🔄 GPT 自动话术生成循环（已废弃）
     * 原有的定时循环检查机制会导致重复生成，已改为使用 onAudioEnd 事件触发检查
     * 保留此方法仅为了兼容性，实际不再调用
     */
    startAutoScriptGenerationLoop() {
      // ⚠️ 此方法已废弃，不再使用
      // 原因：定时循环会与 onAudioEnd 检查重复，导致重复生成话术
      // 新机制：在每次音频播放结束时检查队列（onAudioEnd），更及时且不会重复
      this.debugLog('⚠️ startAutoScriptGenerationLoop 已废弃，使用 onAudioEnd 机制', 'warning')
    },

    /**
     * 🚀 生成自动话术并加入队列
     * 从 generateAutoScriptWithGPT 生成话术，分割后加入播放队列
     * 使用 isGeneratingAutoScript 标志防止重复生成
     */
    async generateAndAddAutoScript() {
      // 防止重复生成
      if (this.isGeneratingAutoScript) {
        this.debugLog('已有生成任务在进行中，跳过本次生成', 'info')
        return
      }

      // 🎯 检查播放会话状态（防止停播后继续生成）
      if (!this.isPlayingSession || this.isStopping) {
        this.debugLog('播放会话已结束或正在停播，取消生成任务', 'info')
        return
      }

      try {
        this.isGeneratingAutoScript = true
        this.debugLog('🚀 开始生成自动话术...', 'info')

        // 生成自动话术
        const script = await this.generateAutoScriptWithGPT()

        // ⚠️ 生成完成后再次检查播放状态（异步操作后状态可能已变化）
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog('⚠️ 生成期间播放已停止，丢弃生成结果', 'warning')
          return
        }

        if (!script || !script.trim()) {
          this.debugLog('✗ 生成自动话术失败或内容为空', 'warning')
          return
        }

        this.debugLog(`✓ 生成新话术成功 (${script.length}字)`, 'success')

        // 🎯 按段落拆分话术，生成多个音频
        const paragraphs = this.splitScriptIntoParagraphs(script)
        this.debugLog(`📋 话术拆分为 ${paragraphs.length} 个段落`, 'info')

        // 为每个段落生成音频
        let addedCount = 0
        for (let j = 0; j < paragraphs.length; j++) {
          // ⚠️ 每次循环都检查播放状态（生成音频是耗时操作）
          if (!this.isPlayingSession || this.isStopping) {
            this.debugLog(`⚠️ 播放已停止，中止剩余 ${paragraphs.length - j} 个段落的生成`, 'warning')
            break  // 立即退出循环
          }

          const paragraph = paragraphs[j]
          if (!paragraph.trim()) continue  // 跳过空段落

          // 先处理文本（应用所有规则）
          let processedText = this.parseScriptSyntax(paragraph)
          processedText = this.replaceHomophones(processedText)

          // 生成音频
          const audioUrl = await this.generateTTSAudioOnly(processedText)

          // ⚠️ 生成音频后再次检查（异步操作后状态可能已变化）
          if (!this.isPlayingSession || this.isStopping) {
            this.debugLog('⚠️ 生成音频期间播放已停止，丢弃当前音频', 'warning')
            break  // 立即退出循环
          }

          if (audioUrl) {
            // 计算音量
            const volumeBase = this.anchorControlConfig.volume / 100.0
            const volumeFloat = this.anchorControlConfig.volumeRange / 100.0
            const volumeRandom = (Math.random() * 2 - 1) * volumeBase * volumeFloat
            const volume = Math.max(0, Math.min(1.0, volumeBase + volumeRandom))

            // 添加到队列
            const index = this.autoScriptPlayCount++
            this.audioPlayer.addToQueue({
              text: processedText,
              url: audioUrl,
              speaker: this.getCurrentSpeaker(),
              type: 'script',
              segmentName: `商品 ${index + 1}-段落 ${j + 1}`,
              scriptIndex: index * 100 + j,
              volume: volume
            })
            this.totalLoadedCount++  // 累加已加载音频总数
            addedCount++
          }
        }

        // ⚠️ 最终检查：只有在仍在播放时才启动播放器
        if (addedCount > 0 && this.isPlayingSession && !this.isStopping) {
          this.debugLog(`✅ ${addedCount} 个段落已加入播放队列`, 'success')
          this.updateQueueStatus()

          // 如果播放器未在播放，启动播放
          if (!this.audioPlayer.isPlaying) {
            this.audioPlayer.start()
          }
        } else if (addedCount > 0) {
          this.debugLog('⚠️ 已生成音频但播放已停止，不加入队列', 'warning')
        } else {
          this.debugLog('⚠️ 未能成功生成任何音频', 'warning')
        }

      } catch (error) {
        console.error('[LivePane] 生成自动话术异常:', error)
        this.debugLog(`❌ 生成自动话术异常: ${error.message}`, 'error')
      } finally {
        // 无论成功失败，都要重置标志
        this.isGeneratingAutoScript = false
      }
    },

    // 确保语音服务正在运行
    async ensureVoiceServiceRunning() {
      try {
        this.debugLog('正在检查语音服务状态...', 'info')

        // 读取AI服务器配置
        const config = this.aiServerConfig
        const serviceType = config.selectedServer || 'local'

        // 如果不是本地服务，跳过检查
        if (serviceType !== 'local') {
          this.debugLog('使用云端服务，跳过本地服务检查', 'info')
          this.updateDebugStatus({ serviceRunning: true })
          return true
        }

        // 检查服务状态
        const { ipcRenderer } = require('electron')
        const statusResult = await ipcRenderer.invoke('check-voice-service-status', {
          serviceType: serviceType
        })

        if (statusResult.success && statusResult.running) {
          this.debugLog('语音服务已在运行', 'success')
          this.updateDebugStatus({ serviceRunning: true })
          return true
        }

        // 服务未运行，尝试启动
        this.debugLog('语音服务未运行，正在启动...', 'warning')
        this.$message({
          message: '正在启动语音服务，请稍候...',
          type: 'info',
          duration: 3000,
          showClose: true
        })

        const startResult = await ipcRenderer.invoke('start-voice-service', {
          serviceType: serviceType,
          options: {
            enableSuggestion: config.enableSuggestion || false
          }
        })

        if (startResult.success) {
          this.debugLog('语音服务启动成功', 'success')
          this.updateDebugStatus({ serviceRunning: true })
          this.$message.success('语音服务启动成功')
          return true
        } else {
          this.debugLog(`语音服务启动失败: ${startResult.error}`, 'error')
          this.updateDebugStatus({ serviceRunning: false })
          this.$message.error('语音服务启动失败: ' + startResult.error)
          return false
        }

      } catch (error) {
        this.debugLog(`检查/启动语音服务时出错: ${error.message}`, 'error')
        console.error('[LivePane] 检查/启动语音服务时出错:', error)
        this.updateDebugStatus({ serviceRunning: false })
        this.$message.error('语音服务异常: ' + error.message)
        return false
      }
    },

    // 计算预加载音频数量（根据脚本长度和轮数）
    calculatePreloadCount() {
      // 获取脚本长度
      const scriptLength = this.scriptContentList.length

      if (scriptLength === 0) {
        console.warn('[LivePane] 脚本长度为0，使用默认预加载数量')
        return this.preloadCount  // 默认5个
      }

      // 🎯 修改：只预加载 2 轮完整话术，让随机选择更频繁地触发
      const rounds = 2  // 固定为2轮，不再使用配置的轮数

      // 计算目标加载数量 = 脚本长度 × 2轮
      const targetCount = scriptLength * rounds

      // 设置合理的上下限
      const MIN_PRELOAD = 3    // 最少加载3个
      const MAX_PRELOAD = 20   // 最多加载20个（避免预加载太多导致随机不生效）

      // 应用限制
      const actualCount = Math.max(MIN_PRELOAD, Math.min(targetCount, MAX_PRELOAD))


      return actualCount
    },

    // 预加载音频
    async preloadAudios(count, onLoadProgress) {

      // 检查是否启用了纯互动式模式
      if (this.pureInteractionMode) {
        this.debugLog('纯互动式模式已启用，不会自动播放话术', 'warning')
        this.debugLog('将仅在触发条件（直播互动、临时话术、定时话术）时播放', 'info')

        // 调用完成回调（让UI知道加载已完成）
        if (onLoadProgress) {
          onLoadProgress(0, 0)
        }

        return
      }

      // 检查脚本内容列表是否为空
      if (this.scriptContentList.length === 0) {
        this.debugLog('⚠️ 脚本内容列表为空，无法生成音频', 'error')
        console.error('[LivePane] ⚠️ 脚本内容列表为空，无法生成音频')
        console.error('[LivePane] =====================================')
        console.error('[LivePane] 脚本内容列表为空的可能原因:')
        console.error('[LivePane] 1. 脚本内容字段为空（未在脚本管理中编辑脚本内容）')
        console.error('[LivePane] 2. 脚本内容格式错误（未使用{}包裹片段名）')
        console.error('[LivePane] =====================================')
        console.error('[LivePane] 解决方法:')
        console.error('[LivePane] 1. 打开【脚本管理】页面')
        console.error('[LivePane] 2. 编辑当前脚本')
        console.error('[LivePane] 3. 在脚本内容中添加片段引用，格式如：{片段1}{片段2}{片段3}')
        console.error('[LivePane] 4. 片段名必须与下方【片段列表】中的片段名称一致')
        console.error('[LivePane] =====================================')
        console.error('[LivePane] 当前脚本信息:')
        console.error('[LivePane]   脚本ID:', this.scriptId)
        console.error('[LivePane]   脚本内容:', this.scriptInfo.content)
        console.error('[LivePane]   脚本内容列表:', this.scriptContentList)
        console.error('[LivePane]   可用片段:', Object.keys(this.segmentMap))
        console.error('[LivePane] =====================================')

        this.$message.error('脚本内容为空或格式错误！请在脚本管理中编辑脚本，使用{}格式引用片段，如：{片段1}{片段2}')
        return
      }

      // 使用请求的预加载数量（允许循环播放，不限制为 scriptContentList.length）
      const loadCount = count

      this.debugLog(`开始预加载 ${loadCount} 个音频（支持循环）`, 'info')

      for (let i = 0; i < loadCount; i++) {
        // ⚠️ 每次循环都检查播放状态（防止停播后继续生成）
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog(`⚠️ 播放已停止，中止预加载（已加载 ${i}/${loadCount}）`, 'warning')
          break  // 立即退出循环
        }

        const { word, segmentName, scriptIndex } = this.getNextWord()

        if (!word) {
          this.debugLog(`第${i+1}个音频: 获取话术为空，跳过`, 'warning')
          console.warn('[LivePane] 获取话术为空,跳过')
          continue
        }

        // word 已经在 getSegmentContentWord() 中解析过语法了
        // 这里先替换同音词
        let processedText = this.replaceHomophones(word)

        // 🎯 GPT话术润色（如果启用）
        processedText = await this.polishScriptWithGPT(processedText)

        // ⚠️ 异步操作后再次检查播放状态
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog('⚠️ 润色期间播放已停止，中止预加载', 'warning')
          break
        }

        // 生成音频 URL（使用已处理的文本）
        const audioUrl = await this.generateTTSAudioOnly(processedText)

        // ⚠️ 生成音频后再次检查
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog('⚠️ 生成音频期间播放已停止，中止预加载', 'warning')
          break
        }

        if (!audioUrl) {
          this.debugLog(`第${i+1}个音频: 生成失败`, 'error')
          console.error('[LivePane] 生成音频失败,跳过')
          continue
        }

        // 添加到播放队列
        // 🔧 实时读取最新配置（确保音量使用最新设置）
        this.loadAnchorControlConfig()

        // 计算音量（含随机浮动）
        const volumeBase = this.anchorControlConfig.volume / 100.0  // 0-100 -> 0-1
        const volumeFloat = this.anchorControlConfig.volumeRange / 100.0
        // 随机微调：在基础值的 ±volumeRange% 范围内浮动
        const volumeRandom = (Math.random() * 2 - 1) * volumeBase * volumeFloat
        const volume = Math.max(0, Math.min(1.0, volumeBase + volumeRandom))  // 限制在 0-1.0

        const speaker = this.getCurrentSpeaker()

        this.audioPlayer.addToQueue({
          text: processedText,
          url: audioUrl,
          speaker: speaker,
          segmentName: segmentName,
          scriptIndex: scriptIndex,
          type: 'normal',
          volume: volume
        })

        this.totalLoadedCount++  // 累加已加载音频总数
        this.scriptContentIndex++
        this.debugLog(`第${i+1}个音频已加入队列`, 'success')

        // 调用进度回调
        if (onLoadProgress) {
          onLoadProgress(i + 1, loadCount)
        }
      }

      this.updateQueueStatus()
      const queueLength = this.audioPlayer.getQueueLength().total
      this.debugLog(`预加载完成，队列中有 ${queueLength} 个音频`, 'success')

      if (queueLength === 0) {
        this.debugLog('⚠️ 预加载完成但队列为空！', 'error')
        console.error('[LivePane] ⚠️ 预加载完成但队列为空！')
      }
    },

    // 预加载更多音频 (队列为空时调用)
    async preloadMoreAudios() {

      // 检查是否启用了纯互动式模式
      if (this.pureInteractionMode) {
        this.debugLog('纯互动式模式：队列已空，等待用户触发操作', 'info')
        return
      }

      this.debugLog('队列已空，正在预加载更多音频...', 'warning')

      // 🎯 检查是否启用了自动话术生成
      if (this.gptConfig.autoScriptEnabled) {
        this.debugLog('检测到自动话术生成已启用，使用统一生成方法', 'info')

        // ⚠️ 使用统一的 generateAndAddAutoScript 方法（带互斥标志）
        // 这样可以避免与 onAudioEnd 的检查重复生成
        await this.generateAndAddAutoScript()

        // 检查是否成功生成并加入队列
        const queueLength = this.audioPlayer.getQueueLength().total
        if (queueLength > 0) {
          this.debugLog(`✓ 队列已补充，当前 ${queueLength} 条`, 'success')
          // 启动播放
          this.audioPlayer.start()
          return
        } else {
          this.debugLog('⚠️ GPT生成失败或队列仍为空，将使用普通脚本', 'warning')
          // 继续执行下面的普通脚本预加载
        }
      }

      // 普通脚本预加载（原有逻辑）
      // 计算预加载数量（根据脚本长度和轮数）
      const preloadCount = this.calculatePreloadCount()

      await this.preloadAudios(preloadCount)

      // 预加载完成后，检查队列并重新启动播放
      const queueLength = this.audioPlayer.getQueueLength().total

      if (queueLength > 0) {
        this.debugLog(`预加载完成，继续播放 (已加载${preloadCount}个音频)`, 'success')
        this.audioPlayer.start()
      } else {
        console.error('[LivePane] ⚠️ 预加载完成但队列仍然为空！')
        this.debugLog('⚠️ 预加载失败，队列仍然为空', 'error')
      }
    },

    // 生成 TTS 音频（包含文本处理）
    // 这是一个便捷方法，会先处理文本再生成音频
    // @param {String} text - 原始文本
    // @param {Object} options - 可选参数
    // @param {Boolean} options.skipPolish - 是否跳过润色（默认 false）
    async generateTTSAudio(text, options = {}) {
      const { skipPolish = false } = options

      // 🎯 完整的话术规则解析（处理 []、{}、() 等语法）
      text = this.parseScriptSyntax(text)

      // ⚡ 同音词替换（全局应用到所有音频）
      text = this.replaceHomophones(text)

      // 💎 GPT 实时润色改写话术（如果启用且未跳过）
      console.log('[GPT润色] 检查配置:', {
        polishEnabled: this.gptConfig.polishEnabled,
        autoScriptEnabled: this.gptConfig.autoScriptEnabled,
        skipPolish: skipPolish,
        text: text
      })

      // ⚠️ 如果启用了自动话术功能，跳过润色（因为已经是GPT生成的）
      const shouldSkipPolish = skipPolish || this.gptConfig.autoScriptEnabled

      if (this.gptConfig.polishEnabled && !shouldSkipPolish) {
        try {
          this.debugLog(`🎨 开始GPT润色: "${text.substring(0, 30)}${text.length > 30 ? '...' : ''}"`, 'info')
          console.log('[GPT润色] 调用 gptService.polishScript...')

          const result = await gptService.polishScript(text, this.gptConfig.polishTipWord)

          console.log('[GPT润色] 返回结果:', result)

          if (result.success && result.content) {
            const originalText = text
            text = result.content
            this.debugLog(`✨ GPT润色成功:\n原文: ${originalText}\n润色后: ${text}`, 'success')
            console.log('[GPT润色] 成功 - 原文:', originalText)
            console.log('[GPT润色] 成功 - 润色后:', text)
          } else {
            this.debugLog(`⚠️ GPT润色失败，使用原文: ${result.error || '未返回内容'}`, 'warning')
            console.warn('[GPT润色] 失败:', result.error)
          }
        } catch (error) {
          this.debugLog(`❌ GPT润色异常，使用原文: ${error.message}`, 'warning')
          console.error('[GPT润色] 异常:', error)
        }
      } else {
        if (shouldSkipPolish && this.gptConfig.autoScriptEnabled) {
          console.log('[GPT润色] 自动话术模式，跳过润色')
          this.debugLog('ℹ️ GPT自动话术无需润色', 'info')
        } else {
          console.log('[GPT润色] 功能未启用，跳过润色')
        }
      }

      // 调用实际的音频生成方法
      return await this.generateTTSAudioOnly(text)
    },

    // 生成 TTS 音频（仅生成，不处理文本）
    // 注意：调用此方法前应已处理文本（parseScriptSyntax + replaceHomophones）
    async generateTTSAudioOnly(text) {
      try {
        const { ipcRenderer } = require('electron')
        const speaker = this.getCurrentSpeaker()

        // 🔧 实时读取最新配置（确保使用最新的设置）
        this.loadAnchorControlConfig()

        // 计算语速（含随机浮动）
        // 反向映射：speed越大，length越小（语速越快）
        // 0% → 2.0 (最慢), 50% → 1.0 (正常), 100% → 0.5 (最快)
        let speedBase
        if (this.anchorControlConfig.speed <= 50) {
          // 0-50: 从2.0(慢)到1.0(正常)
          speedBase = 2.0 - (this.anchorControlConfig.speed / 50.0) * 1.0
        } else {
          // 50-100: 从1.0(正常)到0.5(快)
          speedBase = 1.0 - ((this.anchorControlConfig.speed - 50) / 50.0) * 0.5
        }
        // 随机微调：在基础值的 ±speedRange% 范围内浮动
        const speedFloat = this.anchorControlConfig.speedRange / 100.0  // 百分比
        const speedRandom = (Math.random() * 2 - 1) * speedBase * speedFloat  // 基于当前值的百分比
        const length = Math.max(0.5, Math.min(2.0, speedBase + speedRandom))  // 限制在 0.5-2.0

        // 计算语调（含随机浮动）
        // BERT-VITS2 noise 参数说明：
        //   - 0.2: 非常平稳、机械感
        //   - 0.33: 默认值，正常表现力
        //   - 0.5-0.7: 情感丰富
        //   - 0.9+: 情感强烈，可能不稳定
        // 映射：0% → 0.2 (平稳), 50% → 0.33 (默认), 100% → 0.9 (丰富)
        let noiseBase
        if (this.anchorControlConfig.tone <= 50) {
          // 0-50%: 从0.2(平稳)到0.33(正常)
          noiseBase = 0.2 + (this.anchorControlConfig.tone / 50.0) * 0.13
        } else {
          // 50-100%: 从0.33(正常)到0.9(丰富)
          noiseBase = 0.33 + ((this.anchorControlConfig.tone - 50) / 50.0) * 0.57
        }

        const toneFloat = this.anchorControlConfig.toneRange / 100.0
        // 随机微调：在基础值的 ±toneRange% 范围内浮动
        const toneRandom = (Math.random() * 2 - 1) * noiseBase * toneFloat
        const noise = Math.max(0.1, Math.min(1.2, noiseBase + toneRandom))  // 限制在 0.1-1.2
        const noisew = Math.min(1.2, noise + 0.067)  // noisew 略大于 noise（默认差值约0.067）

        this.debugLog(`生成音频: 文本长度=${text.length}, 主播=${speaker}, 语速=${length.toFixed(2)}, 语调=${noise.toFixed(2)}`, 'info')

        const result = await ipcRenderer.invoke('generate-tts-audio', {
          text: text,
          speaker: speaker,
          format: 'wav',
          lang: 'auto',
          length: length,          // 应用计算后的语速
          noise: noise,            // 应用计算后的语调
          noisew: noisew,          // 应用计算后的语调权重
          emotion: 5,
          sdp_ratio: 1.0,
          serverUrl: this.aiServerConfig.serverUrl
        })


        if (result.success) {
          this.debugLog(`音频生成成功: ${result.audioUrl}`, 'success')

          // 🎯 不再在生成时推送，改为在播放时推送（通过 AudioPlayer.onPlayStart 回调）
          // 推送逻辑已移至 setupAudioPlayerCallbacks() 方法中

          return result.audioUrl
        } else {
          console.error(`[LivePane] ✗ 音频生成失败`)
          console.error(`[LivePane]   错误信息: ${result.error}`)
          this.debugLog(`生成音频失败: ${result.error}`, 'error')
          this.$message.error('生成音频失败: ' + result.error)
          return null
        }
      } catch (error) {
        console.error(`[LivePane] ✗ 生成音频异常:`, error)
        this.debugLog(`生成音频异常: ${error.message}`, 'error')
        this.$message.error('生成音频异常: ' + error.message)
        return null
      }
    },

    // 获取当前主播名称
    getCurrentSpeaker() {
      // 从 LivePaneHeader 获取当前选中的主播
      if (this.liveContext && this.liveContext.$refs.livePaneHeader) {
        const mainHost = this.liveContext.$refs.livePaneHeader.mainHost
        if (mainHost) {
          return mainHost
        } else {
          console.warn('[LivePane] ⚠️ 未选择主播，将使用默认主播')
        }
      } else {
        console.warn('[LivePane] ⚠️ 无法访问 LivePaneHeader')
      }
      // 默认主播（从 config.json 的 spk2id 获取）
      const defaultSpeaker = 'nv-1'  // 对应 1-nv 文件夹中的 config.json
      this.debugLog(`未选择主播，使用默认主播: ${defaultSpeaker}`, 'warning')
      return defaultSpeaker
    },

    // 停播（优雅停播：等待当前音频播放完）
    stopPlayHandle() {
      if (this.audioPlayer) {
        // 提示用户：当前片段播放完后停播
        this.$message({
          message: '当前片段播放完以后停播',
          type: 'warning',
          duration: 0, // 0 表示不自动关闭
          showClose: true,
          customClass: 'stop-pending-message'
        })

        this.debugLog('正在停播，等待当前音频播放完成...', 'warning')

        // 设置停播标志（用于禁用开播按钮）
        this.isStopping = true

        // 设置停播完成回调
        this.audioPlayer.onStopComplete = () => {
          this.isPlayingSession = false  // 清除播放会话标志
          this.isStopping = false  // 清除停播标志
          this.liveStartTime = null  // 清除开播时间
          this.currentPlayingIndex = -1  // 清除片段列表高亮

          // 清除当前播放内容显示
          if (this.liveContext && this.liveContext.$refs.livePaneCurrentSegment) {
            this.liveContext.$refs.livePaneCurrentSegment.clearCurrentSegment()
          }

          // 🎯 清除 GPT 自动话术生成定时器（兼容性保留，实际已不再使用）
          if (this.autoScriptGenerationTimer) {
            clearInterval(this.autoScriptGenerationTimer)
            this.autoScriptGenerationTimer = null
            this.debugLog('✓ 已停止 GPT 自动话术生成循环（兼容性清理）', 'info')
          }

          // 关闭停播提示消息
          this.$message.closeAll()
          this.$message.success('已停播')

          // 通知按钮组件更新状态
          if (this.liveContext && this.liveContext.$refs.livePaneControlButtons) {
            this.liveContext.$refs.livePaneControlButtons.isPlaying = false
            this.liveContext.$refs.livePaneControlButtons.isPause = false
          }
        }

        this.audioPlayer.stopGracefully()
      } else {
        this.$message.warning('播放器未初始化')
      }
    },

    // 暂停播放（延迟响应：等待当前音频播放完成）
    pausePlayHandle(onPauseComplete) {
      if (this.audioPlayer) {
        // 检查播放会话标志
        if (!this.isPlayingSession) {
          this.$message.warning('当前没有播放')
          return
        }

        // 提示用户：当前片段播放完后暂停
        this.$message({
          message: '当前片段播放完以后暂停',
          type: 'warning',
          duration: 0, // 0 表示不自动关闭
          showClose: true,
          customClass: 'pause-pending-message'
        })

        this.debugLog('设置暂停标志，当前音频将播放完成后暂停', 'warning')

        // 设置暂停完成回调
        this.audioPlayer.onPauseComplete = () => {
          this.isPlayingSession = false  // 清除播放会话标志

          // 关闭暂停提示消息
          this.$message.closeAll()
          this.$message.success('已暂停')

          if (onPauseComplete) {
            onPauseComplete()
          }
        }

        // 调用 AudioPlayer 的暂停方法（只设置标志）
        this.audioPlayer.pauseGracefully()
        this.updateQueueStatus()
      }
    },

    // 继续播放
    async resumePlayHandle() {
      if (!this.audioPlayer) {
        return
      }


      // 清除暂停状态
      this.audioPlayer.isPaused = false

      // 设置播放会话标志
      this.isPlayingSession = true

      // 检查队列是否为空，如果为空则加载更多片段
      const queueLength = this.audioPlayer.getQueueLength()
      if (queueLength.total === 0) {
        this.debugLog('队列为空，正在加载片段...', 'info')
        await this.loadMoreSegments()
      }

      // 加载主播控制配置
      this.loadAnchorControlConfig()

      // 检查是否设置了"暂停后启动话术"
      const resumeScript = this.anchorControlConfig.resumeScript
      if (resumeScript && resumeScript.trim()) {
        this.debugLog('检测到暂停后启动话术，准备播放...', 'info')

        try {
          // 生成暂停话术的音频
          const audioUrl = await this.generateTTSAudio(resumeScript)

          if (audioUrl) {
            // 计算音量
            const volumeBase = this.anchorControlConfig.volume / 100.0
            const volumeFloat = this.anchorControlConfig.volumeRange / 100.0
            const volumeRandom = (Math.random() * 2 - 1) * volumeBase * volumeFloat
            const volume = Math.max(0, Math.min(1.0, volumeBase + volumeRandom))

            // 添加到互动队列（优先播放）
            this.audioPlayer.addToHudongQueue({
              text: resumeScript,
              url: audioUrl,
              speaker: this.getCurrentSpeaker(),
              type: 'resume',  // 标记为恢复话术
              volume: volume
            })

            this.debugLog('暂停话术已加入优先播放队列', 'success')

            // 开始播放下一个音频（优先播放互动队列中的暂停话术）
            if (!this.audioPlayer.isPlaying) {
              this.audioPlayer.playNext()
            }
            this.updateQueueStatus()
            this.$message.success('继续播放')
            return
          } else {
            this.debugLog('暂停话术音频生成失败', 'error')
          }
        } catch (error) {
          console.error('[LivePane] 生成暂停话术失败:', error)
          this.debugLog('生成暂停话术失败: ' + error.message, 'error')
        }
      }

      // 没有设置暂停话术，直接播放下一个
      if (!this.audioPlayer.isPlaying) {
        this.audioPlayer.playNext()
      }
      this.updateQueueStatus()
      this.$message.success('继续播放')
    },

    // 播放背景音乐
    async playBackgroundMusic(config) {
      if (!this.bgMusicPlayer) {
        this.$message.error('背景音乐播放器未初始化')
        return
      }

      this.debugLog('正在加载背景音乐...', 'info')

      try {
        // 检查音乐目录
        if (!config.musicDir || config.musicDir.trim() === '') {
          this.$message.warning('请先选择背景音乐目录')
          return
        }

        // 扫描音乐文件
        const { ipcRenderer } = require('electron')
        const scanResult = await ipcRenderer.invoke('scan-music-files', {
          musicDir: config.musicDir
        })

        if (!scanResult.success) {
          this.$message.error('扫描音乐文件失败: ' + scanResult.error)
          return
        }

        if (scanResult.count === 0) {
          this.$message.warning('该目录下没有找到音乐文件')
          return
        }


        // 设置音乐目录和文件列表
        this.bgMusicPlayer.setMusicDirectory(config.musicDir, scanResult.files)

        // 设置音量
        this.bgMusicPlayer.setVolume(config.bgVolume)

        // 设置自动回避
        this.bgMusicPlayer.setAutoAvoid(config.autoAvoid, config.bgMusic, config.fadeTime)

        // 设置播放间隔
        this.bgMusicPlayer.setPlayInterval(config.musicInterval)

        // 开始随机播放
        this.bgMusicPlayer.startRandomPlay()

        this.debugLog(`背景音乐已开始播放 (共 ${scanResult.count} 首)`, 'success')
        this.$message.success('背景音乐已开始播放')
      } catch (error) {
        console.error('[LivePane] 播放背景音乐失败:', error)
        this.debugLog('播放背景音乐失败: ' + error.message, 'error')
        this.$message.error('播放背景音乐失败: ' + error.message)
      }
    },

    // 停止背景音乐
    stopBackgroundMusic() {
      if (!this.bgMusicPlayer) {
        return
      }

      this.bgMusicPlayer.stop()
      this.debugLog('背景音乐已停止', 'info')
      this.$message.success('背景音乐已停止')
    },

    // 插入临时话术
    async insertTemporarySpeech(text, type) {

      if (!text || !text.trim()) {
        this.$message.warning('话术内容不能为空')
        return
      }

      // ⚠️ 检查播放状态（防止停播后继续生成）
      if (!this.isPlayingSession || this.isStopping) {
        this.debugLog('⚠️ 播放已停止，取消临时话术生成', 'warning')
        this.$message.warning('当前未在播放，无法插入临时话术')
        return
      }

      try {
        this.debugLog('正在生成临时话术音频...', 'info')

        // 🎯 先处理文本（应用所有规则）
        const processedText = this.parseScriptSyntax(text)
        const finalText = this.replaceHomophones(processedText)

        // 生成临时话术的音频（使用处理后的文本）
        const audioUrl = await this.generateTTSAudioOnly(finalText)

        // ⚠️ 生成音频后再次检查播放状态（异步操作后状态可能已变化）
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog('⚠️ 生成音频期间播放已停止，丢弃临时话术', 'warning')
          this.$message.warning('播放已停止，临时话术已取消')
          return
        }

        if (audioUrl) {
          // 计算音量（使用主播管控配置）
          const volumeBase = this.anchorControlConfig.volume / 100.0
          const volumeFloat = this.anchorControlConfig.volumeRange / 100.0
          const volumeRandom = (Math.random() * 2 - 1) * volumeBase * volumeFloat
          const volume = Math.max(0, Math.min(1.0, volumeBase + volumeRandom))

          // 添加到互动队列（使用处理后的文本）
          this.audioPlayer.addToHudongQueue({
            text: finalText,  // ✓ 使用处理后的文本
            url: audioUrl,
            speaker: this.getCurrentSpeaker(),
            type: 'temporary',  // 标记为临时话术
            volume: volume
          })

          this.totalLoadedCount++  // 累加已加载音频总数
          this.debugLog('临时话术已加入优先播放队列', 'success')

          // 更新队列状态显示
          this.updateQueueStatus()

          this.$message.success('临时话术已插入到下一条')
        } else {
          this.debugLog('临时话术音频生成失败', 'error')
          throw new Error('音频生成失败')
        }
      } catch (error) {
        console.error('[LivePane] 插入临时话术失败:', error)
        this.debugLog('插入临时话术失败: ' + error.message, 'error')
        throw error  // 向上抛出错误，让调用方处理
      }
    },

    // 插入定时话术
    async insertScheduledSpeech(text, type) {

      if (!text || !text.trim()) {
        console.warn('[LivePane] 定时话术内容为空，跳过插入')
        return
      }

      // ⚠️ 检查播放状态（防止停播后继续生成）
      if (!this.isPlayingSession || this.isStopping) {
        this.debugLog('⚠️ 播放已停止，取消定时话术生成', 'warning')
        return
      }

      try {
        this.debugLog('正在生成定时话术音频...', 'info')

        // 🎯 先处理文本（应用所有规则）
        const processedText = this.parseScriptSyntax(text)
        const finalText = this.replaceHomophones(processedText)

        // 生成定时话术的音频（使用处理后的文本）
        const audioUrl = await this.generateTTSAudioOnly(finalText)

        // ⚠️ 生成音频后再次检查播放状态（异步操作后状态可能已变化）
        if (!this.isPlayingSession || this.isStopping) {
          this.debugLog('⚠️ 生成音频期间播放已停止，丢弃定时话术', 'warning')
          return
        }

        if (audioUrl) {
          // 计算音量（使用主播管控配置）
          const volumeBase = this.anchorControlConfig.volume / 100.0
          const volumeFloat = this.anchorControlConfig.volumeRange / 100.0
          const volumeRandom = (Math.random() * 2 - 1) * volumeBase * volumeFloat
          const volume = Math.max(0, Math.min(1.0, volumeBase + volumeRandom))

          // 添加到互动队列（使用处理后的文本）
          this.audioPlayer.addToHudongQueue({
            text: finalText,  // ✓ 使用处理后的文本
            url: audioUrl,
            speaker: this.getCurrentSpeaker(),
            type: 'scheduled',  // 标记为定时话术
            volume: volume
          })

          this.totalLoadedCount++  // 累加已加载音频总数
          this.debugLog('定时话术已加入优先播放队列', 'success')

          // 更新队列状态显示
          this.updateQueueStatus()

        } else {
          this.debugLog('定时话术音频生成失败', 'error')
          throw new Error('音频生成失败')
        }
      } catch (error) {
        console.error('[LivePane] 插入定时话术失败:', error)
        this.debugLog('插入定时话术失败: ' + error.message, 'error')
        // 定时话术失败不向上抛出错误，只记录日志
      }
    },

    // 开启主播话术生成定时器
    startWordTimer() {
      // 开启主播话术生成
      this.wordTimer = setInterval(async () => {
        if (this.wordTimerIsRunning) {
          return
        }
        this.wordTimerIsRunning = true
        try {
           // 如果未播放的话术数量大于N，则不生成话术
          if (this.scriptTotalCount - this.playIndex >= this.wordTimerMaxCount) {
            return
          }

          const {word, segmentName} = this.getNextWord()
          if (word == '') {
            this.$message.warning('生成话术为空，请检查脚本配置')
            return
          }

          // 生成音频链接
          const res  = await this.generateAudioUrl(word)
          this.scriptContentIndex++

          // 使用封装的方法添加新数据，默认添加到尾部
          this.addTableData({
            name: segmentName,
            type: '主播话术', // 默认为主播话术，后续可根据具体逻辑调整
            text: word,
            audioUrl: res.data
          }, 'tail')

          this.scriptTotalCount = this.tableData.length
        } catch (error) {
          console.error(error)
        } finally {
          this.wordTimerIsRunning = false
        }
      }, this.wordTimerInterval)
    },
    // 开启播放定时器
    startPlayTimer() {
      this.playTimer = setInterval(() => {
        if(this.liveContext.$refs.livePaneCurrentSegment.isPlaying) {
          return
        }
        if (this.playIndex <= this.scriptTotalCount) {
          const currentItem = this.tableData.find(item => item.index == this.playIndex)
          currentItem.play = true
          const word = currentItem.text
          const audioUrl = currentItem.audioUrl
          this.liveContext.$refs.livePaneCurrentSegment.playAudio(currentItem.index, word, audioUrl)
          this.playIndex++
        }
      }, this.playTimerInterval)
    },

    // 获取脚本信息
    getLiveScriptInfo() {
      return this.$http.get(`/apps/live-script/info/${this.scriptId}`).then(({data}) => {

        if (!data.liveScript.content || data.liveScript.content.trim() === '') {
          console.error('[LivePane] ⚠️ 脚本内容为空！')
          console.error('[LivePane] 提示: 脚本内容需要包含片段引用，格式如: {片段1}{片段2}{片段3}')
          this.debugLog('⚠️ 脚本内容为空！请在脚本管理中编辑脚本，添加片段引用（格式：{片段名}）', 'error')
        } else {
        }

        this.scriptInfo = data.liveScript
        // 解析脚本内容，将{}{}{}格式的内容拆分成数组
        this.scriptContentList = this.parseScriptContent(data.liveScript.content)
        this.scriptContentIndex = 0


        if (this.scriptContentList.length === 0) {
          console.error('[LivePane] ⚠️ 解析后的脚本内容列表为空！')
          console.error('[LivePane] 可能原因:')
          console.error('[LivePane]   1. 脚本内容为空')
          console.error('[LivePane]   2. 脚本内容格式不正确（没有使用{}包裹片段名）')
          console.error('[LivePane]   3. 正确格式示例: {片段1}{片段2}{片段3}')
          this.debugLog('⚠️ 脚本格式错误！脚本内容需要使用{}包裹片段名，如：{片段1}{片段2}', 'error')
        } else {
          this.debugLog(`脚本信息加载成功，包含${this.scriptContentList.length}个片段引用`, 'success')
          // 初始化所有随机选择（让随机标签立即显示）
          this.initRandomSelections()
        }
      }).catch(error => {
        console.error('[LivePane] 获取脚本信息失败:', error)
        this.debugLog('获取脚本信息失败: ' + error.message, 'error')
      })
    },

    // 初始化所有随机选择（在脚本加载后立即调用，让随机标签显示）
    initRandomSelections() {
      this.randomSelections = {}

      this.scriptContentList.forEach((scriptContent, index) => {
        if (scriptContent.type === 'random' && scriptContent.options && scriptContent.options.length > 0) {
          // 随机选择一个
          const randomIndex = Math.floor(Math.random() * scriptContent.options.length)
          const selectedSegment = scriptContent.options[randomIndex]

          this.$set(this.randomSelections, index, selectedSegment)
        }
      })

    },

    // 解析脚本内容
    parseScriptContent(content) {

      if (!content) {
        return []
      }


      const result = []
      let currentIndex = 0
      let parseCount = 0

      while (currentIndex < content.length) {
        // 查找下一个左花括号
        const leftBraceIndex = content.indexOf('{', currentIndex)
        if (leftBraceIndex === -1) {
          break
        }

        // 查找对应的右花括号
        let rightBraceIndex = content.indexOf('}', leftBraceIndex)
        if (rightBraceIndex === -1) {
          break
        }

        // 提取花括号内的内容
        const contentInside = content.slice(leftBraceIndex + 1, rightBraceIndex)
        parseCount++

        // 检查是否是随机选项（包含|符号）
        if (contentInside.includes('|')) {
          // 将随机选项拆分成数组
          const options = contentInside.split('|')
          result.push({
            type: 'random',
            options: options
          })
        } else {
          // 普通片段引用
          result.push({
            type: 'segment',
            name: contentInside
          })
        }

        // 移动到右花括号后继续查找
        currentIndex = rightBraceIndex + 1
      }

      return result
    },
    // 获取脚本片段列表
    getLiveSegmentList() {
      return this.$http.get('/apps/live-segment/all', {
        params: {
          scriptId: this.scriptId
        }
      }).then(({data}) => {

        this.segmentList = data.list
        // 将片段列表转换为map,key为片段名称,value为片段内容
        this.segmentMap = {}
        data.list.forEach(item => {
          this.segmentMap[item.name] = item.content
        })

        this.debugLog(`片段列表加载成功，共${data.list.length}个片段`, 'success')
      }).catch(error => {
        console.error('[LivePane] 获取片段列表失败:', error)
        this.debugLog('获取片段列表失败: ' + error.message, 'error')
      })
    },
    // 加载片段列表（供外部调用）
    loadSegmentList(scriptId) {
      if (!scriptId) {
        return
      }
      this.scriptId = scriptId

      // 🎯 同时加载脚本信息和片段列表，确保表格显示顺序正确
      Promise.all([
        // 1. 加载脚本信息（获取脚本内容，用于解析顺序）
        this.$http.get(`/apps/live-script/info/${scriptId}`),
        // 2. 加载片段列表
        this.$http.get('/apps/live-segment/all', {
          params: { scriptId: scriptId }
        })
      ]).then(([scriptRes, segmentRes]) => {
        // 处理脚本信息
        const scriptInfo = scriptRes.data.liveScript
        this.scriptInfo = scriptInfo
        // 解析脚本内容，生成 scriptContentList（用于表格排序）
        this.scriptContentList = this.parseScriptContent(scriptInfo.content)
        // 初始化随机选择
        this.initRandomSelections()

        // 处理片段列表
        this.segmentList = segmentRes.data.list
        this.segmentMap = {}
        segmentRes.data.list.forEach(item => {
          this.segmentMap[item.name] = item.content
        })
      }).catch(error => {
        console.error('[LivePane] 加载数据失败:', error)
      })
    },
    // 刷新片段列表（供外部调用）
    refreshSegmentList(scriptId) {
      return new Promise((resolve, reject) => {
        if (!scriptId) {
          reject(new Error('脚本ID不能为空'))
          return
        }
        this.scriptId = scriptId

        // 清空随机选择，让随机片段重新随机
        this.randomSelections = {}

        // 🎯 同时加载脚本信息和片段列表
        Promise.all([
          // 1. 加载脚本信息
          this.$http.get(`/apps/live-script/info/${scriptId}`),
          // 2. 加载片段列表
          this.$http.get('/apps/live-segment/all', {
            params: { scriptId: scriptId }
          })
        ]).then(([scriptRes, segmentRes]) => {
          // 处理脚本信息
          const scriptInfo = scriptRes.data.liveScript
          this.scriptInfo = scriptInfo
          // 解析脚本内容
          this.scriptContentList = this.parseScriptContent(scriptInfo.content)
          // 初始化随机选择
          this.initRandomSelections()

          // 处理片段列表
          this.segmentList = segmentRes.data.list
          this.segmentMap = {}
          segmentRes.data.list.forEach(item => {
            this.segmentMap[item.name] = item.content
          })
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 获取下一个话术
    getNextWord() {

      // 如果脚本内容列表为空，则返回空
      if (this.scriptContentList.length == 0) {
        console.warn('[LivePane] 脚本内容列表为空')
        return {
          word: '',
          segmentName: '',
          scriptIndex: -1,
        }
      }

      // 如果脚本内容索引大于等于脚本内容列表长度，则重置脚本内容索引
      if (this.scriptContentIndex >= this.scriptContentList.length) {
        this.scriptContentIndex = 0
      }

      // 记录当前使用的索引（在重置之后）
      const currentIndex = this.scriptContentIndex

      let scriptContent = this.scriptContentList[this.scriptContentIndex]

      let segmentName = ''
      // 如果脚本内容包含随机选项，每次都重新随机选择
      if (scriptContent.type == 'random') {
        const optionList = scriptContent.options

        // 🎯 每次都重新随机选择（不使用缓存）
        const randomIndex = Math.floor(Math.random() * optionList.length)
        segmentName = optionList[randomIndex]


      } else if (scriptContent.type == 'segment') {
        segmentName = scriptContent.name
      }

      // 拿到片段内容
      const segmentContent = this.segmentMap[segmentName]

      if (!segmentContent) {
        console.error(`[LivePane] ⚠️ 片段[${segmentName}]在片段Map中不存在！`)
        console.error(`[LivePane] 可用的片段:`, Object.keys(this.segmentMap))
      }

      const word = this.getSegmentContentWord(segmentContent)

      return {
        word: word,
        segmentName: segmentName,
        scriptIndex: currentIndex,  // 返回实际使用的索引
      }
    },

    // 获取片段内容中的话术
    getSegmentContentWord(content) {
      if (!content) {
        return ''
      }

      // 🎯 直接使用完整内容，不进行分割和随机选择
      // 只使用统一的话术语法解析方法（处理所有规则：[]、{}、() 等）
      const word = this.parseScriptSyntax(content.trim())

      return word
    },

    // 生成音频链接
    generateAudioUrl(word) {
      // 生成音频链接
      return this.$http.audioPost('/api/audio/generateAudio', {
        text: word
      })
    },

    // 获取类型对应的CSS类
    getTypeClass(type) {
      switch (type) {
        case '主播话术':
          return 'type-host'
        case '关键词互动':
          return 'type-keyword'
        case '动作互动':
          return 'type-action'
        default:
          return 'type-default'
      }
    },

    /**
     * 添加新的表格数据
     * @param {Object} data - 要添加的数据对象，包含 name, type, text, audioUrl 等字段
     * @param {String} position - 插入位置：'next' 当前播放的下一个位置, 'tail' 尾部
     */
    addTableData(data, position = 'tail') {
      const newItem = {
        play: false,
        name: data.name || '',
        type: data.type || '主播话术',
        text: data.text || '',
        audioUrl: data.audioUrl || '',
        ...data // 允许传入其他字段
      }

      if (position === 'next') {
        // 插入到当前播放位置的下一个
        const insertIndex = this.playIndex
        this.tableData.splice(insertIndex, 0, newItem)
        // 重新计算所有项的索引
        this.reorderTableDataIndex()
      } else if (position === 'tail') {
        // 插入到尾部
        newItem.index = this.tableData.length + 1
        this.tableData.push(newItem)
      }
    },

    /**
     * 重新排列表格数据的索引
     */
    reorderTableDataIndex() {
      this.tableData.forEach((item, index) => {
        item.index = index + 1
      })
    },

    /**
     * 公开方法：添加主播话术
     * @param {String} text - 话术内容
     * @param {String} segmentName - 片段名称
     * @param {String} position - 插入位置：'next' 或 'tail'
     */
    addHostScript(text, segmentName = '', position = 'tail') {
      this.addTableData({
        name: segmentName,
        type: '主播话术',
        text: text
      }, position)
      this.scriptTotalCount = this.tableData.length
    },

    /**
     * 公开方法：添加关键词互动
     * @param {String} text - 互动内容
     * @param {String} segmentName - 片段名称
     * @param {String} position - 插入位置：'next' 或 'tail'
     */
    async addKeywordInteraction(text, segmentName = '', position = 'next') {
      const res  = await this.generateAudioUrl(text)
      const audioUrl = res.data
      this.addTableData({
        name: segmentName,
        type: '关键词互动',
        text: text,
        audioUrl: audioUrl
      }, position)
      this.scriptTotalCount = this.tableData.length
    },

    /**
     * 公开方法：添加动作互动
     * @param {String} text - 动作内容
     * @param {String} segmentName - 片段名称
     * @param {String} position - 插入位置：'next' 或 'tail'
     */
    async addActionInteraction(text, segmentName = '', position = 'next') {
      const res  = await this.generateAudioUrl(text)
      const audioUrl = res.data
      this.addTableData({
        name: segmentName,
        type: '动作互动',
        text: text,
        audioUrl: audioUrl
      }, position)
      this.scriptTotalCount = this.tableData.length
    },

    /**
     * 试听片段（播放前50个字）
     */
    async handlePreview(row) {
      try {
        // 获取片段内容
        const content = row.content
        if (!content || content.trim() === '') {
          this.$message.warning('片段内容为空，无法试听')
          return
        }

        // 截取前50个字
        const previewText = content.substring(0, 50)

        // 显示黄色提示
        this.$message({
          message: '当前试听前50个字，完整播放请开播',
          type: 'warning',
          duration: 3000
        })

        // 生成试听音频
        this.debugLog(`试听片段: ${row.name}（前50字）`, 'info')
        const audioUrl = await this.generateTTSAudio(previewText)

        if (!audioUrl) {
          this.$message.error('音频生成失败')
          return
        }

        // 播放试听音频（不加入队列，直接播放）
        const { Howl } = require('howler')
        const previewSound = new Howl({
          src: [audioUrl],
          format: ['wav'],
          volume: 1.0,
          onplay: () => {
            this.debugLog('试听播放中...', 'info')
          },
          onend: () => {
            this.debugLog('试听播放结束', 'success')
          },
          onloaderror: (id, error) => {
            console.error('[LivePane] 试听音频加载失败:', error)
            this.$message.error('音频加载失败')
          },
          onplayerror: (id, error) => {
            console.error('[LivePane] 试听音频播放失败:', error)
            this.$message.error('音频播放失败')
          }
        })

        previewSound.play()
      } catch (error) {
        console.error('[LivePane] 试听失败:', error)
        this.$message.error('试听失败: ' + error.message)
      }
    },

    /**
     * 显示弹窗（待实现功能）
     */
    handleShowDialog(row) {
      this.$message.info('弹窗功能开发中，片段: ' + row.name)
      // TODO: 后续实现弹窗功能
    },

    /**
     * 完整的话术规则解析
     * 支持的语法：
     * 1. [] 中括号：随机选择其中一组，如 [xx{xxx}x][yy{yyy}y] 随机选一个
     * 2. {} 花括号：随机选择内容，如 {欢迎|感谢} 随机选一个
     * 3. () 小括号：
     *    - (510) → 随机停顿5或10秒
     *    - (我是助手|我是助播) → 随机助播话术
     *    - (c1.mp3|c2.mp3) → 随机音频
     * 4. | 竖线：用于 {} 和 () 内的随机选择
     *
     * @param {String} text - 原始话术文本
     * @returns {String} - 处理后的文本
     */
    parseScriptSyntax(text) {
      if (!text) return text

      try {
        let result = text
        let previousResult = ''
        let iterations = 0
        const MAX_ITERATIONS = 10  // 防止无限循环


        // 循环处理，直到文本不再变化（处理嵌套的花括号）
        while (result !== previousResult && iterations < MAX_ITERATIONS) {
          previousResult = result
          iterations++


          // 1. 处理 [] 中括号：随机选择其中一组
          result = this.processSquareBrackets(result)

          // 2. 处理 {} 花括号：随机选择内容（排除特殊变量）
          result = this.processCurlyBraces(result)

          // 3. 处理 () 小括号：停顿时间、随机话术、随机音频
          result = this.processParentheses(result)
        }

        if (iterations >= MAX_ITERATIONS) {
          console.warn('[parseScriptSyntax] 达到最大迭代次数，可能存在循环引用')
        }


        return result
      } catch (error) {
        console.error('[LivePane] 话术规则解析失败:', error)
        return text
      }
    },

    /**
     * 处理 [] 中括号：随机选择其中一组
     * 例：[xx{xxx}x][yy{yyy}y] → 随机选择第一个或第二个
     */
    processSquareBrackets(text) {
      // 匹配所有连续的 [] 组
      const bracketGroups = []
      let currentPos = 0

      while (currentPos < text.length) {
        const startIdx = text.indexOf('[', currentPos)
        if (startIdx === -1) break

        // 找到匹配的右括号
        let depth = 0
        let endIdx = -1
        for (let i = startIdx; i < text.length; i++) {
          if (text[i] === '[') depth++
          if (text[i] === ']') {
            depth--
            if (depth === 0) {
              endIdx = i
              break
            }
          }
        }

        if (endIdx === -1) break

        // 提取内容
        const content = text.substring(startIdx + 1, endIdx)

        // 检查后面是否还有连续的 []
        let nextStart = endIdx + 1
        const group = { start: startIdx, end: endIdx, contents: [content] }

        while (nextStart < text.length && text[nextStart] === '[') {
          let nextDepth = 0
          let nextEnd = -1
          for (let i = nextStart; i < text.length; i++) {
            if (text[i] === '[') nextDepth++
            if (text[i] === ']') {
              nextDepth--
              if (nextDepth === 0) {
                nextEnd = i
                break
              }
            }
          }

          if (nextEnd === -1) break

          const nextContent = text.substring(nextStart + 1, nextEnd)
          group.contents.push(nextContent)
          group.end = nextEnd
          nextStart = nextEnd + 1
        }

        if (group.contents.length > 1) {
          bracketGroups.push(group)
        }

        currentPos = group.end + 1
      }

      // 从后往前替换（避免索引变化）
      for (let i = bracketGroups.length - 1; i >= 0; i--) {
        const group = bracketGroups[i]
        const randomContent = group.contents[Math.floor(Math.random() * group.contents.length)]
        const before = text.substring(0, group.start)
        const after = text.substring(group.end + 1)
        text = before + randomContent + after
      }

      return text
    },

    /**
     * 处理 {} 花括号：随机选择内容
     * 例：{欢迎|感谢} → 随机选"欢迎"或"感谢"
     * 注意：排除特殊变量如 {当前日期}、{当前时间}、{星期}
     */
    processCurlyBraces(text) {
      let result = text
      let iterations = 0
      const MAX_ITERATIONS = 20

      while (iterations < MAX_ITERATIONS) {
        iterations++
        let changed = false


        // 使用正则匹配最内层的花括号（不包含其他花括号的）
        const regex = /\{([^{}]+)\}/g
        const newResult = result.replace(regex, (match, content) => {

          // 特殊变量：保持原样
          if (content.includes('当前') || content.includes('星期')) {
            return match
          }

          // 处理随机选择
          if (content.includes('|')) {
            const options = content.split('|').map(s => s.trim()).filter(s => s)
            if (options.length > 0) {
              const selected = options[Math.floor(Math.random() * options.length)]
              changed = true
              return selected
            }
          }

          // 没有竖线，直接返回内容
          changed = true
          return content
        })

        result = newResult

        // 如果没有发生变化，说明没有可处理的花括号了
        if (!changed) {
          break
        }
      }

      if (iterations >= MAX_ITERATIONS) {
        console.warn(`[processCurlyBraces] 达到最大迭代次数`)
      }

      return result
    },

    /**
     * 处理 () 小括号
     * 1. (5|10) → 随机停顿5或10秒（有竖线才随机）
     * 2. (我是助手|我是助播) → 随机助播话术
     * 3. (c1.mp3|c2.mp3) → 随机音频文件
     * 注意：必须有竖线才会随机选择！
     */
    processParentheses(text) {
      return text.replace(/\(([^)]+)\)/g, (match, content) => {
        // 检查是否包含竖线
        if (content.includes('|')) {
          const options = content.split('|').map(s => s.trim()).filter(s => s)
          if (options.length > 0) {
            const selected = options[Math.floor(Math.random() * options.length)]

            // 如果是纯数字（停顿时间），返回空字符串
            if (/^\d+$/.test(selected)) {
              return ''  // 停顿不需要在文本中显示
            }

            // 如果是音频文件，返回空字符串（需要后续特殊处理）
            if (selected.includes('.mp3') || selected.includes('.wav') || selected.includes('.mp4')) {
              return ''  // 音频文件不需要在文本中显示
            }

            // 普通话术，直接返回
            return selected
          }
        }

        // 没有竖线，直接返回原内容（去掉小括号）
        return content
      })
    },

    /**
     * 同音词替换（全局应用到所有音频）
     * @param {String} text - 原始文本
     * @returns {String} - 替换后的文本
     */
    replaceHomophones(text) {
      if (!text) return text

      try {
        const saved = localStorage.getItem('homophone_config')
        if (!saved || !saved.trim()) return text

        // 解析同音词规则
        const lines = saved.split('\n').filter(line => line.trim())
        let result = text

        for (const line of lines) {
          const parts = line.split('=')
          if (parts.length === 2) {
            const original = parts[0].trim()
            const replacement = parts[1].trim()
            if (original && replacement) {
              // 全局替换
              result = result.replace(new RegExp(original, 'g'), replacement)
            }
          }
        }

        // 如果发生了替换，记录日志
        if (result !== text) {
        }

        return result
      } catch (error) {
        console.error('[LivePane] 同音词替换失败:', error)
        return text
      }
    },

    /**
     * 获取直播状态信息（供 LivePaneHeader 调用）
     */
    getLiveStatus() {
      return {
        isPlaying: this.isPlayingSession,
        playedCount: this.playedAudioCount,  // 已播放完成的音频条数
        loadedCount: this.totalLoadedCount,  // 累计已加载的音频总数（只增不减，不含句间结束音）
        startTime: this.liveStartTime  // 开播时间
      }
    }
  }
}
</script>
<style scoped lang="scss">
.live-pane-segment-table {
  width: 100%;
  background: #fff;
  border-radius: 4px;
  padding: 12px 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 0 8px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }
  }

  // 表格圆角样式
  ::v-deep .el-table {
    border-radius: 4px;
    overflow: hidden;
  }

  // 当前播放行的高亮样式（使用柔和的淡蓝色背景）
  ::v-deep .current-playing-row {
    background-color: #e6f7ff !important;  // 淡蓝色背景

    // hover 时保持高亮
    &:hover > td {
      background-color: #d9f0ff !important;
    }
  }

  // GPT自动话术模式提示
  .gpt-auto-mode-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    padding: 40px;
  }
}
</style>
