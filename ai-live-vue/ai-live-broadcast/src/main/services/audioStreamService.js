const fs = require('fs')
const path = require('path')
const WebSocket = require('ws')  // 需要安装: npm install ws
const log = require('electron-log')
const ffmpeg = require('fluent-ffmpeg')
const os = require('os')

/**
 * 音频流推送服务（简化版 - 使用 PCM）
 *
 * 说明：
 * - 使用 PCM 原始音频数据（无压缩）
 * - 未来可升级为 Opus 编码以减少带宽
 * - 边读边推，实现实时推流
 * - 在 main 进程中管理独立的 WebSocket 连接
 */
class AudioStreamService {
  constructor() {
    this.isStreaming = false
    this.currentStreamId = null
    this.webSocket = null
    this.merchantId = null
    this.userId = null
    this.apiHost = null
    this.ipcMain = null
    this.isConnected = false
    this.reconnectTimer = null
  }

  /**
   * 初始化音频流服务
   * @param {Object} ipcMain - Electron IPC Main对象
   */
  init(ipcMain) {
    this.ipcMain = ipcMain
    log.info('[AudioStream] 服务已初始化')

    // 注册 IPC 处理器
    this.registerIpcHandlers()
  }

  /**
   * 注册 IPC 处理器
   */
  registerIpcHandlers() {
    // 连接 WebSocket
    this.ipcMain.handle('audio-stream:connect', async (event, arg) => {
      const { apiHost, merchantId, userId } = arg
      return await this.connectWebSocket(apiHost, merchantId, userId)
    })

    // 断开 WebSocket
    this.ipcMain.handle('audio-stream:disconnect', async (event) => {
      this.disconnectWebSocket()
      return { success: true }
    })

    // 开始推流
    this.ipcMain.handle('audio-stream:start', async (event, arg) => {
      const { text, audioFilePath } = arg
      return await this.startStreamFromFile(audioFilePath, text)
    })

    // 停止推流
    this.ipcMain.handle('audio-stream:stop', async (event) => {
      this.stopStream()
      return { success: true }
    })

    log.info('[AudioStream] IPC 处理器已注册')
  }

  /**
   * 连接到 WebSocket 服务器
   * @param {String} apiHost - API 服务器地址（如 http://192.168.1.100:8080）
   * @param {Number} merchantId - 商户 ID
   * @param {Number} userId - 用户 ID
   */
  async connectWebSocket(apiHost, merchantId, userId) {
    try {
      // 保存配置
      this.apiHost = apiHost
      this.merchantId = merchantId
      this.userId = userId

      // 构建 WebSocket URL
      const wsUrl = apiHost.replace('http://', 'ws://').replace('https://', 'wss://')
      const websocketUrl = `${wsUrl}/ai-live-api/websocket/liveInfo/${userId}`

      log.info(`[AudioStream] 正在连接 WebSocket: ${websocketUrl}`)

      // 创建 WebSocket 连接
      this.webSocket = new WebSocket(websocketUrl)

      // 监听连接打开
      this.webSocket.on('open', () => {
        this.isConnected = true
        log.info('[AudioStream] ✓ WebSocket 连接成功')
      })

      // 监听消息
      this.webSocket.on('message', (data) => {
        log.info('[AudioStream] 收到消息:', data.toString())
      })

      // 监听错误
      this.webSocket.on('error', (err) => {
        log.error('[AudioStream] WebSocket 错误:', err.message)
        this.isConnected = false
      })

      // 监听关闭
      this.webSocket.on('close', () => {
        log.info('[AudioStream] WebSocket 连接已关闭')
        this.isConnected = false

        // 尝试重连（可选）
        // this.scheduleReconnect()
      })

      // 等待连接建立
      await this.waitForConnection()

      return { success: true }

    } catch (err) {
      log.error('[AudioStream] 连接失败:', err)
      return { success: false, error: err.message }
    }
  }

  /**
   * 等待 WebSocket 连接建立
   */
  waitForConnection() {
    return new Promise((resolve, reject) => {
      const timeout = setTimeout(() => {
        reject(new Error('连接超时'))
      }, 10000) // 10秒超时

      const checkConnection = setInterval(() => {
        if (this.isConnected) {
          clearTimeout(timeout)
          clearInterval(checkConnection)
          resolve()
        }
      }, 100)
    })
  }

  /**
   * 断开 WebSocket 连接
   */
  disconnectWebSocket() {
    if (this.webSocket) {
      log.info('[AudioStream] 断开 WebSocket 连接')
      this.webSocket.close()
      this.webSocket = null
      this.isConnected = false
    }
  }

