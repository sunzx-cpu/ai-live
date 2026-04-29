# TTS 音频流对接文档（App端）

## 📋 概述

本文档面向 **App 端（uniapp）开发人员**，说明如何接收并播放商户端推送的 TTS 音频流。

**商户端和后端已100%完成**，App 端只需要按照本文档实现接收和播放即可。

---

## 🎯 功能说明

### 系统架构

```
商户端（Electron）→ 后端（WebSocket）→ App端（uniapp）
    [推送音频流]        [转发]           [接收并播放]
```

### 音频流特点

- ✅ **实时推送**：延迟 < 1秒（vs 文件上传 3-5秒）
- ✅ **低带宽**：平均 120-180 Kbps
- ✅ **无需存储**：流式传输，不占用服务器空间
- ✅ **完全同步**：商户端播放什么，App端就播放什么

---

## 📡 WebSocket 连接

### 连接地址

```
ws://{服务器地址}/ai-live-api/websocket/liveInfo/{userId}
```

**参数说明**：
- `{服务器地址}`：后端服务器地址和端口（如 `192.168.1.100:8080`）
- `{userId}`：当前登录用户的 ID

### 连接示例（uniapp）

```javascript
// 创建 WebSocket 连接
const userId = uni.getStorageSync('userId')
const serverUrl = 'ws://192.168.1.100:8080'
const wsUrl = `${serverUrl}/ai-live-api/websocket/liveInfo/${userId}`

const socketTask = uni.connectSocket({
  url: wsUrl,
  success: () => {
    console.log('WebSocket 连接成功')
  },
  fail: (err) => {
    console.error('WebSocket 连接失败:', err)
  }
})

// 监听连接打开
socketTask.onOpen(() => {
  console.log('✅ WebSocket 已连接')
})

// 监听消息
socketTask.onMessage((res) => {
  const message = JSON.parse(res.data)
  handleMessage(message)
})

// 监听错误
socketTask.onError((err) => {
  console.error('❌ WebSocket 错误:', err)
})

// 监听关闭
socketTask.onClose(() => {
  console.log('🔌 WebSocket 已断开')
})
```

---

## 📨 接收的消息类型

### 1. 音频流开始

**消息类型**：`audio_stream_start`

**示例**：
```json
{
  "type": "audio_stream_start",
  "text": "大家好，欢迎来到我的直播间",
  "audioConfig": {
    "codec": "pcm",
    "sampleRate": 24000,
    "channels": 1,
    "bitsPerSample": 16
  },
  "streamId": "stream_1731838123456",
  "timestamp": 1731838123456
}
```

**字段说明**：
- `text`：TTS 文本内容
- `audioConfig`：音频格式配置
  - `codec`：编码格式（`pcm` 未压缩）
  - `sampleRate`：采样率（24000 Hz）
  - `channels`：声道数（1 = 单声道）
  - `bitsPerSample`：位深度（16 bit）
- `streamId`：流 ID（唯一标识）
- `timestamp`：时间戳

**处理逻辑**：

```javascript
function handleAudioStreamStart(message) {
  console.log('🎬 音频流开始:', message.text)
  
  // 初始化音频播放器
  const audioConfig = message.audioConfig
  initAudioPlayer(audioConfig)
  
  // 保存流信息
  currentStream = {
    streamId: message.streamId,
    text: message.text,
    audioConfig: audioConfig,
    chunks: []  // 用于存储音频数据块
  }
}
```

---

### 2. 音频数据块

**消息类型**：`audio_stream_data`

**示例**：
```json
{
  "type": "audio_stream_data",
  "streamId": "stream_1731838123456",
  "sequence": 0,
  "data": "base64_encoded_pcm_data",
  "size": 960,
  "timestamp": 1731838123476
}
```

**字段说明**：
- `streamId`：流 ID（对应 start 消息）
- `sequence`：序号（从 0 开始递增）
- `data`：Base64 编码的 PCM 音频数据
- `size`：数据块大小（字节）
- `timestamp`：时间戳

**数据块特点**：
- 每个数据块 = 960 bytes = 20ms 音频
- 发送频率 = 50 块/秒
- Base64 编码传输

