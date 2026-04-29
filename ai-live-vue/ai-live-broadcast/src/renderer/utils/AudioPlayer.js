/**
 * 音频播放管理类
 * 基于 Howler.js 实现音频播放、队列管理
 */
import { Howl } from 'howler'

export class AudioPlayer {
  constructor() {
    // 播放队列
    this.playQueue = []           // 主播放队列
    this.hudongQueue = []         // 互动队列(优先级高)

    // 播放状态
    this.isPlaying = false        // 是否正在播放
    this.isPaused = false         // 是否暂停
    this.pendingStop = false      // 是否等待优雅停止
    this.pendingPause = false     // 是否等待优雅暂停
    this.currentSound = null      // 当前播放的 Howl 实例
    this.currentAudio = null      // 当前播放的音频对象

    // 句间效果的异步任务（用于停播时清理）
    this.currentEndSound = null   // 当前播放的句间结束音 Howl 实例
    this.pauseTimer = null        // 随机停顿的 setTimeout ID

    // 配置
    this.volume = 1.0             // 音量 (0-1)
    this.preloadCount = 5         // 预加载数量

    // 句间结束音和随机停顿配置
    this.endSoundEnabled = true   // 是否启用句间结束音
    this.randomPauseEnabled = true // 是否启用随机停顿
    this.pauseMax = 5             // 最大停顿时间（秒）
    this.endSoundFiles = []       // 句间结束音文件列表

    // 回调函数
    this.onPlayStart = null       // 开始播放回调
    this.onPlayEnd = null         // 播放结束回调
    this.onError = null           // 错误回调
    this.onQueueEmpty = null      // 队列为空回调
    this.onStopComplete = null    // 停止完成回调（优雅停播）
    this.onPauseComplete = null   // 暂停完成回调（优雅暂停）
  }

  /**
   * 添加音频到主播放队列
   * @param {Object} audioObj - 音频对象 { text, url, speaker, type }
   */
  addToQueue(audioObj) {
    this.playQueue.push(audioObj)
    console.log(`[AudioPlayer] 添加到主队列: ${audioObj.text?.substring(0, 20)}...`)
  }

  /**
   * 添加音频到互动队列(优先播放)
   * @param {Object} audioObj - 音频对象
   */
  addToHudongQueue(audioObj) {
    this.hudongQueue.push(audioObj)
    console.log(`[AudioPlayer] 添加到互动队列: ${audioObj.text?.substring(0, 20)}...`)

    // 如果当前没有在播放,立即播放
    if (!this.isPlaying) {
      this.playNext()
    }
  }

  /**
   * 开始播放
   */
  start() {
    if (this.playQueue.length === 0 && this.hudongQueue.length === 0) {
      console.warn('[AudioPlayer] 播放队列为空')
      return
    }

    if (!this.isPlaying) {
      this.playNext()
    }
  }

  /**
   * 暂停播放
   */
  pause() {
    if (this.currentSound && this.isPlaying) {
      this.currentSound.pause()
      this.isPaused = true
      console.log('[AudioPlayer] 已暂停播放')
    }
  }

  /**
   * 继续播放
   */
  resume() {
    if (this.currentSound && this.isPaused) {
      this.currentSound.play()
      this.isPaused = false
      console.log('[AudioPlayer] 继续播放')
    }
  }

  /**
   * 停止播放并清空队列（立即停止）
   */
  stop() {
    console.log('[AudioPlayer] 立即停止播放')

    // 取消优雅停止标志
    this.pendingStop = false
    this.pendingPause = false

    // 停止当前播放的主音频
    if (this.currentSound) {
      this.currentSound.stop()
      this.currentSound.unload()
      this.currentSound = null
    }

    // 停止句间结束音
    if (this.currentEndSound) {
      this.currentEndSound.stop()
      this.currentEndSound.unload()
      this.currentEndSound = null
      console.log('[AudioPlayer] 已停止句间结束音')
    }

    // 清除随机停顿的定时器
    if (this.pauseTimer) {
      clearTimeout(this.pauseTimer)
      this.pauseTimer = null
      console.log('[AudioPlayer] 已清除随机停顿定时器')
    }

    this.playQueue = []
    this.hudongQueue = []
    this.isPlaying = false
    this.isPaused = false
    this.currentAudio = null

    console.log('[AudioPlayer] 已停止播放并清空队列')
  }

