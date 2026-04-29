<template>
  <el-dialog :close-on-click-modal="false" :title="!dataForm.id ? '新增' : '修改'" :visible.sync="visible">
    <el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="120px" @keyup.enter.native="saveHandle">
      <el-form-item label="套餐名称" prop="packageName">
        <el-input v-model="dataForm.packageName" placeholder="套餐名称"></el-input>
      </el-form-item>
      <el-form-item label="价格" prop="price">
        <el-input-number v-model="dataForm.price" :precision="2" :step="0.01" placeholder="价格"></el-input-number>
      </el-form-item>
      <el-form-item label="有效期(天)" prop="duration">
        <el-input-number v-model="dataForm.duration" placeholder="有效期(天)"></el-input-number>
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
        packageName: '',
        price: 0,
        duration: 0,
        status: 1
      },
      dataRule: {
        packageName: [
          {required: true, message: '套餐名称不能为空', trigger: 'blur'}
        ],
        price: [
          {required: true, message: '价格不能为空', trigger: 'blur'}
        ],
        duration: [
          {required: true, message: '有效期不能为空', trigger: 'blur'}
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
          url: this.$http.adornUrl(`/biz/license-package/info/${this.dataForm.id}`),
          method: 'get'
        }).then(({data}) => {
          if (data && data.code === 200) {
            this.dataForm = data.licensePackage
          }
        })
      }
    },
    // 表单提交
    saveHandle() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const url = !this.dataForm.id ? '/biz/license-package/save' : '/biz/license-package/update'
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
