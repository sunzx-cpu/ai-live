<!--  -->
<template>
  <div v-if="!IsUseSysTitle&&!IsWeb" class="window-title">
    <!-- 软件logo预留位置 -->
    <div v-if="isNotMac" class="logo" style="-webkit-app-region: drag;">
      <svg-icon icon-class="electron-logo"></svg-icon>
    </div>
    <!-- 菜单栏位置 -->
    <div></div>
    <!-- 中间标题位置 -->
    <div class="title" style="-webkit-app-region: drag;"></div>
    <div v-if="isNotMac" class="controls-container">
      <div class="windows-icon-bg" @click="Mini">
        <svg-icon class-name="icon-size" icon-class="mini"></svg-icon>
      </div>
      <div class="windows-icon-bg" @click="MixOrReduction">
        <svg-icon v-if="mix" class-name="icon-size" icon-class="reduction"></svg-icon>
        <svg-icon v-else class-name="icon-size" icon-class="mix"></svg-icon>
      </div>
      <div class="windows-icon-bg close-icon" @click="Close">
        <svg-icon class-name="icon-size" icon-class="close"></svg-icon>
      </div>
    </div>
  </div>
</template>

<script>
import {ipcRenderer} from "electron";

export default {
  data: () => ({
    mix: false,
    IsUseSysTitle: false,
    isNotMac: process.platform !== "darwin",
    IsWeb: process.env.IS_WEB
  }),

  components: {},
  created() {
    ipcRenderer.invoke("IsUseSysTitle").then(res => {
      this.IsUseSysTitle = res;
    });
  },

  mounted() {
    ipcRenderer.on("w-max", (event, state) => {
      this.mix = state
    })
  },

  methods: {
    Mini() {
      ipcRenderer.invoke("windows-mini");
    },
    MixOrReduction() {
      ipcRenderer.invoke("window-max").then(res => {
        this.mix = res.status
      })
    },
    Close() {
      ipcRenderer.invoke("window-close");
    }
  },
  destroyed() {
    ipcRenderer.removeAllListeners("w-max");
  }
};
</script>
<style lang='scss' rel='stylesheet/scss' scoped>
.window-title {
  width: 100%;
  height: 30px;
  line-height: 30px;
  display: flex;
  -webkit-app-region: drag;
  position: fixed;
  top: 0;
  z-index: 99999;
  background-color: #FFF;

  .title {
    text-align: center;
  }

  .logo {
    margin-left: 20px;
  }

  .controls-container {
    display: flex;
    flex-grow: 0;
    flex-shrink: 0;
    text-align: center;
    position: relative;
    z-index: 3000;
    -webkit-app-region: no-drag;
    height: 100%;
    width: 138px;
    margin-left: auto;

    .windows-icon-bg {
      display: inline-block;
      -webkit-app-region: no-drag;
      height: 100%;
      width: 33.34%;
      color: rgba(129, 129, 129, 0.6);

      .icon-size {
        width: 12px;
        height: 15px;
      }
    }

    .windows-icon-bg:hover {
      background-color: rgba(182, 182, 182, 0.2);
      color: #333;
    }

    .close-icon:hover {
      background-color: rgba(232, 17, 35, 0.9);
      color: #fff;
    }
  }
}
</style>