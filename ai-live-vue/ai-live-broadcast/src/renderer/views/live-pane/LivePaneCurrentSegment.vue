<!-- 当前播放片段内容区 -->
<template>
  <div class="live-pane-current-segment">
    <el-card shadow="hover" class="current-segment-card">
      <div slot="header" class="clearfix">
        <span>当前正在播放的片段内容</span>
        <span v-if="currentSpeaker" class="speaker-info">主播: {{ currentSpeaker }}</span>
      </div>

      <!-- 完整文本显示区域 -->
      <div class="full-text-container">
        <div class="full-text-content" :class="{ 'placeholder-text': !isPlaying }">
          {{ isPlaying ? (fullText || '暂无播放内容') : 'AI主播正在说的话术' }}
        </div>
      </div>
    </el-card>
  </div>
</template>
<script>
export default {
  name: 'LivePaneCurrentSegment',
  data() {
    return {
      liveContext: null,
      fullText: '',              // 完整文本
      currentSpeaker: '',        // 当前主播
      isPlaying: false,          // 是否正在播放
    }
  },
  methods: {
    // 初始化
    init(liveContext) {
      this.liveContext = liveContext
    },

    /**
     * 更新当前播放的片段 (被 AudioPlayer 的 onPlayStart 回调调用)
     * @param {String} text - 话术文本（已处理过的文本）
     * @param {String} speaker - 主播名称
     */
    updateCurrentSegment(text, speaker) {
      // 直接显示文本（文本在加入队列前已经处理过了）
      this.fullText = text || ''
      this.currentSpeaker = speaker || ''
      this.isPlaying = true
    },

    /**
     * 清除当前播放内容（停播时调用）
     */
    clearCurrentSegment() {
      this.fullText = ''
      this.currentSpeaker = ''
      this.isPlaying = false
    }
  }
}
</script>
<style scoped lang="scss">
.live-pane-current-segment {
  width: 100%;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 0;

  .current-segment-card {
    width: 100%;

    ::v-deep .el-card__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      background: #fff;
      color: #333;
      font-weight: 500;
      border-bottom: 1px solid #e8e8e8;

      .clearfix {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .speaker-info {
        font-size: 13px;
        padding: 4px 12px;
        background: #f0f2f5;
        color: #606266;
        border-radius: 12px;
      }
    }

    ::v-deep .el-card__body {
      padding: 20px;
    }

    // 完整文本显示区域
    .full-text-container {
      min-height: 100px;
      padding: 16px 20px;
      background: #fafafa;
      border-radius: 4px;
      border: 1px solid #e8e8e8;

      .full-text-content {
        font-size: 16px;
        line-height: 1.8;
        color: #2c3e50;
        white-space: pre-wrap;
        word-wrap: break-word;

        &.placeholder-text {
          color: #999;
          font-style: italic;
        }
      }
    }
  }
}
</style>
