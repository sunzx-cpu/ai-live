/**
 * 语音服务管理模块
 * 负责启动、停止、监控 VSA/F5TTS/COSYVOICE 等语音服务
 */

import { spawn, exec } from 'child_process'
import path from 'path'
import { app } from 'electron'
import axios from 'axios'
import fs from 'fs'

class VoiceServiceManager {
  constructor() {
    // 服务进程实例
    this.vsaProcess = null
    this.f5ttsProcess = null
    this.cosyVoiceProcess = null

    // 服务状态
    this.vsaStatus = 'stopped' // stopped, starting, running, error
    this.f5ttsStatus = 'stopped'
    this.cosyVoiceStatus = 'stopped'

    // 服务配置
    this.vsaPort = 23456
    this.f5ttsPort = 5010
    this.cosyVoicePort = 9880

    // 应用根目录
    this.appPath = this.getAppPath()
  }

  /**
   * 获取应用根目录
   */
  getAppPath() {
    if (app.isPackaged) {
      // 生产环境：打包后的应用
      return path.dirname(process.execPath)
    } else {
      // 开发环境：项目根目录
      return path.join(__dirname, '..', '..', '..')
    }
  }

  /**
   * 启动 VSA 服务
   */
  async startVSAService(options = {}) {
    console.log('[VoiceService] 启动 VSA 服务...')

    if (this.vsaStatus === 'running') {
      console.log('[VoiceService] VSA 服务已在运行')
      return { success: true, message: 'VSA 服务已在运行' }
    }

    this.vsaStatus = 'starting'

    try {
      // VSA 服务路径
      const vsaPath = path.join(this.appPath, 'extra', 'vsa')
      const vsaExe = path.join(vsaPath, 'vsa.exe')
      const appPy = path.join(vsaPath, 'app.py')
      const platform = process.platform

      console.log('[VoiceService] 操作系统:', platform)
      console.log('[VoiceService] VSA 路径:', vsaPath)

      // 检查 VSA 服务是否已安装（通过检查 app.py）
      if (!fs.existsSync(appPy)) {
        console.error('[VoiceService] VSA 服务未安装')
        this.vsaStatus = 'error'
        return {
          success: false,
          error: 'VSA 服务未安装，请将 vsa.rar 解压到 extra/vsa/ 文件夹'
        }
      }

      // macOS/Linux 开发环境：直接启动 Python
      if (platform !== 'win32') {
        console.warn('[VoiceService] ⚠️  macOS/Linux 环境：使用 Python 直接启动')
        const pythonExe = 'python3'

        this.vsaProcess = spawn(pythonExe, [appPy], {
          cwd: vsaPath,
          detached: false,
          stdio: options.enableSuggestion ? 'pipe' : 'ignore'
        })
      } else {
        // Windows 环境：优先使用 vsa.exe，如果不存在则用 python
        if (fs.existsSync(vsaExe)) {
          console.log('[VoiceService] Windows 环境：使用 vsa.exe 启动')

          const showWindow = options.enableSuggestion || false

          this.vsaProcess = spawn(vsaExe, [], {
            cwd: vsaPath,
            detached: false,
            windowsHide: !showWindow,
            stdio: showWindow ? 'pipe' : 'ignore'
          })
        } else {
          // vsa.exe 不存在，尝试使用 Python
          console.warn('[VoiceService] vsa.exe 不存在，尝试使用 Python 启动')
          const pythonExe = path.join(vsaPath, 'py310', 'python.exe')

          if (!fs.existsSync(pythonExe)) {
            console.error('[VoiceService] Python 环境不存在')
            this.vsaStatus = 'error'
            return {
              success: false,
              error: 'VSA 服务不完整，请确保 vsa.rar 已完整解压'
            }
          }

          this.vsaProcess = spawn(pythonExe, [appPy], {
            cwd: vsaPath,
            detached: false,
            windowsHide: !options.enableSuggestion,
            stdio: options.enableSuggestion ? 'pipe' : 'ignore'
          })
        }
      }

      // 监听进程输出（调试模式）
      if (options.enableSuggestion && this.vsaProcess.stdout) {
        this.vsaProcess.stdout.on('data', (data) => {
          console.log(`[VSA stdout]: ${data}`)
        })
      }

      if (options.enableSuggestion && this.vsaProcess.stderr) {
        this.vsaProcess.stderr.on('data', (data) => {
          console.error(`[VSA stderr]: ${data}`)
        })
      }

      // 监听进程退出
      this.vsaProcess.on('exit', (code, signal) => {
        console.log(`[VoiceService] VSA 进程退出，code: ${code}, signal: ${signal}`)
        this.vsaStatus = 'stopped'
        this.vsaProcess = null
      })

      // 监听进程错误
      this.vsaProcess.on('error', (error) => {
        console.error(`[VoiceService] VSA 进程错误:`, error)
        this.vsaStatus = 'error'

        // macOS/Linux 特殊错误提示
        if (process.platform !== 'win32') {
          console.error('[VoiceService] ❌ macOS/Linux 环境启动失败')
          console.error('[VoiceService] 💡 提示：')
          console.error('[VoiceService]    1. 请确保已安装 Python 依赖（flask, torch 等）')
          console.error('[VoiceService]    2. 或在 AI 服务器设置中选择"云端AI服务器"')
          console.error('[VoiceService]    3. 完整测试请在 Windows 环境下进行')
        }
      })

      // 等待服务启动（最多等待 30 秒）
      const isReady = await this.waitForService('vsa', 30)

      if (isReady) {
        this.vsaStatus = 'running'
        console.log('[VoiceService] VSA 服务启动成功')
        return { success: true, message: 'VSA 服务启动成功', port: this.vsaPort }
      } else {
        this.vsaStatus = 'error'
        console.error('[VoiceService] VSA 服务启动超时')
        return { success: false, error: 'VSA 服务启动超时' }
      }

    } catch (error) {
      this.vsaStatus = 'error'
      console.error('[VoiceService] 启动 VSA 服务失败:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 停止 VSA 服务
   */
  async stopVSAService() {
    console.log('[VoiceService] 停止 VSA 服务...')

    try {
      // 方法1：直接 kill 进程
      if (this.vsaProcess) {
        this.vsaProcess.kill('SIGTERM')
        this.vsaProcess = null
      }

      // 方法2：执行 kill 批处理（作为备用）
      const killBat = path.join(this.appPath, 'extra', 'kill23456.bat')
      exec(`"${killBat}"`, (error) => {
        if (error) {
          console.error('[VoiceService] 执行 kill23456.bat 失败:', error)
        }
      })

      this.vsaStatus = 'stopped'
      console.log('[VoiceService] VSA 服务已停止')

      return { success: true, message: 'VSA 服务已停止' }
    } catch (error) {
      console.error('[VoiceService] 停止 VSA 服务失败:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 检查 VSA 服务状态
   */
  async checkVSAServiceStatus() {
    try {
      const url = `http://127.0.0.1:${this.vsaPort}`
      const response = await axios.get(url, { timeout: 3000 })
      return {
        success: true,
        running: true,
        status: this.vsaStatus,
        port: this.vsaPort
      }
    } catch (error) {
      return {
        success: true,
        running: false,
        status: this.vsaStatus,
        port: this.vsaPort
      }
    }
  }

  /**
   * 等待服务启动
   * @param {String} serviceType - 服务类型：vsa, f5tts, cosyvoice
   * @param {Number} maxWaitSeconds - 最大等待秒数
   */
  async waitForService(serviceType, maxWaitSeconds = 30) {
    const port = this.getServicePort(serviceType)
    const startTime = Date.now()
    const maxWaitTime = maxWaitSeconds * 1000

    console.log(`[VoiceService] 等待 ${serviceType} 服务启动，端口: ${port}`)

    while (Date.now() - startTime < maxWaitTime) {
      try {
        const url = `http://127.0.0.1:${port}`
        await axios.get(url, { timeout: 2000 })
        console.log(`[VoiceService] ${serviceType} 服务已就绪`)
        return true
      } catch (error) {
        // 服务还未就绪，继续等待
        await this.sleep(1000) // 每秒检查一次
      }
    }

    console.error(`[VoiceService] ${serviceType} 服务启动超时`)
    return false
  }

  /**
   * 获取服务端口
   */
  getServicePort(serviceType) {
    switch (serviceType) {
      case 'vsa':
        return this.vsaPort
      case 'f5tts':
        return this.f5ttsPort
      case 'cosyvoice':
        return this.cosyVoicePort
      default:
        return null
    }
  }

  /**
   * 睡眠函数
   */
  sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  /**
   * 获取所有服务状态
   */
  getAllServiceStatus() {
    return {
      vsa: {
        status: this.vsaStatus,
        port: this.vsaPort,
        running: this.vsaStatus === 'running'
      },
      f5tts: {
        status: this.f5ttsStatus,
        port: this.f5ttsPort,
        running: this.f5ttsStatus === 'running'
      },
      cosyvoice: {
        status: this.cosyVoiceStatus,
        port: this.cosyVoicePort,
        running: this.cosyVoiceStatus === 'running'
      }
    }
  }

  /**
   * 检查 VSA 服务是否已安装
   */
  checkVSAInstalled() {
    try {
      const vsaPath = path.join(this.appPath, 'extra', 'vsa')
      const vsaExe = path.join(vsaPath, 'vsa.exe')
      const appPy = path.join(vsaPath, 'app.py')

      console.log('[VoiceService] 检查 VSA 安装状态')
      console.log('[VoiceService] VSA 路径:', vsaPath)
      console.log('[VoiceService] vsa.exe 存在:', fs.existsSync(vsaExe))
      console.log('[VoiceService] app.py 存在:', fs.existsSync(appPy))

      // 检查 app.py 是否存在（必需）
      if (!fs.existsSync(appPy)) {
        return {
          installed: false,
          error: 'VSA 服务未安装',
          message: 'VSA 服务未安装，请将 vsa.rar 解压到 extra/vsa/ 文件夹',
          vsaPath: vsaPath
        }
      }

      // Windows 环境检查 vsa.exe
      if (process.platform === 'win32' && !fs.existsSync(vsaExe)) {
        const pythonExe = path.join(vsaPath, 'py310', 'python.exe')
        if (!fs.existsSync(pythonExe)) {
          return {
            installed: false,
            error: 'VSA 服务不完整',
            message: 'VSA 服务不完整，缺少 vsa.exe 或 Python 环境',
            vsaPath: vsaPath
          }
        }
      }

      console.log('[VoiceService] VSA 服务已安装')
      return {
        installed: true,
        vsaPath: vsaPath
      }
    } catch (error) {
      console.error('[VoiceService] 检查 VSA 安装状态失败:', error)
      return {
        installed: false,
        error: '检查失败',
        message: error.message
      }
    }
  }

  /**
   * 停止所有服务
   */
  async stopAllServices() {
    console.log('[VoiceService] 停止所有服务...')

    const results = []

    if (this.vsaProcess) {
      const result = await this.stopVSAService()
      results.push({ service: 'vsa', ...result })
    }

    // TODO: F5TTS 和 COSYVOICE 停止逻辑

    return { success: true, results }
  }

  /**
   * VSA 服务诊断
   * 用于排查启动超时问题
   */
  async diagnoseVSAService() {
    console.log('[VoiceService] ========================================')
    console.log('[VoiceService]        VSA 服务诊断工具')
    console.log('[VoiceService] ========================================')

    const results = []
    const addResult = (category, status, message, detail = null) => {
      const result = { category, status, message, detail, timestamp: new Date().toISOString() }
      results.push(result)
      const icon = { success: '✓', warning: '⚠', error: '✗', info: 'ℹ' }[status] || '•'
      console.log(`[VoiceService] ${icon} [${category}] ${message}`)
      if (detail) console.log(`[VoiceService]   详情:`, detail)
    }

    // 1. 检查 VSA 文件完整性
    console.log('[VoiceService] \n=== 1. 检查 VSA 文件完整性 ===')

    const vsaPath = path.join(this.appPath, 'extra', 'vsa')
    const vsaExe = path.join(vsaPath, 'vsa.exe')
    const appPy = path.join(vsaPath, 'app.py')

    if (!fs.existsSync(vsaPath)) {
      addResult('文件检查', 'error', 'VSA 目录不存在', { path: vsaPath })
    } else {
      addResult('文件检查', 'success', 'VSA 目录存在', { path: vsaPath })
    }

    if (!fs.existsSync(appPy)) {
      addResult('文件检查', 'error', 'app.py 不存在', { path: appPy })
    } else {
      addResult('文件检查', 'success', 'app.py 存在')
    }

    if (process.platform === 'win32') {
      if (fs.existsSync(vsaExe)) {
        addResult('文件检查', 'success', 'vsa.exe 存在')
      } else {
        addResult('文件检查', 'warning', 'vsa.exe 不存在，将使用 Python 启动')
        const pythonExe = path.join(vsaPath, 'py310', 'python.exe')
        if (!fs.existsSync(pythonExe)) {
          addResult('文件检查', 'error', 'Python 环境不存在', { path: pythonExe })
        } else {
          addResult('文件检查', 'success', 'Python 环境存在')
        }
      }
    }

    // 检查模型目录
    const modelsPath = path.join(vsaPath, 'data', 'models')
    if (!fs.existsSync(modelsPath)) {
      addResult('文件检查', 'warning', '模型目录不存在', { path: modelsPath })
    } else {
      try {
        const models = fs.readdirSync(modelsPath, { withFileTypes: true })
          .filter(item => item.isDirectory())
        addResult('文件检查', 'info', `找到 ${models.length} 个模型文件夹`, {
          count: models.length,
          models: models.map(m => m.name)
        })
      } catch (error) {
        addResult('文件检查', 'error', '读取模型目录失败', { error: error.message })
      }
    }

    // 2. 检查端口占用
    console.log('[VoiceService] \n=== 2. 检查端口占用 ===')
    const portCheckResult = await new Promise((resolve) => {
      exec(`netstat -ano | findstr :${this.vsaPort}`, (error, stdout, stderr) => {
        if (stdout && stdout.trim()) {
          addResult('端口检查', 'warning', `端口 ${this.vsaPort} 已被占用`, {
            output: stdout.trim()
          })
          resolve({ occupied: true, output: stdout.trim() })
        } else {
          addResult('端口检查', 'success', `端口 ${this.vsaPort} 未被占用`)
          resolve({ occupied: false })
        }
      })
    })

    // 3. 检查 VSA 服务运行状态
    console.log('[VoiceService] \n=== 3. 检查 VSA 服务状态 ===')
    try {
      const url = `http://127.0.0.1:${this.vsaPort}`
      const response = await axios.get(url, { timeout: 3000 })
      addResult('服务检查', 'success', 'VSA 服务正在运行', {
        status: response.status
      })
    } catch (error) {
      if (error.code === 'ECONNREFUSED') {
        addResult('服务检查', 'info', 'VSA 服务未运行 (连接被拒绝)')
      } else if (error.code === 'ETIMEDOUT') {
        addResult('服务检查', 'warning', 'VSA 服务响应超时')
      } else {
        addResult('服务检查', 'info', 'VSA 服务未运行', { error: error.message })
      }
    }

    // 4. 尝试手动启动并监控
    console.log('[VoiceService] \n=== 4. 尝试启动 VSA 服务并监控 ===')

    if (this.vsaStatus !== 'stopped') {
      addResult('服务启动', 'warning', `当前服务状态: ${this.vsaStatus}，跳过启动测试`)
    } else {
      try {
        addResult('服务启动', 'info', '开始启动测试，将监控启动过程...')

        const startTime = Date.now()
        const startOptions = { enableSuggestion: true } // 启用日志输出

        // 启动服务（异步）
        const startPromise = this.startVSAService(startOptions)

        // 等待启动结果
        const startResult = await startPromise
        const endTime = Date.now()
        const duration = ((endTime - startTime) / 1000).toFixed(2)

        if (startResult.success) {
          addResult('服务启动', 'success', `VSA 服务启动成功，耗时 ${duration} 秒`)

          // 停止测试服务
          await this.stopVSAService()
        } else {
          addResult('服务启动', 'error', `VSA 服务启动失败，耗时 ${duration} 秒`, {
            error: startResult.error
          })
        }
      } catch (error) {
        addResult('服务启动', 'error', '启动测试异常', { error: error.message })
      }
    }

    // 5. 生成诊断报告
    const successCount = results.filter(r => r.status === 'success').length
    const warningCount = results.filter(r => r.status === 'warning').length
    const errorCount = results.filter(r => r.status === 'error').length

    console.log('[VoiceService] \n=== 诊断报告 ===')
    console.log(`[VoiceService] ✓ 成功: ${successCount}`)
    console.log(`[VoiceService] ⚠ 警告: ${warningCount}`)
    console.log(`[VoiceService] ✗ 错误: ${errorCount}`)

    const report = {
      timestamp: new Date().toISOString(),
      platform: process.platform,
      appPath: this.appPath,
      vsaPath: vsaPath,
      vsaStatus: this.vsaStatus,
      summary: {
        success: successCount,
        warning: warningCount,
        error: errorCount,
        total: results.length
      },
      results: results
    }

    // 保存诊断报告
    try {
      const reportPath = path.join(this.appPath, 'vsa-diagnostic-report.json')
      fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf-8')
      console.log(`[VoiceService] 诊断报告已保存: ${reportPath}`)
      report.reportPath = reportPath
    } catch (error) {
      console.error(`[VoiceService] 保存诊断报告失败:`, error)
    }

    console.log('[VoiceService] ========================================')

    return report
  }
}

// 创建单例
const voiceServiceManager = new VoiceServiceManager()

export default voiceServiceManager
