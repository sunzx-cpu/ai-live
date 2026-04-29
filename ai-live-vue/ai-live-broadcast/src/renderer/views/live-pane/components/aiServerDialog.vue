<!-- AI服务器设置对话框 -->
<template>
  <el-dialog title="AI服务器设置" :visible.sync="visible" width="900px" custom-class="ai-server-dialog" :before-close="handleClose">
    <div class="dialog-content">
      <!-- 第一部分：本机安装AI语音 -->
      <div class="setting-row">
        <div class="setting-label">本机安装AI语音</div>
        <div class="setting-content">
          <el-button type="success" size="small" @click="openPluginDirectory">进入插件安装目录</el-button>
          <el-link type="primary" :underline="false" style="margin-left: 12px">【如何安装本地AI语音服务插件】</el-link>
        </div>
      </div>

      <!-- 第二部分：请选择服务器 -->
      <div class="setting-row">
        <div class="setting-label">请选择服务器</div>
        <div class="setting-content">
          <el-radio-group v-model="selectedServer">
            <el-radio label="cloud" disabled>云端AI服务器</el-radio>
            <el-radio label="local">本地语音服务</el-radio>
            <el-radio label="f5tts" disabled>F5TTS语音服务</el-radio>
            <el-radio label="cosyvoice" disabled>COSYVOICE语音服务</el-radio>
          </el-radio-group>

          <!-- 云端AI服务器说明 -->
          <div v-if="selectedServer === 'cloud'" class="server-tips">
            <p style="font-weight: 500;">请选择AI服务器</p>
            <p style="margin-top: 12px;">云端AI服务器：云端高性能GPU服务器，提供专业级主播，无需本地电脑硬件配置。</p>
            <p>目键AI服务器和内置语音服务器：如果你的电脑配置不符（建议显存8G以上），可以使用自建AI服务器，自建AI服务器则可以使用你自己的AI语音。</p>
          </div>

          <!-- F5TTS语音服务说明 -->
          <div v-if="selectedServer === 'f5tts'" class="warning-tips-box">
            <p class="warning-text">只需要5秒的音频，即可高质量的克隆声音，还原度高，感情丰富。</p>
            <p class="warning-text" style="margin-top: 8px;">
              主播模型：通过下方【上传模型】按钮并打开模型文件夹，放入以音频中的话术文本为文件名的wav文件，文件名中根据语气在合适的位置加入标点符号！</p>
            <p class="warning-text" style="margin-top: 8px;">
              注意：由于生成的音频质量较高，所以生成的速度会慢一些，建议通过加大下方[开播时预加载音频]数量,提前预加载出足够的音频再进行开播。</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
          </div>

          <!-- COSYVOICE语音服务说明 -->
          <div v-if="selectedServer === 'cosyvoice'" class="warning-tips-box">
            <p class="warning-text">只需要5秒钟的音频，即可高质量的克隆声音，还原度高，感情丰富，并且情感可调。</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
            <p class="warning-text" style="margin-top: 8px;">使用前，请确保已经正确安装语音服务器插件！！！</p>
          </div>
        </div>
      </div>

      <!-- 本地语音服务 - 第三部分：模型训练 -->
      <div v-if="selectedServer === 'local'" class="setting-row">
        <div class="setting-label">模型训练</div>
        <div class="setting-content">
          <el-link type="primary" :underline="false">【选查看模训练教程】</el-link>
        </div>
      </div>

      <!-- 本地语音服务 - 第四部分：语音模型 -->
      <div v-if="selectedServer === 'local'" class="setting-row">
        <div class="setting-label">语音模型</div>
        <div class="setting-content">
          <div style="display: flex; align-items: flex-start; gap: 12px;">
            <el-button type="primary" size="small" icon="el-icon-upload" @click="openModelsFolder">上传模型</el-button>
            <div class="model-tips" style="flex: 1; margin-top: 0;">
              在这打开的模型文件夹中，放入训练好的模型，支持Bert-VITS2和GPT-SOVITS2。
              <p class="warning-text">注意：模型文件夹中请存放1个类型的模型语音模型，并选择下方匹配的模型语音模型类型！</p>
            </div>
          </div>

          <el-radio-group v-model="modelType" style="margin-top: 12px;">
            <el-radio label="bert-vits2">BERT-VITS2</el-radio>
            <el-radio label="gpt-sovits">GPT-SOVITS</el-radio>
          </el-radio-group>
        </div>
      </div>


      <!-- F5TTS语音服务 - 声音克隆 -->
      <div v-if="selectedServer === 'f5tts'" class="setting-row">
        <div class="setting-label">声音克隆</div>
        <div class="setting-content">
          <el-button type="primary" size="small" icon="el-icon-folder" @click="openF5ttsModelsFolder">上传模型</el-button>
          <div class="clone-tips">
            在打开的模型文件夹中，只需要放入5秒左右有感情的wav文件。文件名称使用音频内话术文字。
          </div>
          <div class="clone-tips-note">
            下方两个服务地址，可以实现负载均衡，如只需一个，可删除【服务地址2】。
          </div>
        </div>
      </div>

      <!-- COSYVOICE语音服务 - 声音克隆 -->
      <div v-if="selectedServer === 'cosyvoice'" class="setting-row">
        <div class="setting-label">声音克隆</div>
        <div class="setting-content">
          <el-button type="primary" size="small">训练</el-button>
        </div>
      </div>

      <!-- F5TTS和COSYVOICE语音服务 - 服务器地址 -->
      <div v-if="selectedServer === 'f5tts'" class="setting-row">
        <div class="setting-label">服务地址1</div>
        <div class="setting-content">
          <el-input v-model="f5ttsServerUrl" placeholder="http://127.0.0.1:5010/api?" style="width: 400px"></el-input>
        </div>
      </div>

      <div v-if="selectedServer === 'cosyvoice'" class="setting-row">
        <div class="setting-label">服务地址1</div>
        <div class="setting-content">
          <el-input v-model="cosyVoiceServerUrl" placeholder="cosyVoiceTTS api地址，如果此处为空，可以通过右上角头像窗口【恢复出厂】"
            style="width: 500px"></el-input>
        </div>
      </div>

      <!-- 本地语音服务 - 第五部分：AI服务器地址 -->
      <div v-if="selectedServer === 'local'" class="setting-row">
        <div class="setting-label">AI服务器地址</div>
        <div class="setting-content">
          <el-input v-model="serverUrl" placeholder="http://127.0.0.1:23456/" style="width: 400px"></el-input>
        </div>
      </div>

      <!-- 本地语音服务 - VSA服务诊断 -->
      <div v-if="selectedServer === 'local'" class="setting-row">
        <div class="setting-label">服务诊断</div>
        <div class="setting-content">
          <div style="display: flex; align-items: flex-start; gap: 12px;">
            <el-button
              type="warning"
              size="small"
              icon="el-icon-connection"
              @click="runDiagnostic"
              :loading="diagnosticRunning">
              {{ diagnosticRunning ? '诊断中...' : '诊断VSA服务' }}
            </el-button>
            <el-button
              v-if="diagnosticReport"
              type="info"
              size="small"
              icon="el-icon-folder"
              @click="openDiagnosticReport">
              查看报告
            </el-button>
            <div class="diagnostic-tips" style="flex: 1; margin-top: 0;">
              如果遇到"VSA服务启动超时"问题，点击此按钮诊断服务状态。
            </div>
          </div>

          <!-- 诊断结果显示 -->
          <div v-if="diagnosticReport" class="diagnostic-result" style="margin-top: 12px;">
            <div class="result-summary" :class="diagnosticReport.summary.error > 0 ? 'error-summary' : 'success-summary'">
              <div style="font-weight: 600; margin-bottom: 8px;">诊断摘要</div>
              <div style="display: flex; gap: 20px;">
                <span>✓ 成功: {{ diagnosticReport.summary.success }}</span>
                <span>⚠ 警告: {{ diagnosticReport.summary.warning }}</span>
                <span>✗ 错误: {{ diagnosticReport.summary.error }}</span>
              </div>
            </div>

            <div v-if="diagnosticReport.summary.error > 0" class="error-details">
              <div style="font-weight: 600; margin-bottom: 8px; color: #f56c6c;">发现的问题:</div>
              <div v-for="(result, index) in diagnosticReport.results.filter(r => r.status === 'error')"
                   :key="index"
                   style="margin-bottom: 6px; padding-left: 12px;">
                <span style="color: #f56c6c;">✗</span> [{{ result.category }}] {{ result.message }}
              </div>

              <!-- 解决建议 -->
              <div class="solution-tips" style="margin-top: 12px;">
                <div style="font-weight: 600; margin-bottom: 8px;">解决建议:</div>
                <ul style="margin: 0; padding-left: 20px;">
                  <li v-if="hasFileError">VSA文件不完整，请将 vsa.rar 完整解压到 extra/vsa/ 目录</li>
                  <li v-if="hasPortError">端口23456被占用，请关闭占用该端口的程序</li>
                  <li v-if="hasTimeoutError">服务启动超时，建议关闭其他占用显存的程序，或将超时时间增加到60秒</li>
                </ul>
              </div>
            </div>

            <div v-else-if="diagnosticReport.summary.warning > 0" class="warning-details">
              <div style="font-weight: 600; margin-bottom: 8px; color: #e6a23c;">需要注意:</div>
              <div v-for="(result, index) in diagnosticReport.results.filter(r => r.status === 'warning')"
                   :key="index"
                   style="margin-bottom: 6px; padding-left: 12px;">
                <span style="color: #e6a23c;">⚠</span> [{{ result.category }}] {{ result.message }}
              </div>
            </div>

            <div v-else class="success-details">
              <div style="color: #67c23a; font-weight: 600;">
                ✓ VSA服务状态正常，未发现问题
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 第六部分：开播时预加载音频 -->
      <div class="setting-row">
        <div class="setting-content">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="color: #f56c6c; font-weight: 500;">开播时预加载音频</span>
            <el-input-number v-model="preloadCount" :min="1" :max="100" size="small"></el-input-number>
            <span style="color: #f56c6c; font-weight: 500;">句</span>
            <span
              style="padding: 4px 8px; background-color: #ecf5ff; border: 1px solid #d9ecff; border-radius: 3px; color: #409eff; font-size: 13px;">预加载完设定条数后开始播放，并继续后台加载后续音频.（使用本地AI音频服务的用户，显存小的可把该值调大）。</span>
          </div>
          <div style="margin-top: 12px;">
            <span style="color: #f56c6c; font-weight: 500;">调试模式</span>
            <el-checkbox v-model="enableSuggestion">启用</el-checkbox>
            <span
              style="margin-left: 12px; padding: 4px 8px; background-color: #ecf5ff; border: 1px solid #d9ecff; border-radius: 3px; color: #409eff; font-size: 13px;">调试模式,启用后，在启动语音服务时,
              服务窗口可视化(默认最小化),保存设置后重启程序生效</span>
          </div>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="callback()">关闭</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'AiServerDialog',
  props: {
    dialogVisible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      initialData: null,
      selectedServer: 'local',        // 选择的服务器类型（默认选择本地语音服务）
      modelType: 'bert-vits2',        // 语音模型类型
      serverUrl: 'http://127.0.0.1:23456/',  // 本地AI服务器地址
      f5ttsServerUrl: 'http://127.0.0.1:5010/api?',  // F5TTS服务器地址
      cosyVoiceServerUrl: '',         // COSYVOICE服务器地址
      anchorModel: 'default',         // F5TTS主播模型
      preloadCount: 5,                // 预加载音频数量
      enableSuggestion: true,         // 是否启用建议
      diagnosticRunning: false,       // 是否正在运行诊断
      diagnosticReport: null          // 诊断报告
    }
  },
  computed: {
    visible: {
      get() {
        return this.dialogVisible
      },
      set(val) {
        if (!val) {
          this.$emit('close')
        }
      }
    },
    hasChanges() {
      if (!this.initialData) return false
      return JSON.stringify(this.getCurrentData()) !== JSON.stringify(this.initialData)
    },
    // 判断诊断报告中的错误类型
    hasFileError() {
      if (!this.diagnosticReport) return false
      return this.diagnosticReport.results.some(r =>
        r.status === 'error' && r.category === '文件检查'
      )
    },
    hasPortError() {
      if (!this.diagnosticReport) return false
      return this.diagnosticReport.results.some(r =>
        r.status === 'warning' && r.category === '端口检查' && r.message.includes('已被占用')
      )
    },
    hasTimeoutError() {
      if (!this.diagnosticReport) return false
      return this.diagnosticReport.results.some(r =>
        (r.status === 'error' || r.status === 'warning') &&
        r.category === '服务启动' &&
        (r.message.includes('超时') || r.message.includes('未响应'))
      )
    }
  },
  mounted() {
    // 加载已保存的配置
    this.loadConfig()
  },
  methods: {
    // 获取当前表单数据
    getCurrentData() {
      return {
        selectedServer: this.selectedServer,
        modelType: this.modelType,
        serverUrl: this.serverUrl,
        f5ttsServerUrl: this.f5ttsServerUrl,
        cosyVoiceServerUrl: this.cosyVoiceServerUrl,
        anchorModel: this.anchorModel,
        preloadCount: this.preloadCount,
        enableSuggestion: this.enableSuggestion
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
          this.$emit('close')
        }).catch(() => {})
      } else {
        done()
        this.$emit('close')
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
          this.$emit('close')
        }).catch(() => {})
      } else {
        this.$emit('close')
      }
    },
    // 加载已保存的配置
    loadConfig() {
      const savedConfig = localStorage.getItem('aiServerConfig')
      if (savedConfig) {
        try {
          const config = JSON.parse(savedConfig)
          this.selectedServer = config.selectedServer || 'local'
          this.modelType = config.modelType || 'bert-vits2'
          this.serverUrl = config.serverUrl || 'http://127.0.0.1:23456/'
          this.f5ttsServerUrl = config.f5ttsServerUrl || 'http://127.0.0.1:5010/api?'
          this.cosyVoiceServerUrl = config.cosyVoiceServerUrl || ''
          this.anchorModel = config.anchorModel || 'default'
          this.preloadCount = config.preloadCount || 5
          this.enableSuggestion = config.enableSuggestion !== undefined ? config.enableSuggestion : true
        } catch (e) {
          console.error('解析配置失败:', e)
        }
      }

      // 保存初始数据快照
      this.$nextTick(() => {
        this.initialData = this.getCurrentData()
      })
    },
    handleSave() {
      // 保存设置
      const config = {
        selectedServer: this.selectedServer,
        modelType: this.modelType,
        serverUrl: this.serverUrl,
        f5ttsServerUrl: this.f5ttsServerUrl,
        cosyVoiceServerUrl: this.cosyVoiceServerUrl,
        anchorModel: this.anchorModel,
        preloadCount: this.preloadCount,
        enableSuggestion: this.enableSuggestion
      }

      // 保存到 localStorage
      localStorage.setItem('aiServerConfig', JSON.stringify(config))

      // 更新初始数据快照
      this.initialData = this.getCurrentData()

      this.$emit('save', config)
      this.$message.success('AI服务器设置已保存')
      this.$emit('close')
    },
    // 打开模型文件夹（本地语音服务）
    async openModelsFolder() {
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('open-models-folder', { folderType: 'vsa' })

        if (result.success) {
          this.$message.success('已打开模型文件夹')
        } else {
          this.$message.error('打开文件夹失败: ' + result.error)
          console.error('打开失败:', result)
        }
      } catch (error) {
        this.$message.error('打开文件夹时出错: ' + error.message)
        console.error('错误:', error)
      }
    },
    // 打开F5TTS模型文件夹
    async openF5ttsModelsFolder() {
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('open-models-folder', { folderType: 'f5tts' })

        if (result.success) {
          this.$message.success('已打开F5TTS模型文件夹')
        } else {
          this.$message.error('打开文件夹失败: ' + result.error)
          console.error('打开失败:', result)
        }
      } catch (error) {
        this.$message.error('打开文件夹时出错: ' + error.message)
        console.error('错误:', error)
      }
    },
    // 打开插件安装目录（extra文件夹）
    async openPluginDirectory() {
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('open-models-folder', { folderType: 'extra' })

        if (result.success) {
          this.$message.success('已打开插件安装目录')
        } else {
          this.$message.error('打开文件夹失败: ' + result.error)
          console.error('打开失败:', result)
        }
      } catch (error) {
        this.$message.error('打开文件夹时出错: ' + error.message)
        console.error('错误:', error)
      }
    },
    // 运行VSA服务诊断
    async runDiagnostic() {
      try {
        this.diagnosticRunning = true
        this.diagnosticReport = null

        this.$message.info('正在诊断VSA服务，请稍候...')

        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('diagnose-vsa-service')

        if (result.success) {
          this.diagnosticReport = result.report
          console.log('[诊断报告]', this.diagnosticReport)

          // 根据诊断结果显示不同的消息
          if (this.diagnosticReport.summary.error > 0) {
            this.$message.error('诊断完成，发现 ' + this.diagnosticReport.summary.error + ' 个错误')
          } else if (this.diagnosticReport.summary.warning > 0) {
            this.$message.warning('诊断完成，有 ' + this.diagnosticReport.summary.warning + ' 个警告')
          } else {
            this.$message.success('诊断完成，VSA服务状态正常')
          }
        } else {
          this.$message.error('诊断失败: ' + result.error)
          console.error('诊断失败:', result)
        }
      } catch (error) {
        this.$message.error('诊断时出错: ' + error.message)
        console.error('诊断错误:', error)
      } finally {
        this.diagnosticRunning = false
      }
    },
    // 打开诊断报告文件
    async openDiagnosticReport() {
      try {
        const { ipcRenderer } = require('electron')
        const result = await ipcRenderer.invoke('open-diagnostic-report')

        if (result.success) {
          this.$message.success('已打开诊断报告文件夹')
        } else {
          this.$message.error('打开失败: ' + result.error)
        }
      } catch (error) {
        this.$message.error('打开时出错: ' + error.message)
        console.error('错误:', error)
      }
    }
  }
}
</script>

