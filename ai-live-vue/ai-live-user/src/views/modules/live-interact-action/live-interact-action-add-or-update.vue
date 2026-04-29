<template>
  <el-dialog
    title="修改行为互动模板"
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="600px">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="90px">
      <el-form-item label="脚本名称" prop="scriptId">
        <el-select v-model="dataForm.scriptId" placeholder="请选择脚本" style="width: 100%">
          <el-option v-for="item in scriptList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="行为名称" prop="actionType">
        <el-select v-model="dataForm.actionType" placeholder="请选择行为名称" style="width: 100%">
          <el-option label="粉丝团消息" value="粉丝团消息"></el-option>
          <el-option label="礼物消息" value="礼物消息"></el-option>
          <el-option label="关注消息" value="关注消息"></el-option>
          <el-option label="进入直播间" value="进入直播间"></el-option>
          <el-option label="点赞消息" value="点赞消息"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="回复内容">
        <el-select v-model="actionVariable" placeholder="快捷输入直播变量" style="width: 100%" @change="handleActionVariableSelect">
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
      <el-form-item label="" prop="content">
        <el-input
          type="textarea"
          :rows="10"
          v-model="dataForm.content"
          placeholder="请输入回复内容"
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
        actionVariable: '',
        dataForm: {
          id: 0,
          scriptId: '',
          actionType: '',
          content: ''
        },
        dataRule: {
          scriptId: [
            { required: true, message: '脚本不能为空', trigger: 'blur' }
          ],
          actionType: [
            { required: true, message: '行为名称不能为空', trigger: 'blur' }
          ],
          content: [
            { required: true, message: '回复内容不能为空', trigger: 'blur' },
            { max: 3000, message: '回复内容不能超过3000个字符', trigger: 'blur' }
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
            this.$http.get(`/apps/live-interact-action/info/${this.dataForm.id}`).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm = data.liveInteractAction
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
      // 处理行为变量选择
      handleActionVariableSelect(value) {
        if (value) {
          this.dataForm.content += `{${value}}`
          // 清空选择
          this.$nextTick(() => {
            this.actionVariable = ''
          })
        }
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
            this.$http.post(`/apps/live-interact-action/${!this.dataForm.id ? 'save' : 'update'}`, {
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
.word-count {
  text-align: right;
  color: #606266;
  font-size: 12px;
  margin-top: 5px;

  &.warning {
    color: #E6A23C;
  }
}
</style>