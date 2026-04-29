<template>
  <div>
    <el-dialog
        :before-close="handleClose"
        :close-on-click-modal="false"
        :visible.sync="dialogVisible"
        custom-class="gpt-model-dialog"
        title="GPT大模型"
        width="50%">
      <el-form class="dialog-form-content" label-width="180px" size="small">
        <el-divider class="red" content-position="left">GPT配置</el-divider>
        <!-- GPT配置 -->
        <el-form-item label="创建GPT">
          <el-tag class="tip-text" size="small">[ 第一次使用必读！支持OpenAI、豆包、月之暗面等所有兼容OpenAI API的服务 ]</el-tag>
        </el-form-item>
        <el-form-item label="GPT BaseUrl">
          <el-input v-model="baseUrl" placeholder="例如: https://ark.cn-beijing.volces.com/api/v3" size="small"></el-input>
        </el-form-item>
        <el-form-item label="GPT Model">
          <el-input v-model="model" placeholder="例如: doubao-1-5-pro-32k-250115" size="small"></el-input>
        </el-form-item>
        <el-form-item label="GPT ApiKey">
          <el-input v-model="apiKey" placeholder="请输入GPT ApiKey" show-password size="small" type="password"></el-input>
        </el-form-item>
        <el-form-item label=" ">
          <el-button :loading="testing" size="small" type="primary" @click="testConnection">测试连接</el-button>
          <el-tag class="tip-text m-l-10" size="small">建议先测试连接，确保配置正确</el-tag>
        </el-form-item>

        <el-divider class="red" content-position="left">GPT知识库</el-divider>
        <el-form-item label="知识库来源">
          <div class="collect-knowledge">
            <el-radio v-model="knowledge" label="1">本地知识库</el-radio>
            <div class="m-l-10">
              <el-button size="small" type="success" @click="collectGoodsKnowledge">采集别人直播间小黄车商品知识库</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="本地知识库">
          <div class="local-knowledge">
            <el-input v-model="knowledgeFileName" disabled placeholder="请选择知识库" size="small"></el-input>
            <div class="m-l-10 m-r-10">
              <el-button size="small" type="success" @click="selectKnowledge">选择知识库</el-button>
            </div>
            <div>
              <el-button size="small" type="danger" @click="clearKnowledge">清空</el-button>
            </div>
          </div>
          <div class="document-format m-t-10">知识库文档格式："Excel (xlsx/xls)"，不超过500KB</div>
          <div class="m-t-10">
            <el-button size="small" type="text" @click="downloadTemplate">📥 下载知识库模板</el-button>
            <el-tag class="tip-text m-l-10" size="small">格式: 商品链接号 | 商品名称 | 商品详情</el-tag>
          </div>
        </el-form-item>

        <el-divider class="red" content-position="left">GPT话术润色</el-divider>
        <el-form-item label="实时润色">
          <el-checkbox v-model="polishEnabled">GPT实时润色改写话术</el-checkbox>
          <div class="m-t-10">
            <el-tag class="tip-text" size="small">启用后，播放的话术会先通过GPT润色改写，让话术更自然流畅</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="提示词">
          <el-input v-model="polishTipWord" :rows="3" placeholder="输入润色提示词" type="textarea"/>
        </el-form-item>

        <el-divider class="red" content-position="left">GPT自动话术</el-divider>
        <el-form-item label="自动话术">
          <el-checkbox v-model="autoScriptEnabled" @change="handleAutoScriptChange">启用自动话术生成</el-checkbox>
          <div class="m-t-10">
            <el-tag class="tip-text" size="small" type="warning">启用前，请确保已经选择知识库。启用后，将根据知识库内容，自动生成话术进行AI语音播报。知识库仅支持excel格式！</el-tag>
          </div>
          <div class="m-t-10">
            <el-switch v-model="generateSwitch"
                       active-color="#13ce66"
                       active-text="随机"
                       inactive-color="#ff4949"
                       inactive-text="顺序">
            </el-switch>
            <el-tag class="tip-text m-l-10" size="small">从知识库中选择商品的方式</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="提示词">
          <el-input v-model="autoTipWord" :rows="5" placeholder="输入话术生成提示词" type="textarea"/>
          <div class="m-t-10">
            <el-button :loading="testLoading" size="small" type="primary" @click="testGenerateScript">
              测试生成话术
            </el-button>
            <el-tag class="tip-text m-l-10" size="small">点击测试查看GPT生成的原始文本</el-tag>
          </div>
        </el-form-item>

        <el-divider class="red" content-position="left">GPT介入互动</el-divider>
        <el-form-item label="GPT互动">
          <el-checkbox v-model="interactionEnabled" @change="saveGptConfig">启用</el-checkbox>
          <div class="m-t-10">
            <el-tag class="tip-text" size="small">重复用户发言：AI主播会复述用户的打字；点用户名字：AI主播会说用户名字；随后追加GPT回答的话术。</el-tag>
          </div>
          <div class="m-t-10 flex-colum-center">
            <el-checkbox v-model="repeatUserContent" @change="saveGptConfig">启用重复用户发言，并且在之前添加语气助词：</el-checkbox>
            <el-input v-model="repeatUserContentPrefix" size="mini" @change="saveGptConfig"></el-input>
          </div>
          <div class="m-t-10 flex-colum-center">
            <el-checkbox v-model="sayUserName" @change="saveGptConfig">启用点发言用户名字，并且在其之后添加称呼：</el-checkbox>
            <el-input v-model="sayUserNamePrefix" size="mini" @change="saveGptConfig"></el-input>
          </div>
          <div class="m-t-10">
            <el-checkbox v-model="useFragmentData">使用片段数据辅助回答</el-checkbox>
            <el-tag class="tip-text m-l-10" size="small">GPT会参考当前播放片段的内容进行回答</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="从知识库中回答">
          <el-checkbox v-model="knowledgeEnabled" @change="saveGptConfig">启用</el-checkbox>
          <el-tag class="tip-text m-l-10" size="small">不启用，GPT将自主回答，启用后，GPT从选择的知识库中回答</el-tag>
        </el-form-item>

        <el-divider class="red" content-position="left">GPT互动</el-divider>
        <el-form-item label="互动提示词">
          <el-input v-model="interactionTipWord" :rows="10" placeholder="输入互动提示词" type="textarea"/>
        </el-form-item>
        <el-form-item label="问">
          <div class="flex-colum-center">
            <el-input v-model="question" size="mini" placeholder="问我点什么吧？"/>
            <el-button icon="el-icon-s-promotion" size="small" type="primary" @click="sendQuestion" class="m-l-5" :loading="questionLoading"></el-button>
            <el-button size="small" type="danger" @click="stopQuestion" class="m-l-5">停止回答</el-button>
          </div>
        </el-form-item>
        <el-form-item label="答">
          <el-input v-model="answer" :rows="5" placeholder="" type="textarea"/>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="callback()">关闭</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </span>
    </el-dialog>

    <!-- 商品知识库采集对话框 -->
    <KnowledgeCollectDialog ref="knowledgeCollectDialog"></KnowledgeCollectDialog>
  </div>
