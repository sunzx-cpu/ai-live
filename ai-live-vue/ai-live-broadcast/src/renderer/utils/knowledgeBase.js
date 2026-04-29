/**
 * 知识库管理类
 * 复刻自 FlyAiLive v3.3.5
 *
 * @author AI Live Stream Team
 * @date 2025-10-28
 */

const { ipcRenderer } = require('electron')
const fs = require('fs')
const path = require('path')

class KnowledgeBase {
  constructor() {
    this.knowledgeData = [] // 存储知识库数据
    this.filePath = ''
    this.isLoaded = false

    // 从localStorage加载已保存的知识库路径
    this.loadSavedPath()
  }

  /**
   * 加载已保存的知识库路径
   */
  loadSavedPath() {
    try {
      const savedPath = localStorage.getItem('gptFilePath')
      if (!savedPath) {
        console.log('[KnowledgeBase] 没有保存的知识库路径')
        return
      }

      console.log('[KnowledgeBase] 尝试加载已保存的路径:', savedPath)
      console.log('[KnowledgeBase] 当前系统平台:', process.platform)

      // ✅ 检查是否是跨平台路径问题
      const isWindowsPath = savedPath.includes(':\\') || savedPath.startsWith('\\\\')
      const isMacPath = savedPath.startsWith('/')
      const currentPlatform = process.platform

      console.log('[KnowledgeBase] 路径分析:', {
        isWindowsPath,
        isMacPath,
        currentPlatform
      })

      // 如果是 Windows 路径但运行在 macOS/Linux 上，或者反之，清除路径
      if ((isWindowsPath && currentPlatform !== 'win32') ||
          (isMacPath && currentPlatform === 'win32')) {
        console.warn('[KnowledgeBase] ✗ 检测到跨平台路径问题:')
        console.warn(`  - 路径格式: ${isWindowsPath ? 'Windows' : 'Unix'}`)
        console.warn(`  - 当前系统: ${currentPlatform}`)
        console.warn('[KnowledgeBase] 已清除无效路径，请重新选择知识库文件')
        localStorage.removeItem('gptFilePath')
        localStorage.removeItem('gptFileName')
        this.filePath = ''
        this.isLoaded = false
        return
      }

      // 检查文件是否存在
      if (fs.existsSync(savedPath)) {
        this.filePath = savedPath
        // 自动加载知识库
        this.loadFromFile(savedPath)
        console.log('[KnowledgeBase] ✓ 成功加载已保存的知识库')
      } else {
        // 文件不存在，清除保存的路径
        console.warn('[KnowledgeBase] ✗ 文件不存在:', savedPath)
        console.warn('[KnowledgeBase] 已清除无效路径，请重新选择知识库文件')
        localStorage.removeItem('gptFilePath')
        localStorage.removeItem('gptFileName')
        this.filePath = ''
        this.isLoaded = false
      }
    } catch (error) {
      console.error('[KnowledgeBase] 加载已保存路径失败:', error)
      console.error('[KnowledgeBase] 错误堆栈:', error.stack)
      // 清除可能损坏的数据
      localStorage.removeItem('gptFilePath')
      localStorage.removeItem('gptFileName')
      this.filePath = ''
      this.isLoaded = false
    }
  }

  /**
   * 选择Excel文件
   * @returns {Promise<String>} 文件路径
   */
  async selectExcelFile() {
    try {
      console.log('[KnowledgeBase] 打开文件选择对话框...')
      console.log('[KnowledgeBase] 当前系统平台:', process.platform)

      // ✅ Windows 和 macOS 兼容的文件过滤器配置
      const dialogOptions = {
        properties: ['openFile'],
        filters: [
          { name: 'Excel Files', extensions: ['xlsx', 'xls'] },
          { name: 'All Files', extensions: ['*'] }
        ],
        title: 'Select Knowledge Base Excel File'
      }

      console.log('[KnowledgeBase] 对话框配置:', JSON.stringify(dialogOptions, null, 2))

      const result = await ipcRenderer.invoke('dialog:openFile', dialogOptions)

      console.log('[KnowledgeBase] 文件选择结果:', JSON.stringify(result, null, 2))

      // 检查错误
      if (result.error) {
        console.error('[KnowledgeBase] 对话框返回错误:', result.error)
        throw new Error(result.error)
      }

      // 检查是否取消
      if (result.canceled) {
        console.log('[KnowledgeBase] 用户取消了文件选择')
        return null
      }

      // 检查是否有文件路径
      if (!result.filePaths || result.filePaths.length === 0) {
        console.warn('[KnowledgeBase] 对话框没有返回文件路径')
        console.warn('[KnowledgeBase] 完整结果对象:', result)
        return null
      }

      const filePath = result.filePaths[0]
      console.log('[KnowledgeBase] 选择的文件路径:', filePath)
      console.log('[KnowledgeBase] 路径类型:', typeof filePath)
      console.log('[KnowledgeBase] 路径长度:', filePath.length)

      // ✅ 检查文件路径格式（Windows/macOS）
      if (process.platform === 'win32') {
        // Windows 路径格式: C:\Users\...
        console.log('[KnowledgeBase] 检测到 Windows 系统，路径格式检查...')
        if (!filePath.includes(':\\') && !filePath.startsWith('\\\\')) {
          console.error('[KnowledgeBase] Windows 路径格式不正确:', filePath)
          throw new Error('Invalid Windows path format')
        }
      } else {
        // macOS/Linux 路径格式: /Users/...
        console.log('[KnowledgeBase] 检测到 Unix-like 系统，路径格式检查...')
        if (!filePath.startsWith('/')) {
          console.error('[KnowledgeBase] Unix 路径格式不正确:', filePath)
          throw new Error('Invalid Unix path format')
        }
      }

      // 立即加载文件
      console.log('[KnowledgeBase] 开始加载选择的文件...')
      await this.loadFromFile(filePath)
      console.log('[KnowledgeBase] 文件加载完成')

      return filePath
    } catch (error) {
      console.error('[KnowledgeBase] 选择文件失败:', error)
      console.error('[KnowledgeBase] 错误堆栈:', error.stack)
      throw error
    }
  }

