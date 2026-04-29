<template>
  <div>
    <el-card shadow="hover">
      <div slot="header">
        <span>直播间公屏</span>
        <el-button size="mini" type="text" @click="openGrabPublicScreen">[抓取直播间公屏]</el-button>
        <el-button size="mini" type="text" @click="openShield">[屏蔽自己&敏感词]</el-button>

        <el-row :gutter="8" align="middle">
          <el-col class="el-row--flex public-screen-live">
            <el-tag size="small" @click="getPlatformLive()"><span class="underline-text">[如何获取各平台直播间？]</span></el-tag>
            <el-tag size="small">抖、快、视、拼、红、美、淘、京、TK</el-tag>
          </el-col>
          <el-col class="public-screen-setting">
            <el-button round size="mini" type="success" @click="keywordInteractionSettings">[关键词互动设置]</el-button>
            <el-button round size="mini" type="success" @click="behavioralInteractionSettings">[行为互动设置]</el-button>
          </el-col>
        </el-row>

        <el-divider content-position="left">GPT互动，优先级低于关键词互动
          <el-button class="m-l-10" size="mini" type="text" @click="gptModelHandle">[GPT设置]</el-button>
        </el-divider>

        <el-row :gutter="8" align="middle">
          <el-col>
            <el-checkbox v-model="dataForm.enableGptInteraction" @change="saveGptConfig">启用GPT互动</el-checkbox>
            <el-checkbox v-model="dataForm.enableKnowledge" @change="saveGptConfig">启用知识库</el-checkbox>
          </el-col>
          <el-col>
            <el-checkbox v-model="dataForm.repeatUserStatements" :disabled="!dataForm.enableGptInteraction" @change="saveGptConfig">重复用户发言</el-checkbox>
            <el-checkbox v-model="dataForm.clickUsername" :disabled="!dataForm.enableGptInteraction" @change="saveGptConfig">点用户名字</el-checkbox>
          </el-col>
          <el-col>
            <el-checkbox v-model="dataForm.interactNow" @change="saveGptConfig">打断并立即互动（不勾选则说完当前一句再互动）</el-checkbox>
          </el-col>
        </el-row>
      </div>

      <el-row :gutter="8">
        <!-- 用户发言部分 -->
        <el-col :span="24">
          <el-divider class="section-divider" content-position="left">用户发言
            <el-button class="m-l-10" size="mini" type="text" @click="openThisFieldRecord">[本场记录]</el-button>
            <el-button class="m-l-10" size="mini" type="text" @click="openLogsFolder">[打开日志]</el-button>
          </el-divider>
          <div ref="speechList" class="content-list">
            <div v-for="(item) in displayUserSpeeches" :key="item.id" class="list-item">
              [用户发言] {{ item.nickname }}: {{ item.content }}
            </div>
          </div>
        </el-col>
        <!-- 用户行为：进入直播间 -->
        <el-col :span="24">
          <el-divider class="section-divider" content-position="left">用户行为:进入直播间</el-divider>
          <div ref="liveList" class="content-list">
            <div v-for="(item) in displayLiveBehaviors" :key="item.id" class="list-item">
              [进入直播间] {{ item.nickname }}: {{ item.content }}
            </div>
          </div>
        </el-col>
        <!-- 用户行为：其它 -->
        <el-col :span="24">
          <el-divider class="section-divider" content-position="left">用户行为:其它</el-divider>
          <div ref="otherList" class="content-list">
            <div v-for="(item) in displayOtherBehaviors" :key="item.id" class="list-item">
              [{{ item.action }}] {{ item.content }}
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!--   屏蔽自己&敏感词   -->
    <ShieldDialog ref="shieldDialog"></ShieldDialog>
    <!--  本场记录  -->
    <ThisFieldRecordDialog ref="thisFieldRecordDialog" @clearMessages="clearMessages" @openLogsFolder="openLogsFolder"></ThisFieldRecordDialog>
    <!--  GPT大模型  -->
    <GptModelDialog
        ref="gptModelDialog"
        :visible="gptModelDialogVisible"
        @close="gptModelDialogVisible = false">
    </GptModelDialog>
  </div>
</template>
<script>
import {ipcRenderer} from "electron";
import ThisFieldRecordDialog from "./components/thisFieldRecordDialog.vue";
import ShieldDialog from "./components/shieldDialog.vue";
import GptModelDialog from "@/views/live-pane/components/gptModelDialog.vue";
import {filterMsg} from "@/utils/douyinUtil";
import {save, getByMerchantId} from '@/api/livePublicScreenConfig'
const DouyinService = require("@/utils/douyinService");
const gptService = require('@/utils/gptService')

