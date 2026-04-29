<template>
  <div>
    <el-dialog title="主播调整" :visible.sync="dialogVisible" width="50%" top="5vh" :close-on-click-modal="false"
               custom-class="anchor-control-dialog"
               :before-close="handleClose">
      <!-- 内容区域设置最大高度，超出滚动 -->
      <div class="dialog-content-scroll">
        <el-form label-width="180px" size="small">
        <el-divider content-position="left" class="red">主播调整</el-divider>
        <!-- 主播调整部分 -->
        <el-form-item label="语速">
          <el-slider v-model="speed" :min="0" :max="100" :step="1" show-input></el-slider>
          <div style="margin-top: 16px;">
            <span style="color: #909399; font-size: 13px;">随机微调 ±</span>
            <el-input-number v-model="speedRange" :min="0" :max="100" :step="1" :precision="0"
                             size="small" controls-position="right" style="width: 120px; margin: 0 8px;"></el-input-number>
            <span style="color: #909399; font-size: 13px;">%（让每句话的语速有细微差异，更自然）</span>
          </div>
        </el-form-item>
        <el-form-item label="语调">
          <el-slider v-model="tone" :min="0" :max="100" :step="1" show-input></el-slider>
          <div style="margin-top: 16px;">
            <span style="color: #909399; font-size: 13px;">随机微调 ±</span>
            <el-input-number v-model="toneRange" :min="0" :max="100" :step="1" :precision="0"
                             size="small" controls-position="right" style="width: 120px; margin: 0 8px;"></el-input-number>
            <span style="color: #909399; font-size: 13px;">%（让每句话的语调有细微差异，更自然）</span>
          </div>
        </el-form-item>

        <el-form-item label="音量">
          <el-slider v-model="volume" :min="0" :max="100" :step="1" show-input></el-slider>
          <div style="margin-top: 16px;">
            <span style="color: #909399; font-size: 13px;">随机微调 ±</span>
            <el-input-number v-model="volumeRange" :min="0" :max="100" :step="1" :precision="0"
                             size="small" controls-position="right" style="width: 120px; margin: 0 8px;"></el-input-number>
            <span style="color: #909399; font-size: 13px;">%（让每句话的音量有细微差异，更自然）</span>
          </div>
        </el-form-item>

        <el-form-item label="暂停后启动话术">
          <el-input type="text" size="small" :rows="2" placeholder="请输入话术" v-model="resumeScript"></el-input>
        </el-form-item>

        <!-- 互动设置部分 -->
        <el-divider content-position="left" class="red">互动设置</el-divider>
        <el-form-item label="自动互动">
          <el-checkbox v-model="autoInteraction">启用</el-checkbox>
          <el-tag class="tip-text" size="small">在AI话术中的每个句号后，进行互动。</el-tag>
        </el-form-item>

        <el-form-item label="观众发言互动概率(%)">
          <el-input-number size="small" v-model="chatInteractionProb" :min="0" :max="100" :step="1"
                           :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">观众的打字</el-tag>
        </el-form-item>

        <el-form-item label="观众行为互动概率(%)">
          <el-input-number size="small" v-model="enterInteractionProb" :min="0" :max="100" :step="1"
                           :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">仅控制：进入直播间</el-tag>
        </el-form-item>

        <el-form-item label="观众行为互动概率(%)">
          <el-input-number size="small" v-model="actionInteractionProb" :min="0" :max="100" :step="1"
                           :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">控制：进入直播间之外的行为，如：点赞、礼物等</el-tag>
        </el-form-item>

        <!-- 背景音乐部分 -->
        <el-divider content-position="left" class="red">背景音乐</el-divider>
        <el-form-item label="自动回避">
          <el-checkbox v-model="autoAvoid">启用</el-checkbox>
          <div style="margin-top: 16px;">
            <span style="color: #606266; font-size: 13px; display: block; margin-bottom: 8px;">
              回避音量百分比（主播说话时，背景音降低到基础音量的百分之几）：
            </span>
            <el-slider v-model="bgMusic" :min="0" :max="100" :step="1" show-input></el-slider>
          </div>
          <div style="margin-top: 16px;">
            <span style="color: #606266; font-size: 13px; display: block; margin-bottom: 8px;">
              淡入淡出时长（单位：毫秒，1秒=1000毫秒）：
            </span>
            <el-slider v-model="fadeTime" :min="100" :max="3000" :step="100" show-input></el-slider>
          </div>
        </el-form-item>

        <el-form-item label="背景音">
          <div class="bg-music">
            <div class="margin-r-10">
              <el-button size="small" type="primary" @click="selectMusicDir">选择背景音目录</el-button>
            </div>
            <el-input size="small" v-model="musicDir" placeholder="请选择目录" readonly></el-input>
            <div class="margin-l-10 margin-r-10">
              <el-button size="small" type="success"
                         @click="randomPlayBgMusic">随机播放
              </el-button>
            </div>
            <div>
              <el-button size="small" type="warning" @click="stopBgMusic">停止</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="背景音量">
          <el-slider v-model="bgVolume" :min="0" :max="100" :step="1" show-input></el-slider>
        </el-form-item>

        <el-form-item label="播放间隔(秒)">
          <el-input-number size="small" v-model="musicInterval" :min="0" :step="1" :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">随机播放背景音时，切换的间隔时长（单位：秒）</el-tag>
        </el-form-item>

        <!-- 其他设置 -->
        <el-divider content-position="left" class="red">其他设置</el-divider>
        <el-form-item label="句间结束音">
          <el-checkbox v-model="endSound">启用</el-checkbox>
          <el-tag class="tip-text" size="small">是否在句间插入随机音效，可在本软件安装目录中替换音效，如X:AILive\extra\space\</el-tag>
        </el-form-item>

        <el-form-item label="随机停顿">
          <el-checkbox v-model="randomPause">启用</el-checkbox>
          <el-input-number size="small" v-model="pauseMax" :min="0" :step="1" :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">每句话 在0~设定值 范围内随机停顿时长，单位：秒</el-tag>
        </el-form-item>

        <el-form-item label="加载话术轮数">
          <el-input-number size="small" v-model="scriptRounds" :min="1" :step="1" :precision="0"></el-input-number>
          <el-tag class="tip-text" size="small">一次性加载多少遍话术：这样会减少从头开始播放时的进度条出现次数；但是云端修改话术后，当前已经加载的话术无法实时修改，除非手动刷新脚本！</el-tag>
        </el-form-item>
      </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="callback()">关闭</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "AnchorControlDialog",
  data() {
    return {
      dialogVisible: false,
      initialData: null,
      // 主播调整
      speed: 50,
      speedRandom: true,
      speedRange: 0,
      tone: 50,
      toneRandom: true,
      toneRange: 0,
      volume: 100,
      volumeRandom: true,
      volumeRange: 0,
      resumeScript: "是的。(姐姐们哥哥们姐妹宝宝们)，想要的抓紧时间了。",

      // 互动设置
      autoInteraction: true,
      chatInteractionProb: 100,
      enterInteractionProb: 100,
      actionInteractionProb: 100,

      // 背景音乐
      autoAvoid: true,
      bgMusic: 20,
      fadeTime: 1000,
      musicDir: "",
      bgVolume: 100,
      musicInterval: 10,

      // 其他设置
      endSound: true,
      randomPause: true,
      pauseMax: 5,
      scriptRounds: 10
    }
  },
  created() {
    // 从 localStorage 恢复配置
    this.loadConfig()
  },
  computed: {
    hasChanges() {
      if (!this.initialData) return false
      return JSON.stringify(this.getCurrentData()) !== JSON.stringify(this.initialData)
    }
  },
  methods: {
    // 获取当前表单数据
    getCurrentData() {
      return {
        speed: this.speed,
        speedRange: this.speedRange,
        tone: this.tone,
        toneRange: this.toneRange,
        volume: this.volume,
        volumeRange: this.volumeRange,
        resumeScript: this.resumeScript,
        autoInteraction: this.autoInteraction,
        chatInteractionProb: this.chatInteractionProb,
        enterInteractionProb: this.enterInteractionProb,
        actionInteractionProb: this.actionInteractionProb,
        autoAvoid: this.autoAvoid,
        bgMusic: this.bgMusic,
        fadeTime: this.fadeTime,
        musicDir: this.musicDir,
        bgVolume: this.bgVolume,
        musicInterval: this.musicInterval,
        endSound: this.endSound,
        randomPause: this.randomPause,
        pauseMax: this.pauseMax,
        scriptRounds: this.scriptRounds
      }
    },

    // 处理关闭对话框
    handleClose(done) {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要关闭吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          done()
        }).catch(() => {})
      } else {
        done()
      }
    },

    // 加载配置
    loadConfig() {
      const savedConfig = localStorage.getItem('anchorControlConfig')
      if (savedConfig) {
        try {
          const config = JSON.parse(savedConfig)
          // 恢复所有配置
          this.speed = config.speed !== undefined ? config.speed : 50
          this.speedRange = config.speedRange !== undefined ? config.speedRange : 0
          this.tone = config.tone !== undefined ? config.tone : 50
          this.toneRange = config.toneRange !== undefined ? config.toneRange : 0
          this.volume = config.volume !== undefined ? config.volume : 100
          this.volumeRange = config.volumeRange !== undefined ? config.volumeRange : 0
          this.resumeScript = config.resumeScript || "是的。(姐姐们哥哥们姐妹宝宝们)，想要的抓紧时间了。"

          this.autoInteraction = config.autoInteraction !== undefined ? config.autoInteraction : true
          this.chatInteractionProb = config.chatInteractionProb !== undefined ? config.chatInteractionProb : 100
          this.enterInteractionProb = config.enterInteractionProb !== undefined ? config.enterInteractionProb : 100
          this.actionInteractionProb = config.actionInteractionProb !== undefined ? config.actionInteractionProb : 100

          this.autoAvoid = config.autoAvoid !== undefined ? config.autoAvoid : true
          this.bgMusic = config.bgMusic !== undefined ? config.bgMusic : 20
          this.fadeTime = config.fadeTime !== undefined ? config.fadeTime : 1000
          this.musicDir = config.musicDir || ""
          this.bgVolume = config.bgVolume !== undefined ? config.bgVolume : 100
          this.musicInterval = config.musicInterval !== undefined ? config.musicInterval : 10

          this.endSound = config.endSound !== undefined ? config.endSound : true
          this.randomPause = config.randomPause !== undefined ? config.randomPause : true
          this.pauseMax = config.pauseMax !== undefined ? config.pauseMax : 5
          this.scriptRounds = config.scriptRounds !== undefined ? config.scriptRounds : 10
        } catch (e) {
          console.error('[AnchorControl] 加载配置失败:', e)
        }
      }

      // 保存初始数据快照
      this.$nextTick(() => {
        this.initialData = this.getCurrentData()
      })
    },

    callback() {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要取消吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '返回编辑',
          type: 'warning'
        }).then(() => {
          this.dialogVisible = false
        }).catch(() => {})
      } else {
        this.dialogVisible = false
      }
    },
    async selectMusicDir() {
      // 调用 Electron IPC 选择目录
      const { ipcRenderer } = require('electron')
      try {
        const result = await ipcRenderer.invoke('select-music-directory')

        if (result.success) {
          this.musicDir = result.path
          this.$message.success(`已选择目录: ${result.path}`)

          // 扫描音乐文件
          const scanResult = await ipcRenderer.invoke('scan-music-files', {
            musicDir: result.path
          })

          if (scanResult.success) {
            this.$message.success(`找到 ${scanResult.count} 个音乐文件`)
          } else {
            this.$message.error('扫描音乐文件失败: ' + scanResult.error)
          }
        } else if (!result.canceled) {
          this.$message.error('选择目录失败: ' + (result.error || '未知错误'))
        }
      } catch (error) {
        console.error('[AnchorControl] 选择音乐目录失败:', error)
        this.$message.error('选择音乐目录失败: ' + error.message)
      }
    },
    randomPlayBgMusic() {
      // 触发事件，让父组件处理背景音乐播放
      this.$emit('play-bg-music', {
        musicDir: this.musicDir,
        bgVolume: this.bgVolume,
        autoAvoid: this.autoAvoid,
        bgMusic: this.bgMusic,
        fadeTime: this.fadeTime,
        musicInterval: this.musicInterval
      })
    },
    stopBgMusic() {
      // 触发事件，让父组件处理停止背景音乐
      this.$emit('stop-bg-music')
    },
    saveSettings() {
      // 构建配置对象
      const config = {
        // 主播调整
        speed: this.speed,
        speedRange: this.speedRange,
        tone: this.tone,
        toneRange: this.toneRange,
        volume: this.volume,
        volumeRange: this.volumeRange,
        resumeScript: this.resumeScript,

        // 互动设置
        autoInteraction: this.autoInteraction,
        chatInteractionProb: this.chatInteractionProb,
        enterInteractionProb: this.enterInteractionProb,
        actionInteractionProb: this.actionInteractionProb,

        // 背景音乐
        autoAvoid: this.autoAvoid,
        bgMusic: this.bgMusic,
        fadeTime: this.fadeTime,
        musicDir: this.musicDir,
        bgVolume: this.bgVolume,
        musicInterval: this.musicInterval,

        // 其他设置
        endSound: this.endSound,
        randomPause: this.randomPause,
        pauseMax: this.pauseMax,
        scriptRounds: this.scriptRounds
      }

      // 保存到 localStorage
      localStorage.setItem('anchorControlConfig', JSON.stringify(config))

      // 触发事件通知父组件配置已更新
      this.$emit('config-updated', config)

      // 更新初始数据快照
      this.initialData = this.getCurrentData()

      this.dialogVisible = false
      this.$message.success("设置已保存")
    },
  }
}
</script>

<style>
/* 全局样式：修改 Dialog 的 body 和 footer */
.anchor-control-dialog .el-dialog__body {
  padding: 20px 20px 0 20px; /* 底部padding设为0 */
  max-height: 60vh;
  overflow-y: auto;
}

.anchor-control-dialog .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed; /* 添加上边框 */
  background-color: #fff;
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>

<style scoped>
/* 内容滚动区域 */
.dialog-content-scroll {
  max-height: 60vh; /* 最大高度60%视口 */
  overflow-y: auto;
  padding-right: 10px; /* 为滚动条留空间 */
}

/* 自定义滚动条样式 */
.dialog-content-scroll::-webkit-scrollbar {
  width: 6px;
}

.dialog-content-scroll::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.dialog-content-scroll::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.dialog-content-scroll::-webkit-scrollbar-track {
  background-color: #f5f7fa;
  border-radius: 3px;
}

.red {
  color: red;
}

.tip-text {
  font-size: 12px;
}

.bg-music {
  display: flex;
  flex-direction: colum;
}
</style>