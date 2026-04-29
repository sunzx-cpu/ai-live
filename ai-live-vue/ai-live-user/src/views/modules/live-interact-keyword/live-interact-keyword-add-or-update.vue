<template>
  <el-dialog
    title="修改互动模板管理"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="850px">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="90px">
      <el-form-item label="脚本编号" prop="scriptId">
        <el-select v-model="dataForm.scriptId" placeholder="锚标脚本" style="width: 100%">
          <el-option v-for="item in scriptList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="关键词" prop="keyword">
        <el-input v-model="dataForm.keyword" placeholder="关键词"></el-input>
      </el-form-item>
      <el-form-item label="语音回复">
        <el-select v-model="voiceVariable" placeholder="快捷输入直播变量" style="width: 100%" @change="handleVoiceVariableSelect">
          <el-option label="下个整时间隔" value="下个整时间隔"></el-option>
          <el-option label="下个整点" value="下个整点"></el-option>
          <el-option label="当前时间" value="当前时间"></el-option>
          <el-option label="当前在线人数" value="当前在线人数"></el-option>
          <el-option label="用户昵称" value="用户昵称"></el-option>
          <el-option label="当前日期" value="当前日期"></el-option>
          <el-option label="重复用户发言" value="重复用户发言"></el-option>
          <el-option label="下个10分间隔" value="下个10分间隔"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <div class="tip-info">
          关键词支持星号代表任意文字，即"*代表任意多文字，例: 关键词为 包*邮，则会匹配用户发言【包邮】【包邮吗】【新疆包邮吗123邮币时，则也会匹配到】。
        </div>
        <el-input
          type="textarea"
          :rows="8"
          v-model="dataForm.content"
          placeholder="请输入语音回复内容"
          maxlength="3000"
          @input="handleContentInput">
        </el-input>
        <div class="word-count" :class="{ 'warning': dataForm.content.length > 2700 }">
          {{ dataForm.content.length }}/3000
        </div>
      </el-form-item>
      <el-form-item label="文字回复" prop="textContent">
        <div class="tip-info2">
          (仅对客)当被关关注键词匹配时，如果在客户端勾过了文字互动，则会用下方文字回复。
        </div>
        <el-input
          type="textarea"
          :rows="8"
          v-model="dataForm.textContent"
          placeholder="请输入文字回复内容"
          maxlength="3000"
          @input="handleTextContentInput">
        </el-input>
        <div class="word-count" :class="{ 'warning': dataForm.textContent.length > 2700 }">
          {{ dataForm.textContent.length }}/3000
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
        voiceVariable: '',
        dataForm: {
          id: 0,
          scriptId: '',
          keyword: '',
          content: '',
          textContent: ''
        },
        dataRule: {
          scriptId: [
            { required: true, message: '脚本不能为空', trigger: 'blur' }
          ],
          keyword: [
            { required: true, message: '关键词不能为空', trigger: 'blur' }
          ],
          content: [
            { required: true, message: '内容不能为空', trigger: 'blur' },
            { max: 3000, message: '内容不能超过3000个字符', trigger: 'blur' }
          ]
        },
        scriptList: []
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true

        this.getScriptList()
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http.get(`/apps/live-interact-keyword/info/${this.dataForm.id}`).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm = data.liveInteractKeyword
                if (!this.dataForm.textContent) {
                  this.dataForm.textContent = ''
                }
              }
            })
          }
        })
      },
      // 获取脚本列表
      getScriptList () {
        this.$http.get('/apps/live-script/all').then(({data}) => {
          if (data && data.code === 0) {
            this.scriptList = data.list
          }
        })
      },
      // 处理语音变量选择
      handleVoiceVariableSelect(value) {
        if (value) {
          this.dataForm.content += `{${value}}`
          // 清空选择
          this.$nextTick(() => {
            this.voiceVariable = ''
          })
        }
      },
      // 处理内容输入
      handleContentInput(value) {
        if (value.length > 3000) {
          this.dataForm.content = value.slice(0, 3000)
        }
      },
      // 处理文字回复输入
      handleTextContentInput(value) {
        if (value.length > 3000) {
          this.dataForm.textContent = value.slice(0, 3000)
        }
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http.post(`/apps/live-interact-keyword/${!this.dataForm.id ? 'save' : 'update'}`, {
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

.tip-info2 {
  padding: 12px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  color: #409eff;
  font-size: 13px;
  margin-bottom: 8px;
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