  /**
   * 从Excel文件加载知识库
   * @param {String} filePath - 文件路径
   */
  async loadFromFile(filePath) {
    try {
      console.log('[KnowledgeBase] 正在加载文件:', filePath)

      // 检查文件是否存在
      if (!fs.existsSync(filePath)) {
        const errorMsg = `文件不存在: ${filePath}`
        console.error('[KnowledgeBase]', errorMsg)

        // 如果是旧的跨平台路径，清除它
        const savedPath = localStorage.getItem('gptFilePath')
        if (savedPath === filePath) {
          console.warn('[KnowledgeBase] 清除无效的保存路径')
          localStorage.removeItem('gptFilePath')
          localStorage.removeItem('gptFileName')
        }

        throw new Error('文件不存在，可能是跨平台路径问题。请重新选择知识库文件。')
      }

      // 检查文件大小（不超过500KB）
      const stats = fs.statSync(filePath)
      if (stats.size > 500 * 1024) {
        throw new Error('文件大小不能超过500KB')
      }

      console.log('[KnowledgeBase] 文件大小:', (stats.size / 1024).toFixed(2), 'KB')

      // ✅ Windows 兼容：先读取文件为 Buffer，再用 xlsx 解析
      // 这样可以避免 xlsx 库在 Windows 上处理中文路径的问题
      console.log('[KnowledgeBase] 开始读取文件内容...')
      let fileBuffer
      try {
        fileBuffer = fs.readFileSync(filePath)
        console.log('[KnowledgeBase] ✓ 文件内容读取成功，大小:', fileBuffer.length, 'bytes')
      } catch (readError) {
        console.error('[KnowledgeBase] ✗ 读取文件失败:', readError.message)

        // 提供详细的错误提示
        if (readError.code === 'EBUSY') {
          throw new Error('文件被占用，请关闭 Excel 或其他正在使用该文件的程序后重试')
        } else if (readError.code === 'EACCES' || readError.code === 'EPERM') {
          throw new Error('没有读取文件的权限，请检查文件权限设置')
        } else {
          throw new Error(`读取文件失败: ${readError.message}`)
        }
      }

      // 使用 xlsx 解析 Buffer
      console.log('[KnowledgeBase] 开始解析 Excel 文件...')
      const xlsx = require('xlsx')
      let workbook
      try {
        workbook = xlsx.read(fileBuffer, { type: 'buffer' })
        console.log('[KnowledgeBase] ✓ Excel 解析成功，工作表数量:', workbook.SheetNames.length)
      } catch (parseError) {
        console.error('[KnowledgeBase] ✗ 解析 Excel 失败:', parseError.message)
        throw new Error('Excel 文件格式错误，请确保是有效的 .xls 或 .xlsx 文件')
      }

      if (!workbook.SheetNames || workbook.SheetNames.length === 0) {
        throw new Error('Excel 文件中没有工作表')
      }

      const sheetName = workbook.SheetNames[0]
      console.log('[KnowledgeBase] 读取工作表:', sheetName)
      const worksheet = workbook.Sheets[sheetName]

      if (!worksheet) {
        throw new Error(`工作表 "${sheetName}" 不存在`)
      }

      // 转换为JSON
      console.log('[KnowledgeBase] 转换数据为 JSON...')
      const data = xlsx.utils.sheet_to_json(worksheet, { header: 1 })
      console.log('[KnowledgeBase] ✓ 数据转换完成，总行数:', data.length)

      if (data.length < 2) {
        throw new Error('Excel文件格式不正确，至少需要标题行和数据行')
      }

      // 解析数据（跳过标题行）
      this.knowledgeData = []
      for (let i = 1; i < data.length; i++) {
        const row = data[i]
        if (row && row.length >= 3) {
          this.knowledgeData.push({
            id: row[0] || '',           // 商品链接号
            name: row[1] || '',         // 商品名称
            detail: row[2] || ''        // 商品详情
          })
        }
      }

      this.filePath = filePath
      this.isLoaded = true

      // 保存路径到localStorage
      localStorage.setItem('gptFilePath', filePath)
      localStorage.setItem('gptFileName', path.basename(filePath))

      console.log(`[KnowledgeBase] ✓ 成功加载知识库，共 ${this.knowledgeData.length} 条记录`)

      return {
        success: true,
        count: this.knowledgeData.length
      }
    } catch (error) {
      console.error('[KnowledgeBase] ✗ 加载文件失败:', error.message)
      this.isLoaded = false
      this.filePath = ''
      this.knowledgeData = []
      throw error
    }
  }

