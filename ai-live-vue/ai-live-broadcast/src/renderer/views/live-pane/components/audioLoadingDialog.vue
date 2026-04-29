<template>
  <el-dialog
    title="正在准备开播"
    :visible.sync="visible"
    width="500px"
    :close-on-click-modal="false"
    :show-close="false"
    center>
    <div class="loading-content">
      <div class="loading-text">
        <i class="el-icon-loading"></i>
        正在加载音频文件...
      </div>
      <el-progress
        :percentage="percentage"
        :stroke-width="20"></el-progress>
      <div class="loading-info">
        已加载: {{ loadedCount }} / {{ totalCount }}
      </div>
      <div class="loading-tip">
        请稍候，正在为您准备播放内容
      </div>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'AudioLoadingDialog',
  props: {
    dialogVisible: {
      type: Boolean,
      default: false
    },
    loadedCount: {
      type: Number,
      default: 0
    },
    totalCount: {
      type: Number,
      default: 0
    }
  },
  computed: {
    visible: {
      get() {
        return this.dialogVisible
      },
      set(val) {
        if (!val) {
          this.$emit('close')
        }
      }
    },
    percentage() {
      if (this.totalCount === 0) return 0
      return Math.floor((this.loadedCount / this.totalCount) * 100)
    }
  }
}
</script>

<style scoped lang="scss">
.loading-content {
  padding: 20px;
  text-align: center;
}

.loading-text {
  font-size: 16px;
  color: #303133;
  margin-bottom: 24px;
  font-weight: 500;

  i {
    margin-right: 8px;
    color: #409eff;
  }
}

.loading-info {
  margin-top: 16px;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

.loading-tip {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}
</style>