export default {
  name: 'LivePanePublicScreen',
  components: {
    ShieldDialog,
    ThisFieldRecordDialog,
    GptModelDialog
  },
  data() {
    return {
      gptModelDialogVisible: false,
      dataForm: {
        enableGptInteraction: false,
        enableKnowledge: false,
        repeatUserStatements: false,
        clickUsername: false,
        interactNow: true
      },
      maxDisplayItems: 5, // 最大显示条数
      // 用户发言数据
      userSpeeches: [],
      // 进入直播间行为
      userBehaviorsLive: [],
      // 其他行为
      userBehaviorsOther: [],
      textReplyEnabled: false,
      liveContext: null
    }
  },
  mounted() {
    this.init()
    // 初始化时滚动到底部
    this.$nextTick(() => {
      this.scrollToBottom(this.$refs.speechList);
      this.scrollToBottom(this.$refs.liveList);
      this.scrollToBottom(this.$refs.otherList);
    });
    this.listenLiveBehaviorsData();
  },
  computed: {
    // 显示的用户发言（只显示最后maxDisplayItems条）
    displayUserSpeeches() {
      return this.userSpeeches.slice(-this.maxDisplayItems);
    },
    // 显示的进入直播间行为
    displayLiveBehaviors() {
      return this.userBehaviorsLive.slice(-this.maxDisplayItems);
    },
    // 显示的其他行为
    displayOtherBehaviors() {
      return this.userBehaviorsOther.slice(-this.maxDisplayItems);
    }
  },
  watch: {
    // 监听用户发言数据变化，自动滚动到底部
    userSpeeches: {
      handler() {
        this.$nextTick(() => {
          this.scrollToBottom(this.$refs.speechList);
        });
      },
      deep: true
    },
    // 监听进入直播间数据变化
    userBehaviorsLive: {
      handler() {
        this.$nextTick(() => {
          this.scrollToBottom(this.$refs.liveList);
        });
      },
      deep: true
    },
    // 监听其他行为数据变化
    userBehaviorsOther: {
      handler() {
        this.$nextTick(() => {
          this.scrollToBottom(this.$refs.otherList);
        });
      },
      deep: true
    }
  },
  methods: {
    init(liveContext) {
      this.liveContext = liveContext
      // 插入临时话术到播放列表
      ipcRenderer.on("receive-temporary-speech", async (event, data) => {
        await this.liveContext.$refs.livePaneSegmentTable.insertTemporarySpeech(data, 'keyword-interaction')
      })
      // 打断并立即互动
      ipcRenderer.on("receive-interact-now", async (event, data) => {
        // 打断并立即互动
        const word = await this.liveContext.$refs.livePaneSegmentTable.stopPlayTemporarySpeech()
        // 调用 GPT 生成回复
        const result1 = await gptService.replyToKeyword(word)
        // 发送弹幕
        const douyinService = new DouyinService()
        await douyinService.sendDanmaku(result1.content)
      })
      ipcRenderer.on("receive-gpt-config", async (event, data) => {
        this.dataForm.enableGptInteraction = data.enableGptInteraction
        this.dataForm.enableKnowledge = data.enableKnowledge
        this.dataForm.repeatUserStatements = data.repeatUserStatements
        this.dataForm.clickUsername = data.clickUsername
        this.saveGptConfig()
      })
      getByMerchantId().then(({data}) => {
        if (data && data.code === 0) {
          this.dataForm.id = data.data.id
          this.dataForm.enableGptInteraction = data.data.enableGptInteraction === 1
          this.dataForm.enableKnowledge = data.data.enableKnowledge === 1
          this.dataForm.repeatUserStatements = data.data.repeatUserStatements === 1
          this.dataForm.clickUsername = data.data.clickUsername === 1
          this.dataForm.interactNow = data.data.interactNow === 1
        }
      })
    },
    saveGptConfig() {
      save({
        id: this.dataForm.id,
        enableGptInteraction: this.dataForm.enableGptInteraction ? 1 : 0,
        enableKnowledge: this.dataForm.enableKnowledge ? 1 : 0,
        repeatUserStatements: this.dataForm.repeatUserStatements ? 1 : 0,
        clickUsername: this.dataForm.clickUsername ? 1 : 0,
        interactNow: this.dataForm.interactNow ? 1 : 0,
      }).then(async ({data}) => {
        if (data && data.code === 0) {
          localStorage.setItem('gptInteractionEnabled', JSON.stringify(this.dataForm.enableGptInteraction))
          localStorage.setItem('gptRepeatUserContent', JSON.stringify(this.dataForm.repeatUserStatements))
          localStorage.setItem('gptSayUserName', JSON.stringify(this.dataForm.repeatUserStatements))
          localStorage.setItem('gptKnowledgeEnabled', JSON.stringify(this.dataForm.enableKnowledge))
          localStorage.setItem('gptInteractNow', JSON.stringify(this.dataForm.interactNow))
        }
      })
    },
    // 抓取公屏
    openGrabPublicScreen() {
      let data = {
        url: "/grabPublicScreen",
        resizable: true,
        alwaysOnTop: true,
        webview: true
      };
      ipcRenderer.invoke("open-win", data);
    },
    // 屏蔽自己&敏感词
    openShield() {
      this.$refs.shieldDialog.dialogVisible = true
      this.$refs.shieldDialog.init()
    },
    // 关键词互动设置
    keywordInteractionSettings() {
      let data = {
        url: "/live-interact-keyword",
        resizable: true,
        isExternalUrl: true
      };
      ipcRenderer.invoke("open-win", data);
    },
    // 行为互动设置
    behavioralInteractionSettings() {
      let data = {
        url: "/live-interact-action",
        resizable: true,
        isExternalUrl: true
      };
      ipcRenderer.invoke("open-win", data);
    },
    // GPT设置
    gptModelHandle() {
      this.gptModelDialogVisible = true
    },
    // 本场记录
    openThisFieldRecord() {
      this.$refs.thisFieldRecordDialog.dialogVisible = true
    },
    // 清空消息
    clearMessages() {
      this.$confirm('确定要清空所有用户发言吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.userSpeeches = []
        this.$message.success('已清空用户发言')
      }).catch(() => {
        // 用户取消
      })
    },
    // 打开日志窗口
    async openLogsFolder() {
      try {
        this.$log.info("打开日志")
        await ipcRenderer.invoke('open-logs-folder')
      } catch (err) {
        console.error('无法打开日志目录:', err)
      }
    },
    // 滚动到底部（显示最新数据）
    scrollToBottom(element) {
      if (element) {
        element.scrollTop = element.scrollHeight;
      }
    },
    // 监听处理直播间消息
    listenLiveBehaviorsData() {
      ipcRenderer.on('receive-douyin-msg', async (event, data) => {
        for (let item of data) {
          // 如果存在屏蔽自己&敏感词配置，则过滤掉该消息
          item = await filterMsg(item);
          switch (item.type) {
            case 'member':
              this.userBehaviorsLive.push(item);
              this.userBehaviorsLive = this.oldArrayToNewArray(this.userBehaviorsLive);
              break;
            case 'social':
              item.action = '其他消息'
              this.userBehaviorsOther.push(item);
              this.userBehaviorsOther = this.oldArrayToNewArray(this.userBehaviorsOther);
              break;
            case 'chat':
              this.userSpeeches.push(item);
              this.userSpeeches = this.oldArrayToNewArray(this.userSpeeches);
              break;
            case 'like':
              item.action = '点赞消息'
              this.userBehaviorsOther.push(item);
              this.userBehaviorsOther = this.oldArrayToNewArray(this.userBehaviorsOther);
              break;
            case 'gift':
              item.action = '礼物消息'
              this.userBehaviorsOther.push(item);
              this.userBehaviorsOther = this.oldArrayToNewArray(this.userBehaviorsOther);
              break;
          }
        }
      });
    },
    // 如果超过最大显示数量，移除最旧的数据
    oldArrayToNewArray(array) {
      if (array.length > this.maxDisplayItems * 2) {
        array = array.slice(-this.maxDisplayItems * 2);
      }
      return array;
    },
  }
}
</script>
<style lang="scss" scoped>
.public-screen-live {
  margin-left: 20px;

  .underline-text {
    text-decoration: underline;
    text-decoration-color: #409EFF;
    cursor: pointer;
  }
}

.public-screen-setting {
  margin-top: 20px;
}

.section-divider {
  margin: 16px 0 8px 0;
}

.divider-title {
  font-weight: 600;
  color: #303133;
}

.divider-btn {
  margin-left: 10px;
  color: #409eff;
  padding: 2px 5px;
  font-size: 12px;
}

.content-list {
  margin: 15px 10px 0 10px;
  overflow-y: hidden;
}

.scroll-container {
  display: flex;
  flex-direction: column-reverse; /* 新消息从底部向上滚动 */
}

.list-item {
  padding: 2px 0;
  border-bottom: 1px solid #DCDFE6;
  font-size: 13px;
  color: #606266;
  line-height: 1.3;
  min-height: 20px;
  flex-shrink: 0;
}

/* 滚动条样式 */
.content-list::-webkit-scrollbar {
  width: 4px;
}

.content-list::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 2px;
}
</style>
