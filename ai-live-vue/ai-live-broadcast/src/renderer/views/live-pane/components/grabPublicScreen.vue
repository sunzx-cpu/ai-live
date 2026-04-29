<template>
  <el-main class="main">
    <el-button size="small" type="danger" @click="toggleWindowCollapse"> {{ isCollapsed ? '恢复窗口' : '收缩到右下角' }}</el-button>
    <el-divider content-position="left"><span class="tips-title">直播间公屏弹幕监控-抓取弹幕时，不要关闭！！！</span></el-divider>

    <el-form :model="form" label-width="100px" size="small">
      <el-form-item label="使用说明：">
        <el-tag size="small">请在下方输入直播间地址，可拖放到一旁(把该窗口中的弹幕区放置在可看到区域)</el-tag>
      </el-form-item>
      <el-form-item label="直播地址：">
        <div class="flex-colum-center">
          <el-input v-model="form.liveUrl" placeholder="请输入直播间地址" @change="dataFormSubmit"></el-input>
          <div class="m-l-10">
            <el-button type="success" @click="openLiveRoom">打开直播间</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-tag><span class="tips-text">！！！提示：1.在打开的直播窗口中登录抖音号，2.设置弹幕内容，多句时使用竖线'|'隔开，每句弹幕字数不要超过50字。3.打开启动开关。</span></el-tag>
      </el-form-item>
      <el-form-item label="弹幕内容：">
        <el-input v-model="form.danmakuContent" :rows="3" placeholder="弹幕文字，每段文字使用 | 隔开" type="textarea" @change="dataFormSubmit"></el-input>
      </el-form-item>
      <el-form-item label="弹幕间隔：">
        <div class="flex-colum-center">
          <el-input-number v-model="form.intervalMin" :controls="false" :min="10" @change="dataFormSubmit"></el-input-number>
          <span class="m-5">~</span>
          <el-input-number v-model="form.intervalMax" :controls="false" :min="30" @Change="dataFormSubmit"></el-input-number>
          <div class="m-l-10">
            <el-switch v-model="form.enabledInterval" active-color="#13ce66" active-text="启用" inactive-color="#ff4949" inactive-text="关闭" @change="toggleInterval"></el-switch>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="文字回复：">
        <div class="flex-colum-center">
          <el-switch v-model="form.enabledTextReply" active-color="#13ce66" active-text="启用" inactive-color="#ff4949" inactive-text="关闭" @change="toggleTextReply"></el-switch>
          <div class="m-l-10">
            <el-tag>启用后，当触发关键词回复时，除AI语音回答外，将同时使用文字回答。(仅支持抖音平台)</el-tag>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="网页静音：">
        <el-switch v-model="form.enabledMute" active-color="#13ce66" active-text="静音" inactive-color="#ff4949" inactive-text="恢复声音" @change="toggleMute"></el-switch>
      </el-form-item>
    </el-form>
    <!-- 底部空白区域 -->
    <div class="footer-space">
      <webview
          v-if="form.liveUrl"
          ref="douyinWebview"
          partition="persist:websocket-monitor"
          src=""
          style="width: 100%; min-height: 800px;"
          useragent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
          webpreferences="nodeIntegration=no, contextIsolation=yes"
      ></webview>
    </div>
  </el-main>
</template>

<script>
import {ipcRenderer} from "electron";

const DouyinService = require("@/utils/douyinService");
import {save, getByMerchantId} from '@/api/livePublicScreenConfig'
import {enableTextReply} from "@/utils/douyinUtil";