</template>

<script>
import gptService from '../../../utils/gptService'
import knowledgeBase from '../../../utils/knowledgeBase'
import KnowledgeCollectDialog from './knowledgeCollectDialog.vue'

const {ipcRenderer} = require("electron");

export default {
  name: "GptModelDialog",
  components: {
    KnowledgeCollectDialog
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    isPlaying: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // GPT配置
      baseUrl: "https://ark.cn-beijing.volces.com/api/v3",
      model: "doubao-1-5-pro-32k-250115",
      apiKey: "",

      // 知识库
      knowledge: "1",
      knowledgeValue: "",
      knowledgeFileName: "",

      // 话术润色
      polishEnabled: false,
      polishTipWord: "你是一名正在直播带货的主播，帮我把这句话术进行润色改写，要求：意思不变，字数不要偏差太大，不要包含敏感违禁词，生成的话术自然逼真接地气",

      // 自动话术
      autoScriptEnabled: false,
      generateSwitch: false, // false=顺序，true=随机
      autoTipWord: '',  // 默认为空，完全由用户自定义

      // GPT互动
      interactionEnabled: false,
      interactionTipWord: "# 角色\n" +
          "你是这位 23 岁未婚的真人主播。\n" +
          "#性格特点\n" +
          "善于交流。\n" +
          "1.开朗活泼,\n" +
          "2.真诚热情，对待观众友好,\n" +
          "#人际关系\n" +
          "1.与观众保持良好互动关系。\n" +
          "# 过往经历\n" +
          "1.从事主播工作，积累了一定经验。\n" +
          "#经典台词or 口头禅\n" +
          "1.“家人们，欢迎来到我的直播间呀!\n" +
          "2.“这个问题呀，我来告诉你哦。\n" +
          "3.“哈哈，感谢大家的支持呀!\n" +
          "# 相关限制\n" +
          "1.禁止使用极限词、违禁词、暗示性引导用语、不文明用语、封建迷信类、暴力色情类词语、疑似医疗用语\n" +
          "2.回答问题要简短，字数在 40 字以内。\n" +
          "3.支持多平台进行直播。\n" +
          "4.要按照要求重复提问词语后再回答问题\n" +
          "5.尽量不要重复回复，适当改变回复话术。",
      question: '',
      questionLoading: false,
      answer: '',

      // 互动选项
      repeatUserContent: false,
      repeatUserContentPrefix: "啊|嗯|呃|呀|哇|哈|||||",
      sayUserName: false,
      sayUserNamePrefix: "亲|亲啊|铁|老铁|宝贝|老板||||",
      useFragmentData: false,
      knowledgeEnabled: false,

      // 测试相关
      testing: false,
      testLoading: false,

      // 初始数据（用于检测变更）
      initialData: null,
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        if (!val) {
          this.$emit('close')
        }
      }
    },
    // 检测是否有修改
    hasChanges() {
      if (!this.initialData) return false
      return JSON.stringify(this.getCurrentData()) !== JSON.stringify(this.initialData)
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.loadSettings()
      }
    }
  },
  methods: {
    /**
     * 处理自动话术启用变更
     */
    handleAutoScriptChange(value) {
      // Task1: 开播中不能启用自动话术
      if (value && this.isPlaying) {
        this.$message.warning('开播中不能启用自动话术，请先停播')
        this.$nextTick(() => {
          this.autoScriptEnabled = false
        })
        return
      }

      // Task2: 知识库没有选择不能启用自动话术
      if (value && !knowledgeBase.isKnowledgeLoaded()) {
        this.$message.warning('请先选择知识库文件！启用自动话术功能需要知识库数据')
        this.$nextTick(() => {
          this.autoScriptEnabled = false
        })
        return
      }
    },

    /**
     * 加载配置
     */
    loadSettings() {
      // 加载GPT配置
      const config = gptService.getConfig()
      this.baseUrl = config.baseUrl
      this.model = config.model
      this.apiKey = config.apiKey

      // 加载话术润色配置
      this.polishEnabled = JSON.parse(localStorage.getItem('gptPolishEnabled') || 'false')
      this.polishTipWord = localStorage.getItem('gptPolishTipWord') || this.polishTipWord

      // 加载自动话术配置
      this.autoScriptEnabled = JSON.parse(localStorage.getItem('gptAutoScriptEnabled') || 'false')
      this.generateSwitch = JSON.parse(localStorage.getItem('gptGenerateSwitch') || 'false')
      this.autoTipWord = localStorage.getItem('gptAutoTipWord') || this.autoTipWord

      // 加载互动配置
      this.interactionEnabled = JSON.parse(localStorage.getItem('gptInteractionEnabled') || 'false')
      this.interactionTipWord = localStorage.getItem('gptInteractionTipWord') || this.interactionTipWord
      this.repeatUserContent = JSON.parse(localStorage.getItem('gptRepeatUserContent') || 'false')
      this.sayUserName = JSON.parse(localStorage.getItem('gptSayUserName') || 'false')
      this.useFragmentData = JSON.parse(localStorage.getItem('gptUseFragmentData') || 'false')
      this.knowledgeEnabled = JSON.parse(localStorage.getItem('gptKnowledgeEnabled') || 'false')

      // 加载知识库
      if (knowledgeBase.isKnowledgeLoaded()) {
        this.knowledgeFileName = knowledgeBase.getFileName()
        this.knowledgeValue = knowledgeBase.getFilePath()
      }

      // 保存初始数据（用于检测变更）
      this.$nextTick(() => {
        this.initialData = this.getCurrentData()
      })
    },

    /**
     * 获取当前数据（用于变更检测）
     */
    getCurrentData() {
      return {
        baseUrl: this.baseUrl,
        model: this.model,
        apiKey: this.apiKey,
        knowledgeFileName: this.knowledgeFileName,
        polishEnabled: this.polishEnabled,
        polishTipWord: this.polishTipWord,
        autoScriptEnabled: this.autoScriptEnabled,
        generateSwitch: this.generateSwitch,
        autoTipWord: this.autoTipWord,
        interactionEnabled: this.interactionEnabled,
        interactionTipWord: this.interactionTipWord,
        repeatUserContent: this.repeatUserContent,
        sayUserName: this.sayUserName,
        useFragmentData: this.useFragmentData,
        knowledgeEnabled: this.knowledgeEnabled,
      }
    },

    /**
     * 关闭前检查
     */
    handleClose(done) {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要关闭吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          done()
        }).catch(() => {
          // 取消关闭
        })
      } else {
        done()
      }
    },

    /**
     * 测试连接
     */
    async testConnection() {
      if (!this.apiKey) {
        this.$message.warning('请先输入API Key')
        return
      }

      this.testing = true

      try {
        // 先保存配置
        gptService.saveConfig({
          baseUrl: this.baseUrl,
          model: this.model,
          apiKey: this.apiKey
        })

        // 测试连接
        const result = await gptService.testConnection()

        if (result.success) {
          this.$message.success('连接成功！GPT配置有效')
        } else {
          this.$message.error('连接失败：' + result.message)
        }
      } catch (error) {
        this.$message.error('测试失败：' + error.message)
      } finally {
        this.testing = false
      }
    },

    /**
     * 选择知识库
     */
    async selectKnowledge() {
      try {
        const filePath = await knowledgeBase.selectExcelFile()

        if (filePath) {
          this.knowledgeValue = filePath
          this.knowledgeFileName = knowledgeBase.getFileName()
          this.$message.success(`知识库加载成功，共 ${knowledgeBase.getCount()} 条记录`)
        }
      } catch (error) {
        this.$message.error('加载知识库失败：' + error.message)
      }
    },

    /**
     * 清空知识库
     */
    clearKnowledge() {
      this.$confirm('确定要清空知识库吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        knowledgeBase.clear()
        this.knowledgeValue = ''
        this.knowledgeFileName = ''
        this.$message.success('知识库已清空')
      }).catch(() => {
      })
    },

    /**
     * 采集别人直播间小黄车商品知识库
     */
    collectGoodsKnowledge() {
      // 打开商品知识库采集对话框
      this.$refs.knowledgeCollectDialog.dialogVisible = true
    },

    /**
     * 下载知识库模板
     */
    async downloadTemplate() {
      try {
        const {ipcRenderer} = require('electron')

        // 选择保存位置
        const result = await ipcRenderer.invoke('dialog:saveFile', {
          defaultPath: 'GPT知识库模板.xlsx',
          filters: [
            {name: 'Excel Files', extensions: ['xlsx']}
          ]
        })

        if (!result.canceled && result.filePath) {
          const exportResult = await knowledgeBase.constructor.exportTemplate(result.filePath)

          if (exportResult.success) {
            this.$message.success('模板已下载到：' + result.filePath)
          } else {
            this.$message.error('下载失败：' + exportResult.error)
          }
        }
      } catch (error) {
        this.$message.error('下载失败：' + error.message)
      }
    },

    /**
     * 保存设置
     */
    saveSettings() {
      try {
        // ✅ 如果启用了自动话术，检查知识库是否已选择
        if (this.autoScriptEnabled && !knowledgeBase.isKnowledgeLoaded()) {
          this.$message.warning('请先选择知识库文件！启用自动话术功能需要知识库数据。')
          return
        }

        // 保存GPT配置
        const saveResult = gptService.saveConfig({
          baseUrl: this.baseUrl,
          model: this.model,
          apiKey: this.apiKey
        })

        if (!saveResult.success) {
          throw new Error(saveResult.error)
        }

        // 保存话术润色配置
        localStorage.setItem('gptPolishEnabled', JSON.stringify(this.polishEnabled))
        localStorage.setItem('gptPolishTipWord', this.polishTipWord)

        // 保存自动话术配置
        localStorage.setItem('gptAutoScriptEnabled', JSON.stringify(this.autoScriptEnabled))
        localStorage.setItem('gptGenerateSwitch', JSON.stringify(this.generateSwitch))
        localStorage.setItem('gptAutoTipWord', this.autoTipWord)

        // 保存互动配置
        localStorage.setItem('gptInteractionTipWord', this.interactionTipWord)
        localStorage.setItem('gptUseFragmentData', JSON.stringify(this.useFragmentData))

        // 通知父组件配置已更新
        this.$emit('settings-saved', {
          polishEnabled: this.polishEnabled,
          autoScriptEnabled: this.autoScriptEnabled,
          interactionEnabled: this.interactionEnabled
        })

        this.$message.success('设置已保存')
        // 更新初始数据，保存后不再检测为有修改
        this.initialData = this.getCurrentData()
        this.dialogVisible = false
      } catch (error) {
        this.$message.error('保存失败：' + error.message)
      }
    },
    saveGptConfig() {
      localStorage.setItem('gptInteractionEnabled', JSON.stringify(this.interactionEnabled))
      localStorage.setItem('gptRepeatUserContent', JSON.stringify(this.repeatUserContent))
      localStorage.setItem('gptRepeatUserContentPrefix', JSON.stringify(this.repeatUserContentPrefix))
      localStorage.setItem('gptSayUserName', JSON.stringify(this.sayUserName))
      localStorage.setItem('gptSayUserNamePrefix', JSON.stringify(this.sayUserNamePrefix))
      localStorage.setItem('gptKnowledgeEnabled', JSON.stringify(this.knowledgeEnabled))
      // 通知 GPT 监听
      ipcRenderer.send('send-gpt-config', {
        enableGptInteraction: this.interactionEnabled,
        enableKnowledge: this.knowledgeEnabled,
        repeatUserStatements: this.repeatUserContent,
        clickUsername: this.sayUserName
      })
    },
    /**
     * 测试生成话术
     */
    async testGenerateScript() {
      try {
        // 检查知识库是否已加载
        if (!knowledgeBase.isKnowledgeLoaded()) {
          this.$message.error('请先选择知识库！')
          return
        }

        // 检查是否已配置 GPT
        if (!gptService.checkInitialized()) {
          this.$message.error('请先配置 GPT API！')
          return
        }

        this.testLoading = true

        // 获取商品信息
        let product = null
        if (this.generateSwitch) {
          // 随机模式
          product = knowledgeBase.getRandomItem()
        } else {
          // 顺序模式 - 始终获取第一个
          product = knowledgeBase.getByIndex(0)
        }

        if (!product) {
          this.$message.error('知识库中没有商品信息！')
          this.testLoading = false
          return
        }

        // 格式化商品信息
        const productInfo = knowledgeBase.formatForPrompt(product)

        // 调用 GPT 生成话术
        const result = await gptService.generateScript(productInfo, this.autoTipWord)

        if (result.success && result.content) {
          // 🎯 模拟分段逻辑（与 LivePaneSegmentTable.vue 中的逻辑一致）
          const segments = this.simulateSplit(result.content)

          // 构建显示内容
          const displayContent = `
<div style="font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.8;">
  <div style="margin-bottom: 16px; padding: 12px; background: #ecf5ff; border-radius: 4px; border-left: 4px solid #409eff;">
    <strong style="color: #409eff;">📝 原始文本</strong>
    <div style="margin-top: 8px; color: #606266; white-space: pre-wrap; word-wrap: break-word;">
${result.content}
    </div>
  </div>

  <div style="margin-bottom: 16px; padding: 12px; background: #f0f9ff; border-radius: 4px; border-left: 4px solid #67c23a;">
    <strong style="color: #67c23a;">✂️ 分段结果 (共 ${segments.length} 个段落)</strong>
  </div>

  ${segments.map((seg, index) => `
  <div style="margin-bottom: 12px; padding: 10px; background: #fff; border: 1px solid #dcdfe6; border-radius: 4px;">
    <div style="display: flex; align-items: center; margin-bottom: 6px;">
      <span style="display: inline-block; min-width: 60px; padding: 2px 8px; background: #409eff; color: white; font-size: 12px; border-radius: 3px;">
        段落 ${index + 1}
      </span>
      <span style="margin-left: 8px; color: #909399; font-size: 13px;">
        ${seg.length} 字
      </span>
    </div>
    <div style="color: #303133; padding-left: 8px;">
      ${seg}
    </div>
  </div>
  `).join('')}

  <div style="margin-top: 16px; padding: 10px; background: #f5f7fa; border-radius: 4px; font-size: 13px; color: #606266;">
    <strong>💡 提示：</strong> 如果分段效果不理想，可以：<br/>
    1. 修改提示词，要求 GPT 在句子间使用标点符号<br/>
    2. 调整话术内容，避免过长的句子<br/>
    3. 使用换行符分隔不同段落
  </div>
</div>
          `

          // 显示生成结果和分段预览
          this.$alert(displayContent, 'GPT 生成测试 - 原始文本和分段预览', {
            confirmButtonText: '确定',
            type: 'success',
            dangerouslyUseHTMLString: true,
            customClass: 'test-script-dialog'
          })

          // 控制台日志
          console.log('========== GPT 生成测试 ==========')
          console.log('商品:', product.name)
          console.log('提示词长度:', this.autoTipWord ? this.autoTipWord.length : 0)
          console.log('生成文本长度:', result.content.length)
          console.log('生成文本:')
          console.log(result.content)
          console.log('\n========== 分段结果 ==========')
          segments.forEach((seg, index) => {
            console.log(`段落 ${index + 1} (${seg.length}字): ${seg}`)
          })
          console.log('=====================================')
        } else {
          this.$message.error('生成失败: ' + (result.error || '未知错误'))
        }
      } catch (error) {
        console.error('测试生成失败:', error)
        this.$message.error('测试生成失败: ' + error.message)
      } finally {
        this.testLoading = false
      }
    },

    /**
     * 🔍 模拟分段逻辑（与 LivePaneSegmentTable.vue 保持一致）
     */
    simulateSplit(script) {
      if (!script) return []

      // 检测换行符
      const hasDoubleNewline = script.includes('\n\n')
      const hasSingleNewline = script.includes('\n')

      let paragraphs = []

      if (hasDoubleNewline) {
        // 按双换行符分割
        paragraphs = script.split('\n\n').map(p => p.trim()).filter(p => p.length > 0)
      } else if (hasSingleNewline) {
        // 按单换行符分割
        paragraphs = script.split('\n').map(p => p.trim()).filter(p => p.length > 0)
      } else {
        // 按标点符号分割（智能分段）
        paragraphs = this.splitByPunctuation(script)
      }

      return paragraphs
    },

    /**
     * 🎯 按标点符号分割（与 LivePaneSegmentTable.vue 保持一致）
     */
    splitByPunctuation(text) {
      // 强标点符号分割
      const strongPunctuation = /[。！？]/
      const sentences = text.split(/([。！？])/).filter(s => s.trim())
      const result = []
      let tempSentence = ''

      for (let i = 0; i < sentences.length; i++) {
        tempSentence += sentences[i]

        if (strongPunctuation.test(sentences[i])) {
          result.push(tempSentence.trim())
          tempSentence = ''
        }
      }

      // 处理剩余内容
      if (tempSentence.trim()) {
        const lastPart = tempSentence.trim()

        // 如果剩余部分很长，按弱标点符号分割
        if (lastPart.length > 15) {
          const weakParts = lastPart.split(/([~，,、])/).filter(s => s.trim())
          let weakTemp = ''

          for (let i = 0; i < weakParts.length; i++) {
            weakTemp += weakParts[i]

            if (/[~，,、]/.test(weakParts[i]) && weakTemp.trim().length >= 10) {
              result.push(weakTemp.trim())
              weakTemp = ''
            }
          }

          if (weakTemp.trim() && weakTemp.trim().length >= 5) {
            result.push(weakTemp.trim())
          }
        } else {
          // 剩余部分较短，直接添加
          result.push(lastPart)
        }
      }

      return result
    },

    /**
     * 取消按钮（检查是否有修改）
     */
    callback() {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要取消吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '返回编辑',
          type: 'warning'
        }).then(() => {
          this.dialogVisible = false
        }).catch(() => {
          // 取消关闭，继续编辑
        })
      } else {
        this.dialogVisible = false
      }
    },

    /**
     * GPT互动提问
     * @returns {Promise<void>}
     */
    async sendQuestion() {
      this.questionLoading = true
      // 构建上下文信息
      let contextInfo = ''
      // 是否加载知识库信息
      if (this.knowledgeEnabled) {
        if (knowledgeBase.isKnowledgeLoaded()) {
          // 尝试从知识库中搜索相关信息
          const searchResults = knowledgeBase.search(this.question)
          if (searchResults.length > 0) {
            contextInfo += knowledgeBase.formatForPrompt(searchResults.slice(0, 2))  // 最多2个结果
          }
        }
      }
      const result = await gptService.replyToKeyword(this.question, contextInfo, this.interactionTipWord)
      if (result.success) {
        if (this.questionLoading) {
          this.answer = result.content
          this.questionLoading = false
        }
      }
    },
    /**
     * 停止提问
     */
    stopQuestion() {
      this.questionLoading = false
    }
  }
}
</script>

<style scoped>
.red {
  color: red;
}

.tip-text {
  font-size: 12px;
}

.collect-knowledge {
  display: flex;
  flex-direction: colum;
  align-items: center;
}

.local-knowledge {
  display: flex;
  flex-direction: colum;
  align-items: center;
}

.document-format {
  font-size: 12px;
  color: gray;
}
</style>

<style>
/* 测试对话框样式 - 全局样式，不使用 scoped */
.test-script-dialog .el-message-box__content {
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.test-script-dialog .el-message-box {
  width: 600px;
}

/* GPT模型对话框样式 - 固定底部按钮 */
.gpt-model-dialog .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.gpt-model-dialog .el-dialog__footer {
  position: sticky;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 15px 20px;
  z-index: 10;
}

.gpt-model-dialog .dialog-form-content {
  padding-bottom: 20px;
}
</style>