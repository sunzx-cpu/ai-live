<template>
  <el-dialog
    title="插入定时话术"
    :visible.sync="dialogVisible"
    width="900px"
    top="5vh"
    :close-on-click-modal="false"
    custom-class="scheduled-speech-dialog"
    :before-close="handleClose">

    <!-- 提示说明 -->
    <div class="tip-warning">
      <span style="font-weight: 500;">定时话术：</span>使用当前主播声音，插入到下一条播放。<span style="color: #f56c6c;">为提高语音生成速度，转换每机词的一行文字建议不要超过120字。</span>
    </div>

    <!-- 文本输入区 -->
    <div class="input-section">
      <el-input
        type="textarea"
        v-model="speechText"
        :rows="8"
        placeholder="请输入定时话术内容，支持换行和随机语法。例如：欢迎来到直播间！或换行输入多条话术实现随机播放。">
      </el-input>
    </div>

    <!-- 提示说明 -->
    <div class="tip-desc">
      定时话术会在设定的时间间隔内随机插入播放。支持换行输入多条话术，启用"换行随机"后，每行将作为一个独立的话术随机选择播放。支持变量替换（如当前日期、当前时间、星期）和随机语法（如"欢迎|恭喜"将随机选择其中一个词）。
    </div>

    <!-- 换行随机开关 -->
    <div class="switch-section">
      <div class="switch-item">
        <span class="switch-label">换行随机</span>
        <el-switch v-model="lineBreakRandom"></el-switch>
        <span class="switch-desc">启用后，将每行文本视为一个独立的话术，每次随机选择其中一行播放。关闭时，整段文本作为一个话术播放。</span>
      </div>
    </div>

    <!-- 定时间隔设置 -->
    <div class="interval-section">
      <span class="label">定时话术随机间隔</span>
      <el-input-number
        v-model="minInterval"
        :min="1"
        :max="maxInterval - 1"
        controls-position="right"
        size="small"
        style="width: 120px; margin: 0 8px;">
      </el-input-number>
      <span>-</span>
      <el-input-number
        v-model="maxInterval"
        :min="minInterval + 1"
        :max="9999"
        controls-position="right"
        size="small"
        style="width: 120px; margin: 0 8px;">
      </el-input-number>
      <span>秒之间随机插入</span>
    </div>

    <!-- 定时话术开关 -->
    <div class="switch-section" style="margin-top: 20px;">
      <div class="switch-item">
        <span class="switch-label">定时话术开关</span>
        <el-switch v-model="enabled" :disabled="!speechText.trim()"></el-switch>
        <span class="switch-desc" v-if="enabled && speechText.trim()">
          定时话术已启用，将在 {{ minInterval }}-{{ maxInterval }} 秒间隔随机插入播放
        </span>
        <span class="switch-desc" v-else-if="!speechText.trim()" style="color: #f56c6c;">
          请先输入定时话术内容
        </span>
      </div>
    </div>

    <!-- 底部按钮 -->
    <span slot="footer" class="dialog-footer">
      <el-button @click="callback()">关闭</el-button>
      <el-button type="primary" @click="saveConfig">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'ScheduledSpeechDialog',
  data() {
    return {
      dialogVisible: false,
      initialData: null,
      speechText: '',
      lineBreakRandom: false,  // 换行随机开关
      minInterval: 20,         // 最小间隔（秒）
      maxInterval: 60,         // 最大间隔（秒）
      enabled: false,          // 定时话术开关
      timer: null              // 定时器
    }
  },
  computed: {
    hasChanges() {
      if (!this.initialData) return false
      return JSON.stringify(this.getCurrentData()) !== JSON.stringify(this.initialData)
    }
  },
  watch: {
    // 监听开关变化
    enabled(newVal) {
      if (newVal) {
        this.startScheduledSpeech()
      } else {
        this.stopScheduledSpeech()
      }
    },
    // 监听对话框关闭
    dialogVisible(newVal) {
      if (newVal) {
        // 打开对话框时加载配置
        this.loadConfig()
      }
    }
  },
  beforeDestroy() {
    // 组件销毁前清理定时器
    this.stopScheduledSpeech()
  },
  methods: {
    // 获取当前表单数据
    getCurrentData() {
      return {
        speechText: this.speechText,
        lineBreakRandom: this.lineBreakRandom,
        minInterval: this.minInterval,
        maxInterval: this.maxInterval,
        enabled: this.enabled
      }
    },

    // 处理对话框关闭（X按钮或ESC）
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

    // 处理关闭按钮点击
    callback() {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要关闭吗？', '提示', {
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

    // 保存配置
    saveConfig() {
      if (!this.speechText.trim()) {
        this.$message.warning('请输入定时话术内容')
        return
      }

      // 保存到 localStorage
      const config = {
        speechText: this.speechText,
        lineBreakRandom: this.lineBreakRandom,
        minInterval: this.minInterval,
        maxInterval: this.maxInterval,
        enabled: this.enabled
      }

      try {
        localStorage.setItem('scheduledSpeech_config', JSON.stringify(config))
        this.$message.success('定时话术配置已保存')

        // 更新初始数据快照
        this.initialData = this.getCurrentData()

        // 触发配置更新事件
        this.$emit('config-updated', config)

        // 如果启用了，开始定时任务
        if (this.enabled) {
          this.startScheduledSpeech()
        }

        // 关闭对话框
        this.dialogVisible = false
      } catch (error) {
        console.error('保存定时话术配置失败:', error)
        this.$message.error('保存配置失败')
      }
    },

    // 加载配置
    loadConfig() {
      try {
        const saved = localStorage.getItem('scheduledSpeech_config')
        if (saved) {
          const config = JSON.parse(saved)
          this.speechText = config.speechText || ''
          this.lineBreakRandom = config.lineBreakRandom || false
          this.minInterval = config.minInterval || 20
          this.maxInterval = config.maxInterval || 60
          this.enabled = config.enabled || false
        }
      } catch (error) {
        console.error('加载定时话术配置失败:', error)
      }

      // 保存初始数据快照
      this.$nextTick(() => {
        this.initialData = this.getCurrentData()
      })
    },

    // 开始定时话术
    startScheduledSpeech() {
      // 先清除之前的定时器
      this.stopScheduledSpeech()

      // 定义调度函数
      const schedule = () => {
        // 计算随机间隔
        const interval = Math.floor(
          Math.random() * (this.maxInterval - this.minInterval + 1)
        ) + this.minInterval

        // 设置定时器
        this.timer = setTimeout(() => {
          if (this.enabled && this.speechText.trim()) {
            // 获取随机话术
            const text = this.getRandomText()

            // 触发插入事件
            this.$emit('insert', {
              text: text,
              type: 'scheduled'
            })

            // 继续调度下一次
            schedule()
          }
        }, interval * 1000)
      }

      // 开始第一次调度
      schedule()
    },

    // 停止定时话术
    stopScheduledSpeech() {
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
    },

    // 获取随机话术文本
    getRandomText() {
      if (this.lineBreakRandom) {
        // 换行随机：从每行中随机选一行
        const lines = this.speechText.split('\n').filter(line => line.trim())
        if (lines.length === 0) return ''

        const randomLine = lines[Math.floor(Math.random() * lines.length)]
        return this.processText(randomLine)
      } else {
        // 不换行随机：处理整段文本的随机语法
        return this.processText(this.speechText)
      }
    },

    // 处理文本（变量替换 + 随机语法）
    processText(text) {
      // 1. 替换变量
      let processed = this.replaceVariables(text)

      // 2. 处理随机语法 {A|B|C}
      processed = this.processRandomSyntax(processed)

      // 注意：同音词替换已在 LivePaneSegmentTable.generateTTSAudio() 中统一处理

      return processed
    },

    // 替换变量
    replaceVariables(text) {
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth() + 1
      const date = now.getDate()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekday = days[now.getDay()]

      return text
        .replace(/\{当前日期\}/g, `${year}年${month}月${date}日`)
        .replace(/\{当前时间\}/g, `${hours}:${minutes}`)
        .replace(/\{星期\}/g, weekday)
    },

    // 处理随机语法 {A|B|C}
    processRandomSyntax(text) {
      return text.replace(/\{([^}]+)\}/g, (match, options) => {
        // 如果是已知变量（日期、时间等），不处理
        if (match.includes('当前') || match.includes('星期')) {
          return match
        }
        // 处理随机选项
        const choices = options.split('|')
        return choices[Math.floor(Math.random() * choices.length)]
      })
    }
  }
}
</script>

<style>
/* 全局样式：修改 Dialog 的 body 和 footer */
.scheduled-speech-dialog .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.scheduled-speech-dialog .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>

<style scoped lang="scss">
// 红色警告提示
.tip-warning {
  padding: 12px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

// 灰色说明提示
.tip-desc {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 12px;
}

// 输入区域
.input-section {
  margin-bottom: 12px;
}

// 开关区域
.switch-section {
  margin-top: 16px;

  .switch-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .switch-label {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      min-width: 100px;
    }

    .switch-desc {
      font-size: 13px;
      color: #909399;
      line-height: 1.5;
    }
  }
}

// 间隔设置区域
.interval-section {
  margin-top: 16px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;

  .label {
    font-weight: 500;
    color: #303133;
    margin-right: 12px;
  }
}

// 底部按钮区
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
