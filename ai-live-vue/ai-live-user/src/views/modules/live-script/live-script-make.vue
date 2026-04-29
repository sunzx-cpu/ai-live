<!-- 脚本编排 -->
<template>
  <el-dialog
    title="修改播放脚本管理"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="850px">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" label-width="90px">
      <el-form-item label="脚本名称" prop="name">
        <el-input v-model="dataForm.name" placeholder="脚本名称"></el-input>
      </el-form-item>
      <el-form-item label="脚本编排">
        <el-select v-model="scriptArrangement" placeholder="快捷输入直播片段" style="width: 100%" @change="handleSegmentSelect">
          <el-option
            v-for="item in segmentList"
            :key="item.id"
            :label="item.name"
            :value="item.name">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="">
        <div class="tip-info">
          拼编排模式请点此支持随机，如：{A段1|片段2|片段3|提品1|提品2}
        </div>
        <div class="tip-warning">
          此处为脚本管理，直对走本片段进行编排使用，如需编写走本請进入 >
          <span class="link-text" @click="openSegmentManagement">话术片段管理</span>
        </div>
      </el-form-item>
      <el-form-item label="" prop="content">
        <el-input
          type="textarea"
          :rows="10"
          v-model="dataForm.content"
          placeholder="请输入脚本内容"
          maxlength="3000"
          @input="handleContentInput">
        </el-input>
        <div class="word-count" :class="{ 'warning': dataForm.content.length > 2700 }">
          {{ dataForm.content.length }}/3000
        </div>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        scriptArrangement: '',
        dataForm: {
          id: 0,
          name: '',
          content: ''
        },
        dataRule: {
          name: [
            { required: true, message: '脚本名称不能为空', trigger: 'blur' }
          ],
          content: [
            { required: true, message: '脚本内容不能为空', trigger: 'blur' },
            { max: 3000, message: '脚本内容不能超过3000个字符', trigger: 'blur' }
          ]
        },

        segmentList: []
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.getSegmentList(id)
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http.get(`/apps/live-script/info/${this.dataForm.id}`).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm = data.liveScript
                if (this.dataForm.content == null) {
                  this.dataForm.content = ''
                }
              }
            })
          }
        })
      },

      // 获取脚本片段列表
      getSegmentList (id) {
        this.$http.get(`/apps/live-segment/all`, {
          'scriptId': id
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.segmentList = data.list
          }
        })
      },

      // 快速插入脚本片段
      insertSegment (item) {
        this.dataForm.content += `{${item.name}}`
      },

      // 处理片段选择
      handleSegmentSelect(value) {
        if (value) {
          this.dataForm.content += `{${value}}`
          // 清空选择
          this.$nextTick(() => {
            this.scriptArrangement = ''
          })
        }
      },

      // 打开话术片段管理页面
      openSegmentManagement() {
        // 先关闭对话框
        this.visible = false
        // 然后跳转
        this.$nextTick(() => {
          this.$router.push({ name: 'live-segment' })
        })
      },

      // 处理内容输入
      handleContentInput(value) {
        if (value.length > 3000) {
          this.dataForm.content = value.slice(0, 3000)
        }
      },

      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http.post(`/apps/live-script/${!this.dataForm.id ? 'save' : 'update'}`, {
              ...this.dataForm
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
.tip-info {
  padding: 12px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.tip-warning {
  padding: 12px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.5;
}

.link-text {
  color: #409eff;
  text-decoration: underline;
  cursor: pointer;
  font-weight: bold;

  &:hover {
    color: #66b1ff;
  }
}

.example-text {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  font-family: monospace;
  line-height: 1.5;
}

.word-count {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 5px;

  &.warning {
    color: #E6A23C;
  }
}
</style>