**处理逻辑**：

```javascript
function handleAudioStreamData(message) {
  if (message.streamId !== currentStream.streamId) {
    console.warn('⚠️ 流 ID 不匹配，忽略数据块')
    return
  }
  
  // 解码 Base64
  const pcmData = base64ToArrayBuffer(message.data)
  
  // 添加到缓冲区
  currentStream.chunks.push(pcmData)
  
  // 如果缓冲区足够，开始播放
  if (currentStream.chunks.length >= 5) {
    playAudioChunks()
  }
}

// Base64 解码
function base64ToArrayBuffer(base64) {
  const binaryString = atob(base64)
  const len = binaryString.length
  const bytes = new Uint8Array(len)
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  return bytes.buffer
}
```

---

### 3. 音频流结束

**消息类型**：`audio_stream_end`

**示例**：
```json
{
  "type": "audio_stream_end",
  "streamId": "stream_1731838123456",
  "totalChunks": 312,
  "totalSize": 299520,
  "duration": 15.5,
  "timestamp": 1731838138956
}
```

**字段说明**：
- `streamId`：流 ID
- `totalChunks`：总数据块数量
- `totalSize`：总数据大小（字节）
- `duration`：音频时长（秒）
- `timestamp`：时间戳

**处理逻辑**：

```javascript
function handleAudioStreamEnd(message) {
  console.log('🏁 音频流结束:', message.streamId)
  console.log('   时长:', message.duration, '秒')
  console.log('   数据块:', message.totalChunks)
  
  // 播放完剩余的音频数据
  playRemainingChunks()
  
  // 清理流信息
  currentStream = null
}
```

---

## 🎵 音频播放实现

### 方案1：使用 uni.createInnerAudioContext（推荐）

```javascript
let audioContext = null
let audioBuffer = []

// 初始化音频播放器
function initAudioPlayer(audioConfig) {
  audioContext = uni.createInnerAudioContext()
  audioBuffer = []
  
  audioContext.onPlay(() => {
    console.log('▶ 开始播放')
  })
  
  audioContext.onEnded(() => {
    console.log('■ 播放结束')
    // 继续播放下一段
    if (audioBuffer.length > 0) {
      playNextChunk()
    }
  })
  
  audioContext.onError((err) => {
    console.error('❌ 播放错误:', err)
  })
}

// 播放音频数据块
function playAudioChunks() {
  if (audioContext.paused && audioBuffer.length > 0) {
    const chunk = audioBuffer.shift()
    
    // 将 PCM 转换为 WAV（添加 WAV 头部）
    const wavData = pcmToWav(chunk, {
      sampleRate: 24000,
      channels: 1,
      bitsPerSample: 16
    })
    
    // 转换为 Base64
    const wavBase64 = arrayBufferToBase64(wavData)
    const dataUri = `data:audio/wav;base64,${wavBase64}`
    
    audioContext.src = dataUri
    audioContext.play()
  }
}

// PCM 转 WAV
function pcmToWav(pcmData, config) {
  const { sampleRate, channels, bitsPerSample } = config
  const dataLength = pcmData.byteLength
  
  // 创建 WAV 头部（44 bytes）
  const buffer = new ArrayBuffer(44 + dataLength)
  const view = new DataView(buffer)
  
  // RIFF 头
  writeString(view, 0, 'RIFF')
  view.setUint32(4, 36 + dataLength, true)
  writeString(view, 8, 'WAVE')
  
  // fmt 子块
  writeString(view, 12, 'fmt ')
  view.setUint32(16, 16, true)  // fmt 块大小
  view.setUint16(20, 1, true)   // 音频格式（PCM）
  view.setUint16(22, channels, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, sampleRate * channels * bitsPerSample / 8, true)  // 字节率
  view.setUint16(32, channels * bitsPerSample / 8, true)  // 块对齐
  view.setUint16(34, bitsPerSample, true)
  
  // data 子块
  writeString(view, 36, 'data')
  view.setUint32(40, dataLength, true)
  
  // 复制 PCM 数据
  const pcmView = new Uint8Array(pcmData)
  const wavView = new Uint8Array(buffer)
  wavView.set(pcmView, 44)
  
  return buffer
}

function writeString(view, offset, string) {
  for (let i = 0; i < string.length; i++) {
    view.setUint8(offset + i, string.charCodeAt(i))
  }
}

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}
```