  /**
   * 检索知识库
   * @param {String} keyword - 关键词
   * @returns {Array} 匹配的知识库条目
   */
  search(keyword) {
    if (!this.isLoaded || !keyword) {
      return []
    }

    const results = []
    const lowerKeyword = keyword.toLowerCase()

    for (const item of this.knowledgeData) {
      // 在商品名称和详情中搜索
      const nameMatch = item.name && item.name.toLowerCase().includes(lowerKeyword)
      const detailMatch = item.detail && item.detail.toLowerCase().includes(lowerKeyword)

      if (nameMatch || detailMatch) {
        results.push({
          ...item,
          matchInName: nameMatch,
          matchInDetail: detailMatch
        })
      }
    }

    return results
  }

  /**
   * 根据商品ID获取详情
   * @param {String} id - 商品ID
   */
  getById(id) {
    if (!this.isLoaded || !id) {
      return null
    }

    return this.knowledgeData.find(item => item.id === id)
  }

  /**
   * 随机获取一条知识库记录
   */
  getRandomItem() {
    if (!this.isLoaded || this.knowledgeData.length === 0) {
      return null
    }

    const randomIndex = Math.floor(Math.random() * this.knowledgeData.length)
    return this.knowledgeData[randomIndex]
  }

  /**
   * 按顺序获取知识库记录
   * @param {Number} index - 索引
   */
  getByIndex(index) {
    if (!this.isLoaded || index < 0 || index >= this.knowledgeData.length) {
      return null
    }

    return this.knowledgeData[index]
  }

  /**
   * 获取所有知识库数据
   */
  getAll() {
    return this.knowledgeData
  }

  /**
   * 获取知识库数量
   */
  getCount() {
    return this.knowledgeData.length
  }

  /**
   * 清空知识库
   */
  clear() {
    this.knowledgeData = []
    this.filePath = ''
    this.isLoaded = false
    localStorage.removeItem('gptFilePath')
    console.log('[KnowledgeBase] 知识库已清空')
  }

  /**
   * 将知识库数据格式化为文本（用于GPT提示词）
   * @param {Array|Object} items - 知识库条目
   */
  formatForPrompt(items) {
    if (!items) {
      return ''
    }

    // 如果是单个对象，转为数组
    const itemsArray = Array.isArray(items) ? items : [items]

    if (itemsArray.length === 0) {
      return ''
    }

    let text = '参考信息：\n'
    itemsArray.forEach((item, index) => {
      text += `${index + 1}. 商品名称：${item.name}\n`
      text += `   详情：${item.detail}\n`
    })

    return text
  }

  /**
   * 导出知识库为Excel模板
   */
  static async exportTemplate(savePath) {
    try {
      const xlsx = require('xlsx')
      const fs = require('fs')

      // 创建示例数据
      const data = [
        ['商品链接号', '商品名称', '商品详情'],
        ['P001', '复古高腰连衣裙', '材质：优质雪纺，轻盈透气\n设计亮点：V领设计、高腰剪裁、碎花图案\n适用场景：约会、聚会、日常穿搭\n痛点：担心连衣裙显胖\n解决方案：高腰设计+ A字版型，遮肉显瘦，适合各种身材\n差异化卖点：设计独特，材质优质\n促销信息：原价399元，直播间专属价259元，前50名送同款腰带\n下单引导：库存有限，喜欢的姐妹赶紧下单'],
        ['P002', '示例商品2', '这里填写商品详细信息'],
        ['P003', '示例商品3', '这里填写商品详细信息']
      ]

      // 创建工作簿
      const worksheet = xlsx.utils.aoa_to_sheet(data)
      const workbook = xlsx.utils.book_new()
      xlsx.utils.book_append_sheet(workbook, worksheet, '知识库')

      // ✅ 使用 fs 直接写入文件，避免二次弹窗
      const buffer = xlsx.write(workbook, { type: 'buffer', bookType: 'xlsx' })
      fs.writeFileSync(savePath, buffer)

      console.log('[KnowledgeBase] Excel模板已导出:', savePath)

      return {
        success: true,
        path: savePath
      }
    } catch (error) {
      console.error('[KnowledgeBase] 导出模板失败:', error)
      return {
        success: false,
        error: error.message
      }
    }
  }

  /**
   * 检查知识库是否已加载
   */
  isKnowledgeLoaded() {
    return this.isLoaded
  }

  /**
   * 获取当前文件路径
   */
  getFilePath() {
    return this.filePath
  }

  /**
   * 获取文件名
   */
  getFileName() {
    if (!this.filePath) {
      return ''
    }
    return path.basename(this.filePath)
  }
}

// 创建单例
const knowledgeBase = new KnowledgeBase()

export default knowledgeBase
