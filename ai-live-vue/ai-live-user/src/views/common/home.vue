<template>
  <div class="mod-home">
    <div class="welcome-container">
      <h1 class="welcome-title">欢迎使用 AI直播系统</h1>
      <ul class="feature-list">
        <li>请联系渠道在后台进行开通激活</li>
        <li>激活后，请重新登录</li>
        <li>如需帮助请联系技术人员</li>
        <li>获取程序相关文档资料</li>
      </ul>
      <div class="update-time">更新时间: 2025-06-14 14:09:07</div>
      <div class="activation-section">
        <el-input
          v-model="activationCode"
          placeholder="请输入激活码"
          class="activation-input"
        ></el-input>
        <el-button type="primary" @click="handleActivation">激活</el-button>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    data () {
      return {
        activationCode: ''
      }
    },
    components: {
    },
    activated () {
    },
    methods: {
      handleActivation () {
        if (!this.activationCode) {
          this.$message.warning('请输入激活码')
          return
        }
        // TODO: 暂时不接入实际接口，只做UI展示
        this.$message.success('激活功能暂未开放')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .mod-home {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 500px;
    padding: 20px;
  }

  .welcome-container {
    text-align: center;
    max-width: 600px;
  }

  .welcome-title {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin-bottom: 30px;
  }

  .feature-list {
    list-style: disc;
    text-align: left;
    margin: 0 auto 20px;
    padding-left: 40px;
    max-width: 400px;

    li {
      margin-bottom: 12px;
      font-size: 14px;
      line-height: 1.6;
      color: #666;
    }
  }

  .update-time {
    font-size: 12px;
    color: #67c23a;
    margin-bottom: 30px;
  }

  .activation-section {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 10px;

    .activation-input {
      width: 300px;
    }
  }
</style>