  /**
   * 优雅停止播放（等待当前音频播放完成）
   */
  stopGracefully() {
    console.log('[AudioPlayer] 开始优雅停播...')

    // 清空队列，防止播放下一个
    this.playQueue = []
    this.hudongQueue = []

    // 停止句间结束音（不等待它播完）
    if (this.currentEndSound) {
      this.currentEndSound.stop()
      this.currentEndSound.unload()
      this.currentEndSound = null
      console.log('[AudioPlayer] 已停止句间结束音')
    }

    // 清除随机停顿的定时器
    if (this.pauseTimer) {
      clearTimeout(this.pauseTimer)
      this.pauseTimer = null
      console.log('[AudioPlayer] 已清除随机停顿定时器')
    }

    // 设置等待停止标志
    this.pendingStop = true

    // 如果当前没有播放，直接完成停止
    if (!this.isPlaying || !this.currentSound) {
      console.log('[AudioPlayer] 当前没有播放，直接完成停止')
      this.pendingStop = false
      this.isPlaying = false
      this.currentAudio = null

      if (this.onStopComplete) {
        this.onStopComplete()
      }
      return
    }

    console.log('[AudioPlayer] 等待当前音频播放完成后停止...')
  }

  /**
   * 暂停播放（延迟响应：等待当前音频播放完成）
   */
  pauseGracefully() {
    console.log('[AudioPlayer] 设置暂停标志（延迟响应）')

    // 只设置暂停标志，当前音频继续播放到结束
    this.isPaused = true

    // 如果当前没有音频在播放，立即完成暂停
    if (!this.isPlaying) {
      console.log('[AudioPlayer] 当前没有音频在播放，立即完成暂停')
      if (this.onPauseComplete) {
        this.onPauseComplete()
      }
    } else {
      console.log('[AudioPlayer] 当前音频将播放完成后暂停')
    }
  }

  /**
   * 带句间结束音和随机停顿的播放下一个
   */
  playNextWithEffects() {
    // 检查是否等待停止
    if (this.pendingStop) {
      console.log('[AudioPlayer] 检测到优雅停播标志，完成停止')
      this.isPlaying = false
      this.pendingStop = false
      this.currentAudio = null

      if (this.onStopComplete) {
        this.onStopComplete()
      }
      return
    }

    // 检查是否需要暂停（延迟响应）
    if (this.isPaused) {
      console.log('[AudioPlayer] 检测到暂停标志，完成暂停')
      this.isPlaying = false
      if (this.onPauseComplete) {
        this.onPauseComplete()
      }
      return
    }

    // 1. 先播放句间结束音（如果启用）
    if (this.endSoundEnabled && this.endSoundFiles.length > 0) {
      console.log('[AudioPlayer] 播放句间结束音')
      const randomIndex = Math.floor(Math.random() * this.endSoundFiles.length)
      const endSoundFile = this.endSoundFiles[randomIndex]
      console.log(`[AudioPlayer] 选择音效: ${endSoundFile}`)

      // 🎯 创建句间结束音的音频对象（用于推送）
      const endSoundObj = {
        text: '句间结束音',
        url: endSoundFile,
        speaker: 'system',
        type: 'end_sound',
        volume: this.volume * 0.3
      }

      // 创建音效播放器并保存实例（用于停播时清理）
      this.currentEndSound = new Howl({
        src: [endSoundFile],
        format: ['mp3', 'wav'],
        volume: this.volume * 0.3, // 音效音量降低
        onplay: () => {
          console.log('[AudioPlayer] ▶ 句间结束音开始播放')
          // 🎯 触发 onPlayStart 回调，让句间结束音也能被推送
          if (this.onPlayStart) {
            this.onPlayStart(endSoundObj)
          }
        },
        onend: () => {
          console.log('[AudioPlayer] 句间结束音播放完成')
          // 清除引用
          this.currentEndSound = null
          // 音效播放完成后，进行随机停顿
          this.applyRandomPauseAndPlayNext()
        },
        onerror: (error) => {
          console.error('[AudioPlayer] 句间结束音播放失败:', error)
          // 清除引用
          this.currentEndSound = null
          // 即使失败也继续
          this.applyRandomPauseAndPlayNext()
        }
      })

      this.currentEndSound.play()
    } else {
      // 未启用句间结束音，直接进行随机停顿
      this.applyRandomPauseAndPlayNext()
    }
  }

