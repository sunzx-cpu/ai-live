<!-- 开播确认对话框 -->
<template>
  <el-dialog
    title="开播确认"
    :visible.sync="dialogVisible"
    width="600px"
    :before-close="handleClose">
    <div class="dialog-content">
      <!-- 副标题 -->
      <div class="dialog-subtitle">
        请确认是否开播
      </div>

      <!-- 模式选择 -->
      <div class="mode-selection">
        <div class="mode-label">请选择开播模式：</div>
        <el-radio-group v-model="selectedMode" size="medium">
          <el-radio-button label="audio">
            <i class="el-icon-microphone"></i>
            仅使用音频
          </el-radio-button>
          <el-radio-button label="video" disabled>
            <i class="el-icon-video-camera"></i>
            使用音频和视频
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 说明文字区域 -->
      <div class="mode-desc-box">
        <div class="desc-item">
          <span class="desc-title">仅使用音频：</span>
          <span class="desc-text">使用AI主播进行循环话术播放！</span>
        </div>
        <div class="desc-item">
          <span class="desc-title">使用视频和音频：</span>
          <span class="desc-text">AI主播在播话术的同时播放对应关联的视频,做到AI音频与视频同步！</span>
        </div>
        <div class="desc-item desc-note">
          <span class="desc-text">注：上方的两种模式，以音视频效果为准是！之视频可在关联片段文件夹中放入多个广告行播讲现议。</span>
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消开播</el-button>
      <el-button type="primary" @click="handleConfirm">确认开播</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'StartPlayDialog',
  data() {
    return {
      dialogVisible: false,
      selectedMode: 'audio'  // 默认选择仅使用音频：'audio' | 'video'
    }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false
    },
    handleConfirm() {
      // 返回选择的模式配置
      this.$emit('confirm', {
        useAudioOnly: this.selectedMode === 'audio',
        useVideo: this.selectedMode === 'video'
      })
      this.handleClose()
    }
  }
}
</script>

<style scoped lang="scss">
.dialog-content {
  padding: 0;

  .dialog-subtitle {
    font-size: 14px;
    color: #606266;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e4e7ed;
  }

  .mode-selection {
    margin-bottom: 20px;

    .mode-label {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 12px;
    }

    .el-radio-group {
      display: flex;
      justify-content: center;
    }

    ::v-deep .el-radio-button {
      .el-radio-button__inner {
        min-width: 160px;
        padding: 12px 20px;

        i {
          margin-right: 6px;
        }
      }
    }
  }

  .mode-desc-box {
    padding: 16px;
    background-color: #ecf5ff;
    border-radius: 4px;

    .desc-item {
      margin-bottom: 10px;
      line-height: 1.8;

      &:last-child {
        margin-bottom: 0;
      }

      .desc-title {
        font-weight: 500;
        color: #409eff;
      }

      .desc-text {
        color: #409eff;
        font-size: 14px;
      }

      &.desc-note {
        margin-top: 12px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
