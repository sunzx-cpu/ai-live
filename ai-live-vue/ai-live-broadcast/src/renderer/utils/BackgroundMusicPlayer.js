/**
 * 背景音乐播放管理类
 * 基于 Howler.js 实现背景音乐播放、自动回避（ducking）
 */
import { Howl } from 'howler'

export class BackgroundMusicPlayer {
  constructor() {
    // 播放状态
    this.isPlaying = false           // 是否正在播放
    this.currentSound = null         // 当前播放的 Howl 实例
    this.currentMusicPath = null     // 当前播放的音乐路径

    // 音乐文件列表
    this.musicFiles = []             // 音乐文件路径列表
    this.musicDir = ''               // 音乐目录
    this.playedFiles = []            // 已播放的文件（避免重复）

    // 音量设置
    this.baseVolume = 1.0            // 基础音量 (0-1)
    this.currentVolume = 1.0         // 当前音量 (0-1)
    this.isDucking = false           // 是否正在回避（降低音量）
    this.duckVolume = 0.2            // 回避时的音量（基础音量的百分比）
    this.fadeDuration = 1000         // 淡入淡出时长（毫秒）

    // 播放设置
    this.autoAvoid = true            // 是否启用自动回避
    this.playInterval = 10           // 播放间隔（秒）
    this.intervalTimer = null        // 间隔定时器

    // 回调函数
    this.onMusicStart = null         // 音乐开始播放回调
    this.onMusicEnd = null           // 音乐播放结束回调
    this.onError = null              // 错误回调
  }

  /**
   * 设置音乐目录
   * @param {String} dir - 音乐目录路径
   * @param {Array} files - 音乐文件列表
   */
  setMusicDirectory(dir, files) {
    this.musicDir = dir
    this.musicFiles = files
    this.playedFiles = []
    console.log(`[BgMusic] 设置音乐目录: ${dir}`)
    console.log(`[BgMusic] 找到 ${files.length} 个音乐文件`)
  }

  /**
   * 设置音量
   * @param {Number} volume - 音量值 (0-100)
   */
  setVolume(volume) {
    this.baseVolume = volume / 100.0
    if (!this.isDucking && this.currentSound) {
      this.currentSound.volume(this.baseVolume)
      this.currentVolume = this.baseVolume
      console.log(`[BgMusic] 设置音量: ${this.baseVolume.toFixed(2)}`)
    }
  }

  /**
   * 设置自动回避参数
   * @param {Boolean} enabled - 是否启用
   * @param {Number} duckPercent - 回避音量百分比 (0-100)
   * @param {Number} fadeDuration - 淡入淡出时长（毫秒）
   */
  setAutoAvoid(enabled, duckPercent, fadeDuration) {
    this.autoAvoid = enabled
    this.duckVolume = duckPercent / 100.0
    this.fadeDuration = fadeDuration
    console.log(`[BgMusic] 自动回避: ${enabled ? '启用' : '禁用'}, 回避音量: ${this.duckVolume.toFixed(2)}, 淡入淡出: ${fadeDuration}ms`)
  }

  /**
   * 设置播放间隔
   * @param {Number} interval - 间隔时间（秒）
   */
  setPlayInterval(interval) {
    this.playInterval = interval
    console.log(`[BgMusic] 播放间隔: ${interval}秒`)
  }

  /**
   * 开始随机播放
   */
  startRandomPlay() {
    if (this.musicFiles.length === 0) {
      console.warn('[BgMusic] 音乐文件列表为空，无法播放')
      if (this.onError) {
        this.onError(new Error('没有可播放的音乐文件'))
      }
      return
    }

    console.log('[BgMusic] 开始随机播放背景音乐')
    this.playRandomMusic()
  }

  /**
   * 播放随机音乐
   */
  playRandomMusic() {
    // 如果所有文件都播放过了，重置已播放列表
    if (this.playedFiles.length >= this.musicFiles.length) {
      console.log('[BgMusic] 所有音乐已播放，重置播放列表')
      this.playedFiles = []
    }

    // 过滤出未播放的文件
    const unplayedFiles = this.musicFiles.filter(file => !this.playedFiles.includes(file))

    // 随机选择一个文件
    const randomIndex = Math.floor(Math.random() * unplayedFiles.length)
    const selectedFile = unplayedFiles[randomIndex]

    console.log(`[BgMusic] 随机选择音乐: ${selectedFile}`)
    this.playMusic(selectedFile)
  }

