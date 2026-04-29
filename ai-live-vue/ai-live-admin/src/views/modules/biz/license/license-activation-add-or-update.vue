<template>
  <el-dialog :close-on-click-modal="false" :title="!dataForm.id ? '新增' : '修改'" :visible.sync="visible">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="120px" @keyup.enter.native="saveHandle">
      <el-form-item label="激活码" prop="activationCode">
        <el-input v-model="dataForm.activationCode" placeholder="激活码"></el-input>
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input-number v-model="dataForm.userId" placeholder="用户ID"></el-input-number>
      </el-form-item>
      <el-form-item label="设备信息" prop="deviceInfo">
        <el-input v-model="dataForm.deviceInfo" placeholder="设备信息"></el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="dataForm.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="saveHandle">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
      dataForm: {
        id: 0,
        activationCode: '',
        userId: 0,
        deviceInfo: '',
        status: 1
      },
      dataRule: {
        activationCode: [
          {required: true, message: '激活码不能为空', trigger: 'blur'}
        ],
        userId: [
          {required: true, message: '用户ID不能为空', trigger: 'blur'}
        ],
        deviceInfo: [
          {required: true, message: '设备信息不能为空', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0
      this.visible = true
      if (this.dataForm.id) {
        this.$http({
          url: this.$http.adornUrl(`/biz/license-activation/info/${this.dataForm.id}`),
          method: 'get'
        }).then(({data}) => {
          if (data && data.code === 200) {
            this.dataForm = data.licenseActivation
          }
        })
      }
    },
    // 表单提交
    saveHandle() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const url = !this.dataForm.id ? '/biz/license-activation/save' : '/biz/license-activation/update'
          this.$http({
            url: this.$http.adornUrl(url),
            method: 'post',
            data: this.$http.adornData(this.dataForm)
          }).then(({data}) => {
            if (data && data.code === 200) {
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
