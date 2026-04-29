<template>
  <el-dialog
    title="修改直播片段管理"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="850px">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="90px">
      <el-form-item label="脚本名称" prop="scriptId">
        <el-select v-model="dataForm.scriptId" filterable placeholder="请选择脚本" style="width: 100%">
          <el-option v-for="item in scriptList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="片段名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="片段名称"></el-input>
      </el-form-item>
      <el-form-item label="片段内容">
        <el-select v-model="segmentVariable" placeholder="快捷输入直播变量" style="width: 100%" @change="handleVariableSelect">
          <el-option label="下个整时间隔" value="下个整时间隔"></el-option>
          <el-option label="下个整点" value="下个整点"></el-option>
          <el-option label="当前时间" value="当前时间"></el-option>
          <el-option label="当前在线人数" value="当前在线人数"></el-option>
          <el-option label="用户昵称" value="用户昵称"></el-option>
          <el-option label="当前日期" value="当前日期"></el-option>
          <el-option label="重复用户发言" value="重复用户发言"></el-option>
          <el-option label="下个10分间隔" value="下个10分间隔"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="">
        <div class="tips-container">
          <div class="tip-item">[]中括号: 用于对话术文本进行编组，将随机选用一组，如[xx{x|xx}x][yy{y|yy}y]将优先取其中一个中括号内话术;</div>
          <div class="tip-item">{}花括号: 用于对话术内容的开始和结束、特珠变量、片段名称，如{欢迎|感谢}{大家|宝宝}进入直播间;</div>
          <div class="tip-item">()小括号: (5|10)代表在一句话后随机停顿5或10秒,(我是助手|我是助播)代表随机助播话术,(c:\1.mp3|c:\2.mp3)代表随机音频;</div>
          <div class="tip-item">|竖线: 用于对{}话术内容进行随机选用，见花括号说明;</div>
          <div class="tip-item">请按照正常说话的停顿方式进行标点符号的使用,其中: "," 逗号会有语气的停顿，"。" 句号在生成语音时会进行断句处理;</div>
          <div class="tip-item">对于比较长的话术文本,请在合适的位置添加句号，字数越少语音生成的速度越快，一句话最多120字(解析后);</div>
          <div class="tip-item">另外句号还起到自动互动的功能，需要在客户端主播管控里勾选自动互动(默认启用);</div>
          <div class="tip-item">客户端勾选自动互动后,每个句号处都会按设定概率进行互动,无需在话术中加入{行为互动}和{关键词互动}标签;</div>
        </div>
      </el-form-item>
      <el-form-item label="" prop="content">
        <el-input
          type="textarea"
          :rows="8"
          v-model="dataForm.content"
          placeholder="请输入片段内容"
          maxlength="5000"
          @input="handleContentInput"
          @blur="saveToHistory">
        </el-input>
        <div class="word-count" :class="{ 'warning': dataForm.content.length > 4500 }">
          {{ dataForm.content.length }}/5000
        </div>
      </el-form-item>
      <el-form-item label="" class="ai-generated-section">
        <div class="ai-generated-header">
          <span class="label-text">AI生成内容</span>
          <el-button type="primary" size="mini" @click="replaceContent" :disabled="!aiGeneratedContent">
            一键替换
          </el-button>
        </div>
        <el-input
          type="textarea"
          :rows="8"
          v-model="aiGeneratedContent"
          placeholder="AI生成的内容将显示在这里，可以直接编辑"
          maxlength="5000"
          @input="handleAIContentInput"
          class="ai-generated-textarea">
        </el-input>
        <div class="word-count" :class="{ 'warning': aiGeneratedContent && aiGeneratedContent.length > 4500 }">
          {{ aiGeneratedContent ? aiGeneratedContent.length : 0 }}/5000
        </div>
      </el-form-item>
      <el-form-item label="" prop="content2">
        <el-input
          type="textarea"
          :rows="4"
          v-model="dataForm.content2"
          placeholder="请输入AI提示词（可选，不输入将使用默认提示词）"
          maxlength="1000"
          @input="handleContent2Input">
        </el-input>
        <div class="word-count" :class="{ 'warning': dataForm.content2.length > 900 }">
          {{ dataForm.content2.length }}/1000
        </div>
      </el-form-item>
      <el-form-item label="">
        <div class="action-buttons">
          <el-button type="danger" size="small" @click="generateAIContent" :loading="isGenerating">
            {{ isGenerating ? '生成中...' : 'GPT智能编写' }}
          </el-button>
          <el-button type="warning" size="small" disabled>50字一句(使用F5TTS时)</el-button>
          <el-button type="warning" size="small" @click="addSmartVariables" :loading="isAddingVariables">
            {{ isAddingVariables ? '处理中...' : '智能变量' }}
          </el-button>
          <el-button type="success" size="small" @click="undoContentEdit" :disabled="contentHistory.length === 0">
            返回上一次编辑
          </el-button>
          <el-button type="primary" size="small" disabled>检查语法</el-button>
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        segmentVariable: '',
        aiGeneratedContent: '', // AI生成的内容
        isGenerating: false, // 是否正在生成
        isAddingVariables: false, // 是否正在添加智能变量
        contentHistory: [], // 片段内容的历史记录
        lastSavedContent: '', // 上次保存的内容（用于对比是否真正改变）
        dataForm: {
          id: 0,
          scriptId: '',
          name: '',
          content: '',
          content2: ''
        },
        dataRule: {
          scriptId: [
            { required: true, message: '脚本不能为空', trigger: 'blur' }
          ],
          name: [
            { required: true, message: '片段名称不能为空', trigger: 'blur' }
          ],
          content: [
            { required: true, message: '片段内容不能为空', trigger: 'blur' },
            { max: 5000, message: '片段内容不能超过5000个字符', trigger: 'blur' }
          ]
        },

        scriptList: []
      }
    },
    methods: {
      // 保存当前内容到历史记录（只在有实质性改变时）
      saveToHistory() {
        const currentContent = this.dataForm.content || ''
        // 去除首尾空白进行对比
        const trimmedCurrent = currentContent.trim()
        const trimmedLast = this.lastSavedContent.trim()

        // 只有当内容真正改变时才保存
        if (trimmedCurrent !== trimmedLast && trimmedLast !== '') {
          this.contentHistory.push(this.lastSavedContent)
          // 限制历史记录数量，最多保留50条
          if (this.contentHistory.length > 50) {
            this.contentHistory.shift()
          }
        }

        // 更新最后保存的内容
        this.lastSavedContent = currentContent
      },
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.aiGeneratedContent = '' // 清空AI生成内容
        this.isGenerating = false
        this.isAddingVariables = false
        this.contentHistory = [] // 清空历史记录
        this.lastSavedContent = '' // 清空上次保存内容
        this.getScriptList()
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          // 确保 content2 字段始终存在
          this.$set(this.dataForm, 'content2', '')
          if (this.dataForm.id) {
            this.$http.get(`/apps/live-segment/info/${this.dataForm.id}`).then(({data}) => {
              if (data && data.code === 0) {
                // 使用 Object.assign 保持响应式
                Object.assign(this.dataForm, {
                  id: data.liveSegment.id,
                  scriptId: data.liveSegment.scriptId,
                  name: data.liveSegment.name,
                  content: data.liveSegment.content || '',
                  content2: data.liveSegment.content2 || ''
                })
                // 初始化lastSavedContent
                this.lastSavedContent = data.liveSegment.content || ''
              }
            })
          } else {
            // 新增时确保字段初始化
            this.dataForm.content = ''
            this.dataForm.content2 = ''
            this.lastSavedContent = ''
          }
        })
      },

      // 获取脚本列表
      getScriptList () {
        this.$http.get('/apps/live-script/all').then(({data}) => {
          if (data && data.code === 0) {
            this.scriptList = data.list
          }
        })
      },

      // 处理变量选择
      handleVariableSelect(value) {
        if (value) {
          this.dataForm.content += `{${value}}`
          // 清空选择
          this.$nextTick(() => {
            this.segmentVariable = ''
          })
        }
      },

      // 处理内容输入
      handleContentInput(value) {
        if (value.length > 5000) {
          this.dataForm.content = value.slice(0, 5000)
        }
      },

      // 处理第二个内容输入
      handleContent2Input(value) {
        if (value.length > 1000) {
          this.dataForm.content2 = value.slice(0, 1000)
        }
      },

      // 处理AI生成内容输入
      handleAIContentInput(value) {
        if (value && value.length > 5000) {
          this.aiGeneratedContent = value.slice(0, 5000)
        }
      },

      // 返回上一次编辑
      undoContentEdit() {
        // 检查是否有历史记录
        if (this.contentHistory.length === 0) {
          this.$message.warning('没有可以返回的历史记录')
          return
        }

        // 从历史记录中取出最后一项
        const previousContent = this.contentHistory.pop()

        // 恢复内容
        this.dataForm.content = previousContent

        // 更新lastSavedContent为当前内容
        this.lastSavedContent = previousContent

        this.$message.success('已返回上一次编辑')
      },

      // 一键替换：将AI生成的内容替换到片段内容
      replaceContent() {
        if (!this.aiGeneratedContent) {
          this.$message.warning('没有AI生成的内容可以替换')
          return
        }

        this.$confirm('确定要用AI生成的内容替换当前片段内容吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 替换前先保存历史
          this.saveToHistory()
          // 执行替换
          this.dataForm.content = this.aiGeneratedContent
          // 更新lastSavedContent
          this.lastSavedContent = this.aiGeneratedContent
          this.$message.success('替换成功')
        }).catch(() => {})
      },

      // 智能变量：将话术中的通用词汇替换为同义词组
      addSmartVariables() {
        // 检查片段内容是否为空
        if (!this.dataForm.content || this.dataForm.content.trim() === '') {
          this.$message.warning('请先输入片段内容')
          return
        }

        // 防止重复点击
        if (this.isAddingVariables) {
          this.$message.warning('正在处理中，请稍候...')
          return
        }

        // 保存历史（在修改前）
        this.saveToHistory()

        // 重置状态
        this.isAddingVariables = true

        // 显示全屏loading
        const loading = this.$loading({
          lock: true,
          text: 'AI正在添加智能变量，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)',
          customClass: 'ai-loading'
        })

        // 调用后端智能变量接口
        const requestData = {
          projectId: (this.$store.state.user && this.$store.state.user.projectId) || 1,
          input: this.dataForm.content2 ? this.dataForm.content2.trim() : '', // 用户的额外要求
          context: this.dataForm.content || '', // 原始话术
          scriptType: 'smart_variables'
        }

        console.log('智能变量请求参数:', requestData)

        this.$http.post('/apps/ai-script/add-smart-variables', requestData)
          .then(({data}) => {
            console.log('智能变量响应:', data)
            if (data && data.code === 0) {
              // 直接替换片段内容
              const result = String(data.data || '')
              if (result) {
                this.dataForm.content = result
                // 更新lastSavedContent
                this.lastSavedContent = result
                this.$message.success('智能变量添加成功')
              } else {
                this.$message.warning('AI返回内容为空')
              }
            } else {
              const errorMsg = (data && (data.msg || data.message)) || '智能变量添加失败'
              this.$message.error(errorMsg)
              console.error('智能变量添加失败，服务器返回:', data)
            }
          })
          .catch(error => {
            console.error('智能变量请求异常:', error)
            let errorMsg = '智能变量添加失败'
            if (error.response) {
              const msg = (error.response.data && error.response.data.msg) || error.response.statusText || '服务器错误'
              errorMsg += '：' + msg
            } else if (error.request) {
              errorMsg += '：网络连接失败，请检查后端服务是否正常'
            } else {
              errorMsg += '：' + (error.message || '未知错误')
            }
            this.$message.error(errorMsg)
          })
          .finally(() => {
            // 关闭loading
            loading.close()
            // 确保无论成功失败都重置状态
            this.isAddingVariables = false
          })
      },

      // GPT智能编写
      generateAIContent() {
        // 防止重复点击
        if (this.isGenerating) {
          this.$message.warning('正在生成中，请稍候...')
          return
        }

        // 重置状态
        this.isGenerating = true
        this.aiGeneratedContent = ''

        // 显示全屏loading
        const loading = this.$loading({
          lock: true,
          text: 'AI正在生成话术，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)',
          customClass: 'ai-loading'
        })

        // 调用后端AI话术生成接口
        // 如果用户没有输入提示词，传空字符串，后端会使用ai_config表中的system_prompt
        const requestData = {
          projectId: (this.$store.state.user && this.$store.state.user.projectId) || 1,
          input: this.dataForm.content2 ? this.dataForm.content2.trim() : '',
          context: this.dataForm.content || '',
          scriptType: 'segment',
          maxLength: 5000
        }

        console.log('AI生成请求参数:', requestData)

        this.$http.post('/apps/ai-script/generate', requestData)
          .then(({data}) => {
            console.log('AI生成响应:', data)
            if (data && data.code === 0) {
              // 确保返回的内容是字符串
              this.aiGeneratedContent = String(data.data || '')
              if (this.aiGeneratedContent) {
                this.$message.success('AI内容生成成功')
              } else {
                this.$message.warning('AI返回内容为空')
              }
            } else {
              // 显示具体错误信息
              const errorMsg = (data && (data.msg || data.message)) || 'AI内容生成失败'
              this.$message.error(errorMsg)
              console.error('AI生成失败，服务器返回:', data)
            }
          })
          .catch(error => {
            console.error('AI生成请求异常:', error)
            // 显示更详细的错误信息
            let errorMsg = 'AI内容生成失败'
            if (error.response) {
              // 服务器返回错误
              const msg = (error.response.data && error.response.data.msg) || error.response.statusText || '服务器错误'
              errorMsg += '：' + msg
            } else if (error.request) {
              // 请求已发送但没有响应
              errorMsg += '：网络连接失败，请检查后端服务是否正常'
            } else {
              // 其他错误
              errorMsg += '：' + (error.message || '未知错误')
            }
            this.$message.error(errorMsg)
          })
          .finally(() => {
            // 关闭loading
            loading.close()
            // 确保无论成功失败都重置状态
            this.isGenerating = false
          })
      },

      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http.post(`/apps/live-segment/${!this.dataForm.id ? 'save' : 'update'}`, {
              ...this.dataForm
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
.tips-container {
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;

  .tip-item {
    color: #409eff;
    font-size: 13px;
    line-height: 1.8;
    margin-bottom: 4px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.word-count {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 5px;

  &.warning {
    color: #E6A23C;
  }
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

// AI生成内容区域样式
.ai-generated-section {
  margin-top: 10px;
  margin-bottom: 10px;

  .ai-generated-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .label-text {
      font-size: 14px;
      color: #606266;
      font-weight: 500;
    }
  }

  .ai-generated-textarea {
    ::v-deep .el-textarea__inner {
      background-color: #f0f9ff;  // 浅蓝色背景，表示AI生成
      border-color: #91d5ff;      // 蓝色边框
      color: #333;

      &:hover {
        border-color: #40a9ff;
      }

      &:focus {
        border-color: #409eff;
      }
    }
  }
}

// 调整表单项间距
::v-deep .el-form-item {
  margin-bottom: 18px;
}

// AI加载动画自定义样式
::v-deep .ai-loading {
  .el-loading-spinner {
    top: 50%;
    margin-top: -40px;

    .el-icon-loading {
      font-size: 50px;
      color: #409eff;
    }

    .el-loading-text {
      font-size: 16px;
      color: #fff;
      margin-top: 15px;
      font-weight: 500;
      letter-spacing: 1px;
    }
  }
}
</style>