<style>
/* 全局样式：修改 Dialog 的 body 和 footer */
.ai-server-dialog .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.ai-server-dialog .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>

<style scoped lang="scss">
.dialog-content {
  .setting-row {
    display: flex;
    margin-bottom: 20px;
    align-items: flex-start;
    gap: 2%;

    .setting-label {
      width: 15%;
      min-width: 100px;
      max-width: 140px;
      flex-shrink: 0;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      padding-top: 4px;
      text-align: right;
    }

    .setting-content {
      flex: 1;
      min-width: 0;

      .server-tips {
        margin-top: 12px;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;

        p {
          margin: 6px 0;
          font-size: 13px;
          color: #606266;
          line-height: 1.6;

          &.warning-text {
            color: #f56c6c;
            font-weight: 500;
          }
        }
      }

      .warning-tips-box {
        margin-top: 12px;
        padding: 12px;
        background-color: #fef0f0;
        border: 1px solid #fde2e2;
        border-radius: 4px;

        .warning-text {
          color: #f56c6c;
          font-size: 13px;
          line-height: 1.6;
          margin: 0;
        }
      }

      .clone-tips {
        margin-top: 12px;
        padding: 8px 12px;
        background-color: #ecf5ff;
        border: 1px solid #d9ecff;
        border-radius: 4px;
        color: #409eff;
        font-size: 13px;
        line-height: 1.6;
      }

      .clone-tips-note {
        margin-top: 8px;
        padding: 8px 12px;
        background-color: #ecf5ff;
        border: 1px solid #d9ecff;
        border-radius: 4px;
        color: #409eff;
        font-size: 13px;
        line-height: 1.6;
      }

      .model-tips {
        margin: 12px 0;
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;
        font-size: 13px;
        color: #606266;
        line-height: 1.6;

        .warning-text {
          color: #f56c6c;
          font-weight: 500;
          margin-top: 8px;
        }
      }

      .preload-tips {
        margin: 12px 0;

        .info-text {
          font-size: 13px;
          color: #409eff;
        }
      }

      // 诊断相关样式
      .diagnostic-tips {
        padding: 8px 12px;
        background-color: #fff7e6;
        border: 1px solid #ffd591;
        border-radius: 4px;
        color: #d48806;
        font-size: 13px;
        line-height: 1.6;
      }

      .diagnostic-result {
        padding: 12px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .result-summary {
          padding: 12px;
          border-radius: 4px;
          margin-bottom: 12px;

          &.success-summary {
            background-color: #f0f9ff;
            border: 1px solid #b7eb8f;
            color: #52c41a;
          }

          &.error-summary {
            background-color: #fff1f0;
            border: 1px solid #ffccc7;
            color: #f5222d;
          }
        }

        .error-details,
        .warning-details,
        .success-details {
          padding: 12px;
          border-radius: 4px;
          background-color: #fff;
        }

        .solution-tips {
          padding: 12px;
          background-color: #e6f7ff;
          border: 1px solid #91d5ff;
          border-radius: 4px;

          ul {
            li {
              color: #1890ff;
              font-size: 13px;
              line-height: 1.8;
            }
          }
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
