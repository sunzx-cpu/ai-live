<template>
  <nav class="site-navbar" :class="'site-navbar--' + navbarLayoutType" style="background-color: #263238;">
    <div class="site-navbar__header">
      <h1 class="site-navbar__brand" style="background-color: #263238;" @click="$router.push({ name: 'live-pane' })">
        <a class="site-navbar__brand-lg" href="javascript:;">
          <img src="~@/assets/img/logo.png"  style="width: 90%; height: 90%;" />
        </a>
        <a class="site-navbar__brand-mini" style="color: #666;" href="javascript:;">LIVE</a>
      </h1>
    </div>
    <div class="site-navbar__body clearfix">
      <!-- 折叠按钮 -->
      <el-menu
        class="site-navbar__menu"
        mode="horizontal">
        <el-menu-item class="site-navbar__switch" index="0" @click="sidebarFold = !sidebarFold">
          <icon-svg name="zhedie"></icon-svg>
        </el-menu-item>
      </el-menu>
      <el-menu
        class="site-navbar__menu site-navbar__menu--right"
        mode="horizontal">
        <el-menu-item class="site-navbar__avatar" index="3">
          <el-dropdown :show-timeout="0" placement="bottom">
            <span class="el-dropdown-link">
              <img src="~@/assets/img/avatar.png" :alt="userName">{{ userName }}
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="updatePasswordHandle()">修改密码</el-dropdown-item>
              <el-dropdown-item @click.native="logoutHandle()">退出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-menu-item>
      </el-menu>
    </div>
    <!-- 弹窗, 修改密码 -->
    <update-password v-if="updatePasswordVisible" ref="updatePassword"></update-password>
  </nav>
</template>

<script>
  import UpdatePassword from './main-navbar-update-password'
  import { clearLoginInfo } from '@/utils'
import {messages} from "../i18n";
  import Cookies from "js-cookie";
  export default {
    data () {
      return {
        i18nMessages: messages,
        updatePasswordVisible: false
      }
    },
    components: {
      UpdatePassword
    },
    computed: {
      navbarLayoutType: {
        get () { return this.$store.state.common.navbarLayoutType }
      },
      sidebarFold: {
        get () { return this.$store.state.common.sidebarFold },
        set (val) { this.$store.commit('common/updateSidebarFold', val) }
      },
      mainTabs: {
        get () { return this.$store.state.common.mainTabs },
        set (val) { this.$store.commit('common/updateMainTabs', val) }
      },
      userName: {
        get () { return this.$store.state.user.name }
      },
      email: {
        get () { return this.$store.state.user.email }
      }
    },
    created() {
    },
    methods: {
      //保存语言选择
      saveIl8n(key){
        this.$i18n.locale = key
        Cookies.set('language', this.$i18n.locale);
      },
      // 修改密码
      updatePasswordHandle () {
        this.updatePasswordVisible = true
        this.$nextTick(() => {
          this.$refs.updatePassword.init()
        })
      },
      // 退出
      logoutHandle () {
        this.$confirm(`确定进行[退出]操作?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/sys/logout'),
            method: 'post',
            data: this.$http.adornData()
          }).then(({data}) => {
            if (data && data.code === 0) {
              clearLoginInfo()
              this.$router.push({ name: 'login' })
            }
          })
        }).catch(() => {})
      }
    }
  }
</script>
