<template>
  <el-dialog
    title="插入临时话术"
    :visible.sync="dialogVisible"
    width="800px"
    top="5vh"
    :close-on-click-modal="false"
    @close="handleClose">

    <!-- 提示说明 -->
    <div class="tip-warning">
      <span style="font-weight: 500;">临时话术：</span>使用当前主播声音，插入到下一条播放。<span style="color: #f56c6c;">为提高语音生成速度，转换每机词的一行文字建议不要超过120字。</span>
    </div>

    <!-- 文本输入区 -->
    <div class="input-section">
      <el-input
        type="textarea"
        v-model="speechText"
        :rows="4"
        :maxlength="300"
        placeholder="请输入临时话术内容"
        show-word-limit>
      </el-input>
    </div>

    <!-- 快捷短语标签区 -->
    <div class="quick-phrases-section">
      <div class="section-label">点击快捷提示语插入，你也可以新增快捷提示语保存。</div>
      <div class="phrases-container">
        <el-tag
          v-for="(phrase, index) in quickPhrases"
          :key="index"
          closable
          type="info"
          @click="insertPhrase(phrase)"
          @close="removePhrase(index)"
          style="margin-right: 8px; margin-bottom: 8px; cursor: pointer;">
          {{ phrase }}
        </el-tag>
        <el-button
          type="text"
          icon="el-icon-plus"
          size="small"
          @click="showAddPhraseDialog = true"
          style="color: #409eff;">
          新建快捷提示语
        </el-button>
      </div>
    </div>

    <!-- 蓝色提示信息 -->
    <!-- AI麦克风功能暂时禁用，相关提示暂时隐藏
    <div class="tip-info">
      <div>通过AI技术，使用麦克风实时采集你对着麦克风说话的声音，转换成文字输入，请保使用麦克风输入，点击即可用麦克风等待5秒，开始时说话会首先自前言后音后进行播放。</div>
      <div style="margin-top: 8px;">如使用麦克风采集输入，请注意当前使用的麦克风是否正确！！！</div>
      <div style="margin-top: 8px;">为确保录入正确，请做认识内容时先完整话！！！</div>
    </div>
    -->

    <!-- 底部按钮 -->
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭窗口</el-button>
      <!-- AI麦克风功能暂时禁用
      <el-button
        :type="aiMicEnabled ? 'success' : 'info'"
        @click="toggleAiMic">
        {{ aiMicEnabled ? '关闭AI麦克风' : '启用AI麦克风' }}
      </el-button>
      -->
      <el-button
        type="primary"
        @click="insertAndPlay"
        :disabled="!speechText.trim()">
        插入到下一条并播放
      </el-button>
    </span>

    <!-- 新建快捷短语对话框 -->
    <el-dialog
      title="新建快捷提示语"
      :visible.sync="showAddPhraseDialog"
      width="500px"
      append-to-body>
      <el-input
        v-model="newPhrase"
        placeholder="请输入快捷提示语内容"
        :maxlength="100"
        show-word-limit>
      </el-input>
      <span slot="footer">
        <el-button @click="showAddPhraseDialog = false">取消</el-button>
        <el-button type="primary" @click="addPhrase">确定</el-button>
      </span>
    </el-dialog>
  </el-dialog>
</template>

<script>
export default {
  name: 'TemporarySpeechDialog',
  data() {
    return {
      dialogVisible: false,
      speechText: '',
      // aiMicEnabled: false,  // AI麦克风功能暂时禁用
      showAddPhraseDialog: false,
      newPhrase: '',
      quickPhrases: []  // 快捷短语列表
      // recognition: null  // 语音识别实例（暂时禁用）
    }
  },
  mounted() {
    // 从 localStorage 加载快捷短语
    this.loadQuickPhrases()
    // AI麦克风功能暂时禁用
    // this.initSpeechRecognition()
  },
  beforeDestroy() {
    // AI麦克风功能暂时禁用
    // if (this.recognition && this.aiMicEnabled) {
    //   this.stopRecording()
    // }
  },
  methods: {
    // AI麦克风相关方法暂时禁用
    /*
    // 初始化语音识别
    initSpeechRecognition() {
      ... 省略代码 ...
    },
    */

    // 关闭对话框
    handleClose() {
      // AI麦克风功能暂时禁用
      // if (this.aiMicEnabled) {
      //   this.stopRecording()
      //   this.aiMicEnabled = false
      // }
      this.dialogVisible = false
      this.speechText = ''
    },

    // AI麦克风相关方法暂时禁用
    /*
    // 切换AI麦克风状态
    toggleAiMic() { ... },
    // 开始录音
    startRecording() { ... },
    // 停止录音
    stopRecording() { ... },
    */

    // 插入快捷短语
    insertPhrase(phrase) {
      // 替换变量
      const processedPhrase = this.replaceVariables(phrase)
      this.speechText = processedPhrase
    },

    // 删除快捷短语
    removePhrase(index) {
      this.quickPhrases.splice(index, 1)
      this.saveQuickPhrases()
      this.$message.success('已删除快捷提示语')
    },

    // 添加快捷短语
    addPhrase() {
      if (!this.newPhrase.trim()) {
        this.$message.warning('请输入快捷提示语内容')
        return
      }

      this.quickPhrases.push(this.newPhrase.trim())
      this.saveQuickPhrases()
      this.$message.success('已添加快捷提示语')

      this.newPhrase = ''
      this.showAddPhraseDialog = false
    },

    // 替换变量（支持 {当前日期}、{当前时间}、{星期} 等）
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

    // 插入到下一条并播放
    insertAndPlay() {
      if (!this.speechText.trim()) {
        this.$message.warning('请输入话术内容')
        return
      }

      // 1. 替换变量
      let processedText = this.replaceVariables(this.speechText)

      // 2. 处理随机语法 {A|B|C}
      processedText = this.processRandomSyntax(processedText)

      // 注意：同音词替换已在 LivePaneSegmentTable.generateTTSAudio() 中统一处理

      // 触发插入事件
      this.$emit('insert', {
        text: processedText,
        type: 'temporary'
      })

      // 关闭对话框（成功提示由插入方法显示）
      this.handleClose()
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
    },

    // 加载快捷短语
    loadQuickPhrases() {
      try {
        const saved = localStorage.getItem('temporarySpeech_quickPhrases')
        if (saved) {
          this.quickPhrases = JSON.parse(saved)
        } else {
          // 默认快捷短语
          this.quickPhrases = [
            '欢迎{新朋友|宝宝们}来到直播间，点点关注不迷路！',
            '欢迎宝宝来到直播间，音乐王播给大家关注啦！',
            '点个小爱，关个好物，点关注不迷路',
            '现在是{当前日期}{星期}，北京时间{当前时间}'
          ]
          this.saveQuickPhrases()
        }
      } catch (error) {
        console.error('加载快捷短语失败:', error)
        this.quickPhrases = []
      }
    },

    // 保存快捷短语
    saveQuickPhrases() {
      try {
        localStorage.setItem('temporarySpeech_quickPhrases', JSON.stringify(this.quickPhrases))
      } catch (error) {
        console.error('保存快捷短语失败:', error)
      }
    }
  }
}
</script>

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

// 蓝色信息提示
.tip-info {
  padding: 12px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  font-size: 13px;
  line-height: 1.6;
  margin-top: 16px;
}

// 输入区域
.input-section {
  margin-bottom: 16px;
}

// 快捷短语区域
.quick-phrases-section {
  margin-top: 16px;

  .section-label {
    font-size: 13px;
    color: #606266;
    margin-bottom: 12px;
  }

  .phrases-container {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }
}

// 底部按钮区
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
