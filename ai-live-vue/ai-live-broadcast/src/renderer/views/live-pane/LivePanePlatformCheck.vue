<!-- 平台检测防劫区 -->
<template>
  <div class="live-pane-platform-check">
    <el-row type="flex" align="middle" justify="start">
      <el-col>
        <el-switch v-model="platformCheck" active-text="平台检测防劫" inactive-text="平台检测防劫" />
      </el-col>
    </el-row>
  </div>
</template>
<script>
export default {
  name: 'LivePanePlatformCheck',
  data() {
    return {
      platformCheck: false
    }
  }
}
</script>
<style scoped lang="scss">
.live-pane-platform-check {
  width: 100%;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 4px;
  padding: 12px 16px;
  box-sizing: border-box;
}
</style>