  /**
   * 将 MP3 文件转换为 WAV 格式
   * @param {String} mp3FilePath - MP3 文件路径
   * @returns {Promise<String>} WAV 文件路径
   */
  async convertMp3ToWav(mp3FilePath) {
    return new Promise((resolve, reject) => {
      // 生成临时 WAV 文件路径
      const tempDir = os.tmpdir()
      const tempWavPath = path.join(tempDir, `audio_${Date.now()}.wav`)

      console.log(`[AudioStream] 正在转换 MP3 -> WAV: ${path.basename(mp3FilePath)}`)

      ffmpeg(mp3FilePath)
        .outputOptions([
          '-acodec pcm_s16le',  // PCM 16-bit
          '-ar 24000',          // 采样率 24kHz
          '-ac 1'               // 单声道
        ])
        .output(tempWavPath)
        .on('end', () => {
          console.log(`[AudioStream] ✓ 转换完成: ${path.basename(tempWavPath)}`)
          resolve(tempWavPath)
        })
        .on('error', (err) => {
          console.error('[AudioStream] ✗ 转换失败:', err.message)
          reject(err)
        })
        .run()
    })
  }

  /**
   * 从音频文件开始推流（边读边推）
   * @param {String} audioFilePath - 音频文件路径（WAV/MP3格式）
   * @param {String} text - TTS 文本内容
   */
  async startStreamFromFile(audioFilePath, text) {
    if (this.isStreaming) {
      console.warn('[AudioStream] 已有推流正在进行')
      return { success: false, error: '推流正在进行中' }
    }

    if (!this.webSocket || this.webSocket.readyState !== 1) {
      console.error('[AudioStream] WebSocket 未连接')
      return { success: false, error: 'WebSocket 未连接' }
    }

    if (!fs.existsSync(audioFilePath)) {
      console.error('[AudioStream] 音频文件不存在:', audioFilePath)
      return { success: false, error: '音频文件不存在' }
    }

    let wavFilePath = audioFilePath
    let tempWavFile = null

    try {
      // 检查文件格式
      const fileExt = path.extname(audioFilePath).toLowerCase()

      // 如果是 MP3，先转换为 WAV
      if (fileExt === '.mp3') {
        console.log(`[AudioStream] 检测到 MP3 格式，正在转换...`)
        tempWavFile = await this.convertMp3ToWav(audioFilePath)
        wavFilePath = tempWavFile
      } else if (fileExt !== '.wav') {
        console.warn(`[AudioStream] ⏭ 不支持的格式: ${fileExt}`)
        return { success: false, error: `不支持的格式: ${fileExt}` }
      }

      // 生成流ID
      this.currentStreamId = `stream_${Date.now()}`
      this.isStreaming = true

      console.log(`[AudioStream] ✓ 开始推流: streamId=${this.currentStreamId}, file=${path.basename(audioFilePath)}`)

      // 读取 WAV 文件头，获取音频参数
      const audioConfig = await this.parseWavHeader(wavFilePath)

      // 发送开始消息
      this.sendStartMessage(text, audioConfig)

      // 流式读取并推送音频数据
      const stats = await this.streamAudioData(wavFilePath, audioConfig)

      // 发送结束消息
      this.sendEndMessage(stats)

      this.isStreaming = false
      this.currentStreamId = null

      console.log(`[AudioStream] ✓ 推流完成: 总时长=${stats.duration}s, 总数据=${(stats.totalSize / 1024).toFixed(2)}KB`)

      return { success: true, stats }

    } catch (err) {
      console.error('[AudioStream] 推流失败:', err)
      this.stopStream()
      return { success: false, error: err.message }
    } finally {
      // 清理临时 WAV 文件
      if (tempWavFile && fs.existsSync(tempWavFile)) {
        try {
          fs.unlinkSync(tempWavFile)
          console.log(`[AudioStream] 🗑 已删除临时文件: ${path.basename(tempWavFile)}`)
        } catch (err) {
          console.warn(`[AudioStream] 删除临时文件失败: ${err.message}`)
        }
      }
    }
  }

  /**
   * 解析 WAV 文件头，获取音频参数
   * @param {String} filePath - WAV 文件路径
   * @returns {Object} 音频配置
   */
  async parseWavHeader(filePath) {
    return new Promise((resolve, reject) => {
      const stream = fs.createReadStream(filePath, { start: 0, end: 43 })
      const chunks = []

      stream.on('data', chunk => chunks.push(chunk))
      stream.on('end', () => {
        const header = Buffer.concat(chunks)

        // 解析 WAV 头（简化版）
        const sampleRate = header.readUInt32LE(24)    // 采样率
        const channels = header.readUInt16LE(22)       // 声道数
        const bitsPerSample = header.readUInt16LE(34)  // 位深度

        console.log(`[AudioStream] WAV格式: ${sampleRate}Hz, ${channels}声道, ${bitsPerSample}bit`)

        resolve({
          codec: 'pcm',
          sampleRate,
          channels,
          bitsPerSample
        })
      })
      stream.on('error', reject)
    })
  }