---

## 📊 完整示例代码

### main.js（完整实现）

```javascript
export default {
  data() {
    return {
      socketTask: null,
      currentStream: null,
      audioContext: null,
      audioBuffer: []
    }
  },
  
  onLoad() {
    this.connectWebSocket()
  },
  
  methods: {
    // 连接 WebSocket
    connectWebSocket() {
      const userId = uni.getStorageSync('userId')
      const serverUrl = 'ws://192.168.1.100:8080'
      const wsUrl = `${serverUrl}/ai-live-api/websocket/liveInfo/${userId}`
      
      this.socketTask = uni.connectSocket({
        url: wsUrl,
        success: () => {
          console.log('WebSocket 连接中...')
        }
      })
      
      this.socketTask.onOpen(() => {
        console.log('✅ WebSocket 已连接')
        uni.showToast({ title: '已连接', icon: 'success' })
      })
      
      this.socketTask.onMessage((res) => {
        const message = JSON.parse(res.data)
        this.handleMessage(message)
      })
      
      this.socketTask.onError((err) => {
        console.error('❌ WebSocket 错误:', err)
        uni.showToast({ title: '连接错误', icon: 'error' })
      })
      
      this.socketTask.onClose(() => {
        console.log('🔌 WebSocket 已断开')
        // 尝试重连
        setTimeout(() => {
          this.connectWebSocket()
        }, 3000)
      })
    },
    
    // 处理消息
    handleMessage(message) {
      const type = message.type
      
      switch(type) {
        case 'audio_stream_start':
          this.handleAudioStreamStart(message)
          break
        case 'audio_stream_data':
          this.handleAudioStreamData(message)
          break
        case 'audio_stream_end':
          this.handleAudioStreamEnd(message)
          break
        default:
          console.log('📨 其他消息:', type)
          break
      }
    },
    
    // 处理音频流开始
    handleAudioStreamStart(message) {
      console.log('🎬 音频流开始:', message.text)
      
      this.currentStream = {
        streamId: message.streamId,
        text: message.text,
        audioConfig: message.audioConfig,
        chunks: []
      }
      
      this.initAudioPlayer(message.audioConfig)
    },
    
    // 处理音频数据块
    handleAudioStreamData(message) {
      if (!this.currentStream || message.streamId !== this.currentStream.streamId) {
        return
      }
      
      // 解码 Base64
      const pcmData = this.base64ToArrayBuffer(message.data)
      
      // 添加到缓冲区
      this.audioBuffer.push(pcmData)
      
      // 如果缓冲区足够且未播放，开始播放
      if (this.audioBuffer.length >= 5 && !this.isPlaying) {
        this.playNextChunk()
      }
    },
    
    // 处理音频流结束
    handleAudioStreamEnd(message) {
      console.log('🏁 音频流结束')
      console.log('   时长:', message.duration, '秒')
      console.log('   数据块:', message.totalChunks)
      
      // 标记流结束
      if (this.currentStream) {
        this.currentStream.ended = true
      }
    },
    
    // 初始化音频播放器
    initAudioPlayer(audioConfig) {
      if (this.audioContext) {
        this.audioContext.destroy()
      }
      
      this.audioContext = uni.createInnerAudioContext()
      this.audioBuffer = []
      this.isPlaying = false
      
      this.audioContext.onPlay(() => {
        console.log('▶ 开始播放')
        this.isPlaying = true
      })
      
      this.audioContext.onEnded(() => {
        console.log('■ 播放结束')
        this.isPlaying = false
        
        // 继续播放下一段
        if (this.audioBuffer.length > 0) {
          this.playNextChunk()
        } else if (this.currentStream && this.currentStream.ended) {
          console.log('✅ 整个音频流播放完成')
          this.currentStream = null
        }
      })
      
      this.audioContext.onError((err) => {
        console.error('❌ 播放错误:', err)
        this.isPlaying = false
      })
    },
    
    // 播放下一个数据块
    playNextChunk() {
      if (this.audioBuffer.length === 0) {
        return
      }
      
      const chunk = this.audioBuffer.shift()
      
      // PCM 转 WAV
      const wavData = this.pcmToWav(chunk, {
        sampleRate: 24000,
        channels: 1,
        bitsPerSample: 16
      })
      
      // 转换为 Data URI
      const wavBase64 = this.arrayBufferToBase64(wavData)
      const dataUri = `data:audio/wav;base64,${wavBase64}`
      
      this.audioContext.src = dataUri
      this.audioContext.play()
    },
    
    // Base64 解码
    base64ToArrayBuffer(base64) {
      const binaryString = atob(base64)
      const len = binaryString.length
      const bytes = new Uint8Array(len)
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryString.charCodeAt(i)
      }
      return bytes.buffer
    },
    
    // PCM 转 WAV
    pcmToWav(pcmData, config) {
      // ... (参考上面的实现)
    },
    
    // ArrayBuffer 转 Base64
    arrayBufferToBase64(buffer) {
      // ... (参考上面的实现)
    }
  },
  
  onUnload() {
    // 清理资源
    if (this.socketTask) {
      this.socketTask.close()
    }
    if (this.audioContext) {
      this.audioContext.destroy()
    }
  }
}
```

