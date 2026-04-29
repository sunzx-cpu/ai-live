/**
 * GPT服务类 - 封装OpenAI API调用
 * 复刻自 FlyAiLive v3.3.5
 *
 * @author AI Live Stream Team
 * @date 2025-10-28
 */

import OpenAI from 'openai'

class GPTService {
  constructor() {
    this.client = null
    this.config = {
      baseUrl: '',
      model: '',
      apiKey: ''
    }
    this.isInitialized = false

    // 从localStorage加载配置
    this.loadConfig()
  }

  /**
   * 从localStorage加载配置
   */
  loadConfig() {
    try {
      const baseUrl = localStorage.getItem('gptBaseUrl') || 'https://ark.cn-beijing.volces.com/api/v3'
      const model = localStorage.getItem('gptModel') || 'doubao-1-5-pro-32k-250115'
      const apiKey = localStorage.getItem('gptApiKey') || ''

      this.config = { baseUrl, model, apiKey }

      if (apiKey) {
        this.initializeClient()
      }
    } catch (error) {
      console.error('[GPTService] 加载配置失败:', error)
    }
  }

  /**
   * 保存配置到localStorage
   */
  saveConfig(config) {
    try {
      this.config = { ...this.config, ...config }

      localStorage.setItem('gptBaseUrl', this.config.baseUrl)
      localStorage.setItem('gptModel', this.config.model)
      localStorage.setItem('gptApiKey', this.config.apiKey)

      if (this.config.apiKey) {
        this.initializeClient()
      }

      return { success: true }
    } catch (error) {
      console.error('[GPTService] 保存配置失败:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 初始化OpenAI客户端
   */
  initializeClient() {
    try {
      if (!this.config.apiKey) {
        throw new Error('API Key 未配置')
      }

      this.client = new OpenAI({
        apiKey: this.config.apiKey,
        baseURL: this.config.baseUrl,
        dangerouslyAllowBrowser: true // Electron环境允许浏览器端调用
      })

      this.isInitialized = true
      console.log('[GPTService] OpenAI客户端初始化成功')
    } catch (error) {
      console.error('[GPTService] 初始化客户端失败:', error)
      this.isInitialized = false
      throw error
    }
  }

  /**
   * 测试连接
   */
  async testConnection() {
    try {
      if (!this.isInitialized) {
        this.initializeClient()
      }

      // 发送一个简单的测试请求
      const response = await this.client.chat.completions.create({
        model: this.config.model,
        messages: [
          { role: 'user', content: '你好' }
        ],
        max_tokens: 10
      })

      return {
        success: true,
        message: '连接成功',
        response: response.choices[0].message.content
      }
    } catch (error) {
      console.error('[GPTService] 测试连接失败:', error)
      return {
        success: false,
        message: '连接失败: ' + error.message,
        error: error
      }
    }
  }

  /**
   * 通用GPT调用方法
   * @param {String} userPrompt - 用户提示词
   * @param {String} systemPrompt - 系统提示词
   * @param {Object} options - 可选参数
   */
  async chat(userPrompt, systemPrompt = '', options = {}) {
    try {
      if (!this.isInitialized) {
        throw new Error('GPT服务未初始化，请先配置API参数')
      }

      const messages = []

      // 添加系统提示词
      if (systemPrompt) {
        messages.push({ role: 'system', content: systemPrompt })
      }

      // 添加用户提示词
      messages.push({ role: 'user', content: userPrompt })

      const response = await this.client.chat.completions.create({
        model: this.config.model,
        messages: messages,
        max_tokens: options.maxTokens || 500,
        temperature: options.temperature || 0.7,
        ...options
      })

      return {
        success: true,
        content: response.choices[0].message.content,
        usage: response.usage
      }
    } catch (error) {
      console.error('[GPTService] GPT调用失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }

  /**
   * 话术润色
   * @param {String} script - 原始话术
   * @param {String} customPrompt - 自定义提示词
   */
  async polishScript(script, customPrompt = '') {
    const defaultPrompt = '你是一名正在直播带货的主播，帮我把这句话术进行润色改写，要求：意思不变，字数不要偏差太大，不要包含敏感违禁词，生成的话术自然逼真接地气'
    const systemPrompt = customPrompt || localStorage.getItem('gptPolishTipWord') || defaultPrompt

    return await this.chat(script, systemPrompt, {
      maxTokens: 200
    })
  }

  /**
   * 自动话术生成
   * @param {String} productInfo - 商品信息
   * @param {String} customPrompt - 自定义提示词
   */
  async generateScript(productInfo, customPrompt = '') {
    // ✅ 直接使用传入的提示词，不要 fallback 到 localStorage
    // 如果没有提供提示词，使用简单的默认提示
    let basePrompt = customPrompt || '你是一名正在直播带货的主播，请根据提供的商品信息生成直播话术。'

    // 🎯 添加随机性指令
    const randomInstructions = `

重要要求：
1. 每次生成的开头要有变化，不要总是"家人们欢迎来到我的直播间"
2. 可以用不同的开场方式，比如：
   - 直接介绍商品："今天给大家带来..."
   - 制造紧迫感："限时优惠，错过就没了..."
   - 突出卖点："这款XX超级实用..."
   - 互动式开场："有没有想要XX的家人..."
3. 每句话之间用句号（。）、感叹号（！）或问号（？）结尾
4. 避免使用语气词（哦、呀、啦、呢等）
5. 不要生成动作描述（如"微笑"、"挥手"等）
6. 保持自然流畅，不要生成格式化的标题或序号
7. 控制每句话长度在15-40字之间`

    const finalPrompt = basePrompt + randomInstructions

    return await this.chat(productInfo, finalPrompt, {
      maxTokens: 2000,  // 足够大的 token 限制，避免截断
      temperature: 0.9  // 提高温度参数，增加随机性
    })
  }

  /**
   * 关键词互动回复
   * @param {String} userMessage - 用户发言
   * @param {String} contextInfo - 上下文信息（片段内容/知识库）
   * @param {String} customPrompt - 自定义提示词
   */
  async replyToKeyword(userMessage, contextInfo = '', customPrompt = '') {
    const defaultPrompt = '你是一名正在直播的主播，请根据提供信息回答用户的提问，话术自然逼真接地气，不超过20字。'
    const systemPrompt = customPrompt || localStorage.getItem('gptAutoGuanjianciAskPreContent') || defaultPrompt

    let fullPrompt = userMessage
    if (contextInfo) {
      fullPrompt = `参考信息：${contextInfo}\n\n用户提问：${userMessage}`
    }

    return await this.chat(fullPrompt, systemPrompt, {
      maxTokens: 2000,  // 足够大的 token 限制，避免截断
      temperature: 0.9  // 提高温度参数，增加随机性
    })
  }

  /**
   * 行为互动回复
   * @param {String} userAction - 用户行为（进入、关注、点赞等）
   * @param {String} userName - 用户名
   * @param {String} contextInfo - 上下文信息
   * @param {String} customPrompt - 自定义提示词
   */
  async replyToAction(userAction, userName = '', contextInfo = '', customPrompt = '') {
    const defaultPrompt = '你是一名正在直播的主播，请根据提供信息生成与用户的行为进行互动的直播话术，话术自然逼真接地气，不超过20字。'
    const systemPrompt = customPrompt || localStorage.getItem('gptAutoXingweiAskPreContent') || defaultPrompt

    let fullPrompt = `用户${userName || ''}${userAction}`
    if (contextInfo) {
      fullPrompt = `参考信息：${contextInfo}\n\n${fullPrompt}`
    }

    return await this.chat(fullPrompt, systemPrompt, {
      maxTokens: 100
    })
  }

  /**
   * 获取当前配置
   */
  getConfig() {
    return { ...this.config }
  }

  /**
   * 检查是否已初始化
   */
  checkInitialized() {
    return this.isInitialized
  }
}

// 创建单例
const gptService = new GPTService()

export default gptService
