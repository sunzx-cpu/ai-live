<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" title="屏蔽自己&敏感词" width="60%">
    <el-form ref="dataForm" :model="dataForm" label-width="180px" size="small" @keyup.enter.native="dataFormSubmit()">
      <el-form-item label="屏蔽用户名">
        <el-input v-model="dataForm.username" placeholder="请输入弹幕屏蔽的用户昵称。如：张三 或 张三|李四，多个用 | 隔开。" size="small"></el-input>
      </el-form-item>
      <el-form-item label="移除敏感词">
        <el-input v-model="dataForm.sensitiveWords" placeholder="移除用户名字或发言中的字词，如名字前的灯牌名或违禁词，多个用 | 隔开。" size="small"></el-input>
      </el-form-item>
      <el-form-item label="屏蔽整句">
        <el-input v-model="dataForm.wholeSentence" placeholder="如果用户名字或发言中的字词包含了设置的字词，则整个屏蔽，多个用 | 隔开。" size="small"></el-input>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
        <el-button @click="callback()">取消</el-button>
        <el-button type="primary" @click="dataFormSubmit">确定</el-button>
      </span>
  </el-dialog>
</template>

<script>
import {save, getByMerchantId} from '@/api/liveShield'

export default {
  name: "ShieldDialog",
  data() {
    return {
      dialogVisible: false,
      dataForm: {
        username: "",
        sensitiveWords: "",
        wholeSentence: ""
      }
    }
  },
  methods: {
    callback() {
      this.dialogVisible = false
    },
    init () {
      getByMerchantId().then(({data}) => {
        if (data && data.code === 0) {
          this.dataForm = data.data || {};
          // 存入缓存
          localStorage.setItem('shield-username', this.dataForm.username);
          localStorage.setItem('shield-sensitiveWords', this.dataForm.sensitiveWords);
          localStorage.setItem('shield-wholeSentence', this.dataForm.wholeSentence);
        }
      })
    },
    dataFormSubmit() {
      // 保存逻辑
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          save(this.dataForm).then(({data}) => {
            if (data && data.code === 0) {
              this.dialogVisible = false;
              this.$message.success("保存成功");
            } else {
              this.$message.error(data.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>

</style>