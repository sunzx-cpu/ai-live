<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="密钥" prop="secretKey">
      <div style="display: flex;justify-content: space-between">
        <el-input v-model="dataForm.secretKey" placeholder="密钥"></el-input>
        <el-button type="success" icon="el-icon-refresh" @click="createSecretKey" style="margin-left: 10px">生成密钥</el-button>
      </div>
    </el-form-item>
    <el-form-item label="到期时间" prop="expirationTime">
      <el-date-picker type="datetime" v-model="dataForm.expirationTime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="到期时间"></el-date-picker>
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
        dataForm: {
          id: 0,
          secretKey: '',
          expirationTime: ''
        },
        dataRule: {
          secretKey: [
            { required: true, message: '密钥不能为空', trigger: 'blur' }
          ],
          expirationTime: [
            { required: true, message: '到期时间不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      createSecretKey () {
        this.dataForm.secretKey = crypto.randomUUID().replaceAll("-", "")
      },
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http.get(`/biz/secretkey/info/${this.dataForm.id}`).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm = data.secretKey
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http.post(`/biz/secretkey/${!this.dataForm.id ? 'save' : 'update'}`, {
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