  /**
   * 应用随机停顿后播放下一个
   */
  applyRandomPauseAndPlayNext() {
    // 检查是否等待停止或暂停
    if (this.pendingStop) {
      console.log('[AudioPlayer] 检测到优雅停播标志，取消停顿')
      this.isPlaying = false
      this.pendingStop = false
      this.currentAudio = null

      if (this.onStopComplete) {
        this.onStopComplete()
      }
      return
    }

    if (this.isPaused) {
      console.log('[AudioPlayer] 检测到暂停标志，取消停顿')
      this.isPlaying = false
      if (this.onPauseComplete) {
        this.onPauseComplete()
      }
      return
    }

    // 2. 计算随机停顿时间
    if (this.randomPauseEnabled && this.pauseMax > 0) {
      const pauseTime = Math.random() * this.pauseMax
      console.log(`[AudioPlayer] 随机停顿: ${pauseTime.toFixed(2)}秒`)

      // 保存 timer ID（用于停播时清理）
      this.pauseTimer = setTimeout(() => {
        console.log('[AudioPlayer] 停顿结束，继续播放')
        this.pauseTimer = null  // 清除引用
        this.playNext()
      }, pauseTime * 1000)
    } else {
      // 未启用随机停顿，直接播放下一个
      this.playNext()
    }
  }

  /**
   * 播放下一个音频
   */
  playNext() {
    // 检查是否等待停止
    if (this.pendingStop) {
      console.log('[AudioPlayer] 检测到优雅停播标志，完成停止')
      this.isPlaying = false
      this.pendingStop = false
      this.currentAudio = null

      if (this.onStopComplete) {
        this.onStopComplete()
      }
      return
    }

    // 检查是否需要暂停（延迟响应）
    if (this.isPaused) {
      console.log('[AudioPlayer] 检测到暂停标志，完成暂停')
      this.isPlaying = false
      if (this.onPauseComplete) {
        this.onPauseComplete()
      }
      return
    }

    // 优先播放互动队列
    let audioObj = null
    if (this.hudongQueue.length > 0) {
      audioObj = this.hudongQueue.shift()
      console.log(`[AudioPlayer] 从互动队列取出音频`)
    } else if (this.playQueue.length > 0) {
      audioObj = this.playQueue.shift()
      console.log(`[AudioPlayer] 从主队列取出音频`)
    } else {
      // 队列为空
      this.isPlaying = false
      console.log('[AudioPlayer] 播放队列已空')

      if (this.onQueueEmpty) {
        this.onQueueEmpty()
      }
      return
    }

    this.playAudio(audioObj)
  }

