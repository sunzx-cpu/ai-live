<!-- 语音直播进度 -->
<template>
  <el-container class="audio-broadcast-container">
    <!-- 顶部控制栏 -->
    <el-header height="80px" class="header">
      <el-row type="flex" align="middle" justify="space-between">
        <el-col :span="16">
          <div class="broadcast-controls">
            <el-button-group>
              <el-button type="primary" icon="el-icon-video-play" @click="startBroadcast">开始直播</el-button>
              <el-button type="warning" icon="el-icon-video-pause" @click="pauseBroadcast">暂停</el-button>
              <el-button type="info" icon="el-icon-refresh" @click="resetBroadcast">重置</el-button>
            </el-button-group>
            <span class="broadcast-status" :class="{ 'is-live': isLive }">
              {{ isLive ? '直播中' : '未开始' }}
            </span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="broadcast-info">
            <el-tag type="success">总时长: {{ formatDuration(totalDuration) }}</el-tag>
            <el-tag type="info">已播放: {{ formatDuration(currentDuration) }}</el-tag>
          </div>
        </el-col>
      </el-row>
    </el-header>

    <!-- 主体内容区 -->
    <el-container class="main-container">
      <!-- 左侧语音列表 -->
      <el-aside width="300px" class="voice-list">
        <div class="list-header">
          <h3>语音列表</h3>
          <el-button type="text" icon="el-icon-plus" @click="addVoiceItem">添加语音</el-button>
        </div>
        <el-scrollbar>
          <div class="voice-items">
            <div v-for="(item, index) in voiceList"
                 :key="index"
                 class="voice-item"
                 :class="{ 'is-playing': currentIndex === index, 'is-completed': item.completed }">
              <div class="voice-item-header">
                <span class="voice-title">{{ item.title }}</span>
                <span class="voice-duration">{{ formatDuration(item.duration) }}</span>
              </div>
              <div class="voice-progress">
                <el-progress :percentage="item.progress" :status="item.completed ? 'success' : undefined"></el-progress>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </el-aside>

      <!-- 右侧内容展示区 -->
      <el-main class="content-area">
        <div class="current-voice">
          <h3>当前播放</h3>
          <div class="voice-player">
            <!-- <audio ref="audioPlayer" :src="currentVoice?.audioUrl" @timeupdate="onTimeUpdate"></audio> -->
            <div class="player-controls">
              <el-slider v-model="currentProgress" :max="100" @change="onProgressChange"></el-slider>
              <div class="control-buttons">
                <el-button icon="el-icon-video-play" circle @click="playCurrent"></el-button>
                <el-button icon="el-icon-video-pause" circle @click="pauseCurrent"></el-button>
                <el-button icon="el-icon-refresh" circle @click="restartCurrent"></el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="voice-content">
          <h3>语音内容</h3>
          <div class="content-text">
            <p v-if="currentVoice && currentVoice.text">{{ currentVoice.text }}</p>
            <p v-else class="no-content">暂无语音内容</p>
          </div>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'AudioBroadcast',
  data() {
    return {
      isLive: false,
      currentIndex: -1,
      currentProgress: 0,
      totalDuration: 0,
      currentDuration: 0,
      voiceList: [
        {
          title: '开场白',
          duration: 120,
          progress: 0,
          completed: false,
          audioUrl: '',
          text: '欢迎来到直播间，今天我们将为大家带来精彩的内容...'
        },
        {
          title: '产品介绍',
          duration: 180,
          progress: 0,
          completed: false,
          audioUrl: '',
          text: '这款产品采用了最新的技术，具有以下特点...'
        }
      ]
    }
  },
  computed: {
    currentVoice() {
      return this.currentIndex >= 0 ? this.voiceList[this.currentIndex] : null
    }
  },
  created() {
    // 注册回调
    if (window.audioBridge) {
      window.audioBridge.registerCallback('onAudioData', function(data) {
        console.log('Received audio data', data);
      });

      window.audioBridge.registerCallback('onPlaybackStatus', function(data) {
        console.log('Playback status updated', data);
      });

      window.audioBridge.registerCallback('onAudioCompleted', function(data) {
        console.log('Audio completed', data);
      });
    } else {
      console.warn('audioBridge is not available');
    }
  },
  methods: {
    // 格式化时间
    formatDuration(seconds) {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.floor(seconds % 60)
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
    },

    // 开始直播
    async startBroadcast() {
      this.isLive = true
      this.playNext()
    },

    // 暂停直播
    pauseBroadcast() {
      this.isLive = false
      this.pauseCurrent()
    },

    // 重置直播
    resetBroadcast() {
      this.isLive = false
      this.currentIndex = -1
      this.currentProgress = 0
      this.voiceList.forEach(item => {
        item.progress = 0
        item.completed = false
      })
    },

    // 播放下一章
    playNext() {
      if (this.currentIndex < this.voiceList.length - 1) {
        this.currentIndex++
        this.playCurrent()
      } else {
        this.$message.error('没有更多语音了')
      }
    },

    // 播放当前语音
    playCurrent() {
      window.audioBridge.play();
    },

    // 暂停当前语音
    pauseCurrent() {
      window.audioBridge.pause();
    },

    // 重置当前语音
    restartCurrent() {
      const audio = this.$refs.audioPlayer
      if (audio) {
        audio.currentTime = 0
        audio.play()
      }
    },

    // 更新时间
    onTimeUpdate(event) {
      const audio = event.target
      const progress = (audio.currentTime / audio.duration) * 100
      this.currentProgress = progress
      this.currentDuration = audio.currentTime

      if (this.currentVoice) {
        this.currentVoice.progress = progress
        if (progress >= 100) {
          this.currentVoice.completed = true
          this.playNext()
        }
      }
    },

    // 更新进度
    onProgressChange(value) {
      const audio = this.$refs.audioPlayer
      if (audio) {
        audio.currentTime = (value / 100) * audio.duration
      }
    },

    // 添加语音
    addVoiceItem() {
      this.voiceList.push({
        title: '新语音',
        duration: 0,
        progress: 0,
        completed: false,
        audioUrl: '',
        text: ''
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.audio-broadcast-container {
  height: 100%;
  background-color: #f5f7fa;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.broadcast-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.broadcast-status {
  padding: 5px 15px;
  border-radius: 15px;
  background-color: #909399;
  color: #fff;

  &.is-live {
    background-color: #67c23a;
  }
}

.broadcast-info {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.main-container {
  height: calc(100% - 80px);
}

.voice-list {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    margin: 0;
  }
}

.voice-items {
  .voice-item {
    padding: 10px;
    border-radius: 4px;
    margin-bottom: 10px;
    background-color: #f5f7fa;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background-color: #ecf5ff;
    }

    &.is-playing {
      background-color: #ecf5ff;
      border-left: 3px solid #409eff;
    }

    &.is-completed {
      background-color: #f0f9eb;
    }
  }

  .voice-item-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 5px;
  }

  .voice-title {
    font-weight: 500;
  }

  .voice-duration {
    color: #909399;
    font-size: 12px;
  }
}

.content-area {
  padding: 20px;
}

.current-voice {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;

  h3 {
    margin-top: 0;
    margin-bottom: 20px;
  }
}

.player-controls {
  .control-buttons {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 10px;
  }
}

.voice-content {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;

  h3 {
    margin-top: 0;
    margin-bottom: 20px;
  }

  .content-text {
    min-height: 200px;

    .no-content {
      color: #909399;
      text-align: center;
      padding: 40px 0;
    }
  }
}
</style>
