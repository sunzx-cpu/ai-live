<template>
  <el-dialog
    title="同音词设置"
    :visible.sync="dialogVisible"
    width="600px"
    top="10vh"
    :close-on-click-modal="false"
    custom-class="homophone-dialog"
    :before-close="handleClose">

    <!-- 提示说明 -->
    <div class="tip-info">
      AI主播在播报时，进行同音词替换。<span style="font-weight: 500;">建议：</span>使用多个字的词语，不要使用一个字。一行一个，左侧为原文字→右侧为替换的文字，使用等号连接。
    </div>

    <!-- 文本输入区 -->
    <div class="input-section">
      <el-input
        type="textarea"
        v-model="homophoneText"
        :rows="12"
        placeholder="请输入同音词替换规则，例如：&#10;抓紧时间=抓紧时&#10;山楂干=山楂肝&#10;靓女=靓仔">
      </el-input>
    </div>

    <!-- 底部按钮 -->
    <span slot="footer" class="dialog-footer">
      <el-button @click="callback()">关闭</el-button>
      <el-button type="primary" @click="saveHomophone">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'HomophoneDialog',
  data() {
    return {
      dialogVisible: false,
      initialData: null,
      homophoneText: ''
    }
  },
  computed: {
    hasChanges() {
      if (!this.initialData) return false
      return JSON.stringify(this.getCurrentData()) !== JSON.stringify(this.initialData)
    }
  },
  watch: {
    // 监听对话框打开
    dialogVisible(newVal) {
      if (newVal) {
        // 打开对话框时加载配置
        this.loadHomophone()
      }
    }
  },
  methods: {
    // 获取当前表单数据
    getCurrentData() {
      return {
        homophoneText: this.homophoneText
      }
    },
    // 处理对话框关闭（X按钮或ESC）
    handleClose(done) {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要关闭吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          done()
        }).catch(() => {})
      } else {
        done()
      }
    },

    // 处理关闭按钮点击
    callback() {
      if (this.hasChanges) {
        this.$confirm('您有未保存的修改，确定要关闭吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '返回编辑',
          type: 'warning'
        }).then(() => {
          this.dialogVisible = false
        }).catch(() => {})
      } else {
        this.dialogVisible = false
      }
    },

    // 保存同音词设置
    saveHomophone() {
      try {
        // 保存到 localStorage
        localStorage.setItem('homophone_config', this.homophoneText)

        // 更新初始数据快照
        this.initialData = this.getCurrentData()

        this.$message.success('同音词设置已保存')
        this.dialogVisible = false
      } catch (error) {
        console.error('保存同音词设置失败:', error)
        this.$message.error('保存失败')
      }
    },

    // 加载同音词设置
    loadHomophone() {
      try {
        const saved = localStorage.getItem('homophone_config')
        if (saved) {
          this.homophoneText = saved
        } else {
          // 默认示例
          this.homophoneText = `抓紧时间=抓紧时
山楂干=山楂肝
靓女=靓仔
靓仔=靓妈
注重=注众
长靴=涨靴
拍摄=欧佩
强出=强出`
        }
      } catch (error) {
        console.error('加载同音词设置失败:', error)
        this.homophoneText = ''
      }

      // 保存初始数据快照
      this.$nextTick(() => {
        this.initialData = this.getCurrentData()
      })
    }
  }
}
</script>

<style>
/* 全局样式：修改 Dialog 的 body 和 footer */
.homophone-dialog .el-dialog__body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.homophone-dialog .el-dialog__footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
  position: sticky;
  bottom: 0;
  z-index: 10;
}
</style>

<style scoped lang="scss">
// 蓝色提示框
.tip-info {
  padding: 12px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 16px;
}

// 输入区域
.input-section {
  margin-bottom: 12px;
}

// 底部按钮区
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