  /**
   * 播放单个音频
   * @param {Object} audioObj - 音频对象
   */
  playAudio(audioObj) {
    if (!audioObj || !audioObj.url) {
      console.error('[AudioPlayer] 音频对象无效:', audioObj)
      this.playNext()
      return
    }

    // 🔧 关键修复：如果当前已有音频在播放，先停止它
    if (this.currentSound) {
      console.log('[AudioPlayer] 检测到已有音频在播放，先停止')
      this.currentSound.stop()
      this.currentSound.unload()
      this.currentSound = null
    }

    this.isPlaying = true
    this.currentAudio = audioObj

    console.log(`[AudioPlayer] ========== 开始播放音频 ==========`)
    console.log(`[AudioPlayer] 文本: ${audioObj.text?.substring(0, 50)}...`)
    console.log(`[AudioPlayer] URL: ${audioObj.url}`)
    console.log(`[AudioPlayer] 主播: ${audioObj.speaker}`)
    console.log(`[AudioPlayer] 类型: ${audioObj.type}`)
    console.log(`[AudioPlayer] 音量: ${audioObj.volume !== undefined ? audioObj.volume.toFixed(2) : this.volume}`)

    // 使用音频对象自带的音量，如果没有则使用默认音量
    const playVolume = audioObj.volume !== undefined ? audioObj.volume : this.volume

    // 创建 Howl 实例
    this.currentSound = new Howl({
      src: [audioObj.url],
      format: ['wav', 'mp3'],
      html5: true,         // 使用 HTML5 Audio 以支持流式播放
      volume: playVolume,  // 使用计算后的音量
      onload: () => {
        const duration = this.currentSound.duration()
        console.log(`[AudioPlayer] ✓ 音频加载完成`)
        console.log(`[AudioPlayer]    时长: ${duration.toFixed(2)}秒`)
        console.log(`[AudioPlayer]    状态: ready`)
      },
      onplay: () => {
        console.log(`[AudioPlayer] ▶ 开始播放音频`)
        if (this.onPlayStart) {
          this.onPlayStart(audioObj)
        }
      },
      onend: () => {
        console.log(`[AudioPlayer] ■ 音频播放结束`)
        this.isPlaying = false

        if (this.onPlayEnd) {
          this.onPlayEnd(audioObj)
        }

        // 检查是否需要暂停（延迟响应暂停）
        if (this.isPaused) {
          console.log('[AudioPlayer] 检测到暂停标志，完成暂停')
          // 清理资源
          if (this.currentSound) {
            this.currentSound.unload()
            this.currentSound = null
          }
          // 调用暂停完成回调
          if (this.onPauseComplete) {
            this.onPauseComplete()
          }
          return
        }

        // 播放句间结束音和随机停顿后，再播放下一个
        this.playNextWithEffects()
      },
      onloaderror: (id, error) => {
        console.error(`[AudioPlayer] ✗ 音频加载失败`)
        console.error(`[AudioPlayer]    Sound ID: ${id}`)
        console.error(`[AudioPlayer]    Error: ${error}`)
        console.error(`[AudioPlayer]    URL: ${audioObj.url}`)
        console.error(`[AudioPlayer]    可能原因: 1) VSA服务未运行 2) 网络问题 3) URL格式错误`)
        this.isPlaying = false

        if (this.onError) {
          this.onError(audioObj, `加载失败: ${error}`)
        }

        // 跳过错误音频,播放下一个
        this.playNext()
      },
      onplayerror: (id, error) => {
        console.error(`[AudioPlayer] ✗ 音频播放失败`)
        console.error(`[AudioPlayer]    Sound ID: ${id}`)
        console.error(`[AudioPlayer]    Error: ${error}`)
        console.error(`[AudioPlayer]    URL: ${audioObj.url}`)
        this.isPlaying = false

        if (this.onError) {
          this.onError(audioObj, `播放失败: ${error}`)
        }

        // 跳过错误音频,播放下一个
        this.playNext()
      }
    })

    // 开始播放
    console.log(`[AudioPlayer] 调用 Howl.play()...`)
    this.currentSound.play()
  }

  /**
   * 设置音量
   * @param {Number} volume - 音量 (0-1)
   */
  setVolume(volume) {
    this.volume = Math.max(0, Math.min(1, volume))
    if (this.currentSound) {
      this.currentSound.volume(this.volume)
    }
    console.log(`[AudioPlayer] 设置音量: ${this.volume}`)
  }

  /**
   * 设置句间结束音配置
   * @param {Boolean} enabled - 是否启用
   * @param {Array} files - 音效文件路径列表
   */
  setEndSoundConfig(enabled, files) {
    this.endSoundEnabled = enabled
    this.endSoundFiles = files || []
    console.log(`[AudioPlayer] 句间结束音: ${enabled ? '启用' : '禁用'}, 音效数量: ${this.endSoundFiles.length}`)
  }

  /**
   * 设置随机停顿配置
   * @param {Boolean} enabled - 是否启用
   * @param {Number} maxPause - 最大停顿时间（秒）
   */
  setRandomPauseConfig(enabled, maxPause) {
    this.randomPauseEnabled = enabled
    this.pauseMax = maxPause
    console.log(`[AudioPlayer] 随机停顿: ${enabled ? '启用' : '禁用'}, 最大停顿: ${maxPause}秒`)
  }

  /**
   * 获取当前队列长度
   */
  getQueueLength() {
    return {
      playQueue: this.playQueue.length,
      hudongQueue: this.hudongQueue.length,
      total: this.playQueue.length + this.hudongQueue.length
    }
  }

  /**
   * 获取当前播放状态
   */
  getStatus() {
    return {
      isPlaying: this.isPlaying,
      isPaused: this.isPaused,
      currentAudio: this.currentAudio,
      queueLength: this.getQueueLength()
    }
  }

  /**
   * 清空队列
   */
  clearQueue() {
    this.playQueue = []
    this.hudongQueue = []
    console.log('[AudioPlayer] 队列已清空')
  }
}

export default AudioPlayer