  /**
   * 播放指定音乐
   * @param {String} musicPath - 音乐文件路径
   */
  playMusic(musicPath) {
    // 停止当前播放的音乐
    if (this.currentSound) {
      this.currentSound.stop()
      this.currentSound.unload()
    }

    this.currentMusicPath = musicPath
    this.playedFiles.push(musicPath)

    console.log(`[BgMusic] ========== 开始播放背景音乐 ==========`)
    console.log(`[BgMusic] 文件: ${musicPath}`)
    console.log(`[BgMusic] 基础音量: ${this.baseVolume.toFixed(2)}`)

    // 创建 Howl 实例
    this.currentSound = new Howl({
      src: [musicPath],
      format: ['mp3', 'wav', 'ogg', 'm4a'],
      html5: true,
      loop: false,  // 不循环，播放完后自动播放下一首
      volume: this.isDucking ? this.baseVolume * this.duckVolume : this.baseVolume,
      onload: () => {
        const duration = this.currentSound.duration()
        console.log(`[BgMusic] ✓ 音乐加载完成，时长: ${duration.toFixed(2)}秒`)
      },
      onplay: () => {
        console.log(`[BgMusic] ▶ 开始播放背景音乐`)
        this.isPlaying = true
        this.currentVolume = this.currentSound.volume()

        if (this.onMusicStart) {
          this.onMusicStart(musicPath)
        }
      },
      onend: () => {
        console.log(`[BgMusic] ■ 背景音乐播放结束`)
        this.isPlaying = false

        if (this.onMusicEnd) {
          this.onMusicEnd(musicPath)
        }

        // 等待间隔后播放下一首
        if (this.playInterval > 0) {
          console.log(`[BgMusic] 等待 ${this.playInterval} 秒后播放下一首`)
          this.intervalTimer = setTimeout(() => {
            this.playRandomMusic()
          }, this.playInterval * 1000)
        } else {
          // 立即播放下一首
          this.playRandomMusic()
        }
      },
      onerror: (id, error) => {
        console.error(`[BgMusic] ✗ 播放失败:`, error)
        this.isPlaying = false

        if (this.onError) {
          this.onError(error)
        }

        // 尝试播放下一首
        setTimeout(() => {
          this.playRandomMusic()
        }, 1000)
      }
    })

    this.currentSound.play()
  }

  /**
   * 停止播放
   */
  stop() {
    // 清除间隔定时器
    if (this.intervalTimer) {
      clearTimeout(this.intervalTimer)
      this.intervalTimer = null
    }

    // 停止当前音乐
    if (this.currentSound) {
      this.currentSound.stop()
      this.currentSound.unload()
      this.currentSound = null
    }

    this.isPlaying = false
    this.isDucking = false
    this.currentMusicPath = null

    console.log('[BgMusic] 已停止播放背景音乐')
  }

  /**
   * 开始回避（主播说话时降低音量）
   */
  duck() {
    if (!this.autoAvoid || !this.currentSound || this.isDucking) {
      return
    }

    this.isDucking = true
    const targetVolume = this.baseVolume * this.duckVolume

    console.log(`[BgMusic] 🔇 开始回避，音量: ${this.currentVolume.toFixed(2)} → ${targetVolume.toFixed(2)}`)

    // 使用 Howler 的 fade 方法实现淡出
    this.currentSound.fade(this.currentVolume, targetVolume, this.fadeDuration)
    this.currentVolume = targetVolume
  }

  /**
   * 恢复音量（主播说话结束后）
   */
  unduck() {
    if (!this.autoAvoid || !this.currentSound || !this.isDucking) {
      return
    }

    this.isDucking = false

    console.log(`[BgMusic] 🔊 恢复音量，音量: ${this.currentVolume.toFixed(2)} → ${this.baseVolume.toFixed(2)}`)

    // 使用 Howler 的 fade 方法实现淡入
    this.currentSound.fade(this.currentVolume, this.baseVolume, this.fadeDuration)
    this.currentVolume = this.baseVolume
  }

  /**
   * 获取播放状态
   * @returns {Object} 播放状态信息
   */
  getStatus() {
    return {
      isPlaying: this.isPlaying,
      isDucking: this.isDucking,
      currentMusicPath: this.currentMusicPath,
      baseVolume: this.baseVolume,
      currentVolume: this.currentVolume,
      musicCount: this.musicFiles.length,
      playedCount: this.playedFiles.length
    }
  }
}