export default {
  name: 'GrabPublicScreen',
  data() {
    return {
      isCollapsed: false,
      form: {
        liveUrl: '',
        danmakuContent: '',
        intervalMin: 10,
        intervalMax: 30,
        enabledInterval: false,
        enabledTextReply: false,
        enabledMute: false
      },
      douyinService: null,
      douyinWebview: null,
      sendDanmakuInterval: null,
    }
  },
  mounted() {
    this.init()
    ipcRenderer.on('receive-douyin-msg', async (event, data) => {
      for (let item of data) {
        this.handleTextReply(item)
      }
    })
  },
  methods: {
    init() {
      getByMerchantId().then(({data}) => {
        if (data && data.code === 0) {
          this.form.id = data.data.id
          this.form.liveUrl = data.data.liveUrl
          this.form.danmakuContent = data.data.danmakuContent
          this.form.intervalMin = data.data.intervalMin
          this.form.intervalMax = data.data.intervalMax
          this.form.enabledInterval = data.data.enabledInterval === 1
          this.form.enabledTextReply = data.data.enabledTextReply === 1
          this.form.enabledMute = data.data.enabledMute === 1

          if (this.form.enabledInterval) {
            this.handleSendDanmaku()
          }
        }
      })
    },
    dataFormSubmit() {
      save({
        id: this.form.id,
        liveUrl: this.form.liveUrl,
        danmakuContent: this.form.danmakuContent,
        intervalMin: this.form.intervalMin,
        intervalMax: this.form.intervalMax,
        enabledInterval: this.form.enabledInterval ? 1 : 0,
        enabledTextReply: this.form.enabledTextReply ? 1 : 0,
        enabledMute: this.form.enabledMute ? 1 : 0
      }).then(({data}) => {
        if (data && data.code === 0) {
        }
      })
    },
    toggleWindowCollapse() {
      if (!this.isCollapsed) {
        ipcRenderer.invoke("collapse-to-bottom-right").then(res => {
          this.isCollapsed = true
        })
      } else {
        ipcRenderer.invoke("restore-from-bottom-right").then(res => {
          this.isCollapsed = false
        })
      }
    },
    openLiveRoom() {
      if (!this.form.liveUrl) {
        this.$message.error('请输入直播间地址');
        return;
      }
      this.$message({
        message: '正在打开直播间...',
        type: 'success'
      });
      const webview = this.$refs.douyinWebview;
      webview.src = this.form.liveUrl;
      this.douyinService = new DouyinService()

      // 方式1，使用浏览器原生websocket
      // await this.douyinService.init((this.form.liveUrl) => {
      //   try {
      //     console.log(msg);
      //   } catch (err) {
      //     console.log("发生非预期情况 断开socket连接", err);
      //     this.douyinService.destroy();
      //   }
      // });

      // 方式2，使用Electron的浏览器窗口通信（提供对浏览器内核的底层控制能力）
      // 添加事件监听
      webview.addEventListener('dom-ready', () => {
        // 添加调试工具
        // webview.openDevTools();
        webview.setAudioMuted(this.form.enabledMute); // 设置是否静音
      });
      webview.addEventListener('did-finish-load', async () => {
        this.douyinWebview = this.$refs.douyinWebview;
        await this.douyinService.initWebSocketMonitoring(this.douyinWebview, this.form.enabledTextReply);
      })
      webview.addEventListener('did-fail-load', (event) => {
        this.$message.error('直播间加载失败');
      });
    },
    muteWebview(muted) {
      if (this.douyinWebview == null) {
        this.$message.error('请先打开直播间');
        return
      }
      // 调用Electron的API设置静音
      this.douyinWebview.setAudioMuted(muted);
    },
    toggleMute(e) {
      this.form.enabledMute = e
      // 切换静音状态
      this.muteWebview(e);
      this.dataFormSubmit()
    },
    // 开启弹幕时间间隔
    toggleInterval(e) {
      this.form.enabledInterval = e
      this.dataFormSubmit()
      if (e) {
        this.handleSendDanmaku()
      } else {
        // 关闭弹幕时间间隔
        clearTimeout(this.sendDanmakuInterval);
      }
    },
    handleSendDanmaku() {
      // 定时发送弹幕
      this.sendDanmakuInterval = setInterval(() => {
        // 随机选一个
        const randomIndex = Math.floor(Math.random() * this.form.danmakuContent.split('|').length);
        const randomText = this.form.danmakuContent.split('|')[randomIndex];
        this.douyinService.sendDanmaku(randomText, this.douyinWebview);
      }, (Math.floor(Math.random() * (this.form.intervalMax - this.form.intervalMin + 1)) + this.form.intervalMin) * 1000);
    },
    // 开启文字回复
    toggleTextReply(e) {
      this.form.enabledTextReply = e
      this.dataFormSubmit()
      this.handleTextReply()
    },
    handleTextReply(msg) {
      // 是否开启文字回复
      if (this.form.enabledTextReply) {
        enableTextReply(msg, this.douyinWebview)
      }
    }
  },
  beforeDestroy() {
    this.douyinService.cleanup();
    clearTimeout(this.sendDanmakuInterval)
  },
}
</script>

<style lang="scss" scoped>
.main {
  margin-top: 30px;

  .tips-title {
    font-size: 22px;
    font-weight: bold;
  }

  .tips-text {
    color: red;
  }

  .footer-space {
    height: 800px;
    border: 1px solid #008B8B;
  }
}
</style>