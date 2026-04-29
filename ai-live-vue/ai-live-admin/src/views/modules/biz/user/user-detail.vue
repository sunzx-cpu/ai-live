<template>
  <el-dialog title="用户详情" :close-on-click-modal="false" :visible.sync="visible">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户ID">{{ dataForm.id }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ dataForm.username }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ dataForm.nickname }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ dataForm.mobile }}</el-descriptions-item>
      <el-descriptions-item label="简介">{{ dataForm.intro }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag v-if="dataForm.status === 0">未激活</el-tag>
        <el-tag v-if="dataForm.status === 1" type="success">激活</el-tag>
        <el-tag v-if="dataForm.status === 2" type="warning">到期</el-tag>
        <el-tag v-if="dataForm.status === 3" type="danger">禁用</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ dataForm.createTime }}</el-descriptions-item>
    </el-descriptions>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">关闭</el-button>
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
        username: '',
        nickname: '',
        mobile: '',
        createTime: '',
        updateTime: ''
      }
    }
  },
  methods: {
    init(id) {
      this.dataForm.id = id || 0
      this.visible = true
      this.$nextTick(() => {
        if (this.dataForm.id) {
          this.$http.get(`/biz/user/info/${this.dataForm.id}`).then(({ data }) => {
            if (data && data.code === 0) {
              this.dataForm = data.user
            }
          })
        }
      })
    }
  }
}
</script>