  /**
   * 流式读取并推送音频数据
   * @param {String} filePath - 音频文件路径
   * @param {Object} audioConfig - 音频配置
   * @returns {Object} 统计信息
   */
  async streamAudioData(filePath, audioConfig) {
    return new Promise((resolve, reject) => {
      // 计算每块的大小（20ms 的音频数据）
      // 公式: 采样率 × 每采样字节数 × 声道数 × 时长(秒)
      const bytesPerSample = audioConfig.bitsPerSample / 8
      const chunkDuration = 0.02 // 20ms
      const chunkSize = Math.floor(audioConfig.sampleRate * bytesPerSample * audioConfig.channels * chunkDuration)

      console.log(`[AudioStream] 分块大小: ${chunkSize} bytes (${chunkDuration * 1000}ms)`)

      // 创建读取流，跳过 WAV 头（44字节）
      const stream = fs.createReadStream(filePath, {
        start: 44, // 跳过 WAV 头
        highWaterMark: chunkSize
      })

      let sequence = 0
      let totalSize = 0
      let buffer = Buffer.alloc(0)

      stream.on('data', (chunk) => {
        // 将新数据追加到缓冲区
        buffer = Buffer.concat([buffer, chunk])

        // 当缓冲区有完整的块时，推送数据
        while (buffer.length >= chunkSize) {
          const dataChunk = buffer.slice(0, chunkSize)
          buffer = buffer.slice(chunkSize)

          // 推送这一块数据
          this.sendDataMessage(dataChunk, sequence++)
          totalSize += dataChunk.length

          // 控制推送速率（模拟实时播放）
          // 每推送一块，等待 20ms（模拟实时生成）
          // 注意：这会阻塞，仅用于演示，生产环境应使用更好的方案
        }
      })

      stream.on('end', () => {
        // 推送剩余的数据（如果有）
        if (buffer.length > 0) {
          this.sendDataMessage(buffer, sequence++)
          totalSize += buffer.length
        }

        // 计算总时长
        const duration = totalSize / (audioConfig.sampleRate * bytesPerSample * audioConfig.channels)

        resolve({
          totalChunks: sequence,
          totalSize,
          duration: duration.toFixed(2)
        })
      })

      stream.on('error', (err) => {
        reject(err)
      })
    })
  }

  /**
   * 发送开始推流消息
   * @param {String} text - TTS 文本
   * @param {Object} audioConfig - 音频配置
   */
  sendStartMessage(text, audioConfig) {
    const message = {
      type: 'audio_stream_start',
      merchantId: this.merchantId,
      text: text || '',
      audioConfig: {
        codec: audioConfig.codec,
        sampleRate: audioConfig.sampleRate,
        channels: audioConfig.channels,
        bitsPerSample: audioConfig.bitsPerSample
      },
      streamId: this.currentStreamId,
      timestamp: Date.now()
    }

    this.sendMessage(message)
    console.log(`[AudioStream] → 发送开始消息: streamId=${this.currentStreamId}`)
  }

  /**
   * 发送音频数据消息
   * @param {Buffer} data - 音频数据块
   * @param {Number} sequence - 序号
   */
  sendDataMessage(data, sequence) {
    const message = {
      type: 'audio_stream_data',
      streamId: this.currentStreamId,
      sequence: sequence,
      data: data.toString('base64'), // 转 Base64
      size: data.length,
      timestamp: Date.now()
    }

    this.sendMessage(message)

    // 每隔 100 个块打印一次日志（避免日志过多）
    if (sequence % 100 === 0) {
      console.log(`[AudioStream] → 发送数据块: sequence=${sequence}, size=${data.length}`)
    }
  }

  /**
   * 发送结束推流消息
   * @param {Object} stats - 统计信息
   */
  sendEndMessage(stats) {
    const message = {
      type: 'audio_stream_end',
      streamId: this.currentStreamId,
      totalChunks: stats.totalChunks,
      totalSize: stats.totalSize,
      duration: parseFloat(stats.duration),
      timestamp: Date.now()
    }

    this.sendMessage(message)
    console.log(`[AudioStream] → 发送结束消息: streamId=${this.currentStreamId}`)
  }

  /**
   * 发送消息到 WebSocket
   * @param {Object} message - 消息对象
   */
  sendMessage(message) {
    if (!this.webSocket || this.webSocket.readyState !== 1) {
      console.error('[AudioStream] WebSocket 未连接，无法发送消息')
      return false
    }

    try {
      const data = JSON.stringify(message)
      this.webSocket.send(data)
      return true
    } catch (err) {
      console.error('[AudioStream] 发送消息失败:', err)
      return false
    }
  }

  /**
   * 停止推流
   */
  stopStream() {
    if (this.isStreaming) {
      console.log('[AudioStream] 停止推流')

      // 发送结束消息
      this.sendEndMessage({
        totalChunks: 0,
        totalSize: 0,
        duration: 0
      })

      this.isStreaming = false
      this.currentStreamId = null
    }
  }

  /**
   * 检查是否正在推流
   * @returns {Boolean}
   */
  isActive() {
    return this.isStreaming
  }
}

// 导出单例
module.exports = new AudioStreamService()
