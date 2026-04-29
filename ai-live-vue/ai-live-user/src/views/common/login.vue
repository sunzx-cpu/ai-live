<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh;">
    <div v-if="!licenceIsOk">
      <h1>POS solution authority expires, please contact technical support.</h1>
    </div>

    <div class="site-wrapper site-page--login">
      <div class="site-content__wrapper">
        <div class="site-content">
          <div class="brand-info">
            <img alt="直播系统" class="brand-info__box" src="~@/assets/img/login-box-bg.svg">
            <h2 class="brand-info__text">直播系统</h2>
            <p class="brand-info__intro">欢迎使用直播系统</p>
          </div>
          <div class="login-main">
            <div class="login-logo">
            </div>
            <h3 class="login-title">用户登录</h3>
            <el-form :model="dataForm" :rules="dataRule" ref="dataForm" status-icon>
              <template>
                <el-form-item prop="username">
                  <el-input v-model="dataForm.username" placeholder="帐号"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                  <el-input v-model="dataForm.password" type="password" placeholder="密码"></el-input>
                </el-form-item>
              </template>
              <el-form-item>
                <el-button class="login-btn-submit" type="primary" :loading="loginLoading"
                  @click="dataFormSubmit()">登录</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { messages } from '@/i18n'
export default {
  data() {
    return {
      i18nMessages: messages,
      loginLoading: false,
      licenceIsOk: true,
      dataForm: {
        username: 'test',
        password: '123456',
      },
      dataRule: {
        username: [
          { required: true, message: '帐号不能为空', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' }
        ],
      },
    }
  },
  methods: {
    // 提交表单
    dataFormSubmit() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          this.loginLoading = true
          this.$http({
            url: this.$http.adornUrl('/apps/login'),
            method: 'post',
            data: this.$http.adornData({
              ...this.dataForm
            })
          }).then(({ data }) => {
            this.loginLoading = false
            if (data && data.code === 0) {
              this.$cookie.set('token', data.token)
              this.$router.replace({ name: 'home' })
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

<style lang="scss">
.site-wrapper.site-page--login {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: #fff;
  ;
  overflow: hidden;

  &:before {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    margin-left: -48%;
    content: "";
    background-image: url(~@/assets/img/login_bg.svg);
    background-repeat: no-repeat;
    background-position: 100%;
    background-size: auto 100%;
  }

  .site-content__wrapper {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    padding: 0;
    margin: 0;
    overflow-x: hidden;
    overflow-y: auto;
    background-color: transparent;
  }

  .site-content {
    min-height: 100%;
    padding: 30px 30px 30px 30px;
  }

  .brand-info {
    margin: 220px 0 0 150px;
    color: #fff;
  }

  .brand-info__text {
    margin: 50px 0 22px 0;
    font-size: 40px;
    font-weight: 400;
  }

  .brand-info__intro {
    margin: 10px 0;
    font-size: 16px;
    line-height: 1.58;
    opacity: .6;
  }

  .brand-info__box {
    width: 30%;
  }

  .login-main {
    position: absolute;
    top: 0;
    right: 180px;
    padding: 150px 60px 180px;
    width: 470px;
    min-height: 100%;
    background-color: #fff;
  }

  .login-logo {
    text-align: center;
  }

  .login-title {
    font-size: 28px;
    margin-top: 40px;
  }

  .login-type {
    margin-bottom: 8px;
    padding: 3px;

    .disable {
      color: #999;
    }
  }

  .login-captcha {
    overflow: hidden;

    >img {
      width: 100%;
      cursor: pointer;
    }
  }

  .login-btn-submit {
    width: 100%;
    margin-top: 38px;
  }
}
</style>