---

## ⚠️ 注意事项

### 1. 音频格式

- **接收格式**：PCM（未压缩）
- **播放格式**：需要转换为 WAV（添加头部）
- **采样率**：24000 Hz
- **声道**：单声道（mono）
- **位深度**：16 bit

### 2. 缓冲策略

建议缓冲 5-10 个数据块（100-200ms）后再开始播放，避免卡顿。

```javascript
// 缓冲阈值
const BUFFER_THRESHOLD = 5

if (audioBuffer.length >= BUFFER_THRESHOLD && !isPlaying) {
  playNextChunk()
}
```

### 3. 错误处理

- **连接断开**：自动重连（延迟 3 秒）
- **数据丢失**：检查 sequence 序号是否连续
- **播放失败**：跳过错误的数据块，继续播放

### 4. 性能优化

- 使用 `ArrayBuffer` 而不是字符串操作
- 及时清理已播放的数据块
- 避免内存泄漏

---

## 🧪 测试方法

### 1. 商户端准备

商户端（Electron）已完成开发并测试通过，可以直接使用。

### 2. 连接测试

```javascript
// 测试连接是否成功
socketTask.onOpen(() => {
  console.log('✅ 连接成功')
  uni.showToast({ title: '已连接', icon: 'success' })
})
```

### 3. 接收测试

在商户端点击"语音开播"后，App 端应该能收到：
- `audio_stream_start` 消息
- 多个 `audio_stream_data` 消息（每 20ms 一个）
- `audio_stream_end` 消息

### 4. 播放测试

验证音频是否能正常播放，是否有卡顿或延迟。

---

## 📚 参考资料

### 技术规格

| 参数 | 值 |
|------|------|
| 总延迟 | < 1秒 |
| 首字节延迟 | ~ 100ms |
| 数据块大小 | 960 bytes |
| 数据块频率 | 50 块/秒 |
| 平均带宽 | 120-180 Kbps |

### 消息类型总览

| 消息类型 | 说明 | 处理优先级 |
|---------|------|-----------|
| `audio_stream_start` | 音频流开始 | 高 |
| `audio_stream_data` | 音频数据块 | 高 |
| `audio_stream_end` | 音频流结束 | 中 |

---

## ✅ 开发清单

- [ ] 实现 WebSocket 连接
- [ ] 处理 `audio_stream_start` 消息
- [ ] 处理 `audio_stream_data` 消息
- [ ] 处理 `audio_stream_end` 消息
- [ ] 实现 PCM 转 WAV
- [ ] 实现音频播放器
- [ ] 实现缓冲策略
- [ ] 实现错误处理和重连
- [ ] 进行端到端测试

---

**创建时间**: 2025-11-17  
**状态**: 商户端和后端已完成，App 端待开发  
**联系方式**: 如有问题，请联系后端开发团队
