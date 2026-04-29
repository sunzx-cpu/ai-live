<!--开播控制台-->
<template>
  <div class="container-left live-pane-container">
    <!-- 顶部区域 -->
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="开播控制台" name="first">

        <div class="live-pane-content">
          <!-- 左侧区域 -->
          <div class="live-pane-left">
            <!-- 顶部脚本管理区 -->
            <LivePaneHeader ref="livePaneHeader"/>

            <!-- 直播控制按钮区 -->
            <LivePaneControlButtons ref="livePaneControlButtons"/>

            <!-- 平台检测防劫区 -->
            <!-- <LivePanePlatformCheck /> -->

            <!-- 片段列表区 -->
            <LivePaneSegmentTable ref="livePaneSegmentTable" class="live-pane-segment-table"/>
          </div>

          <!-- 右侧区域 -->
          <div class="live-pane-right">
            <div class="live-pane-side">
              <!-- 当前播放片段内容区 -->
              <LivePaneCurrentSegment ref="livePaneCurrentSegment"/>
              <!-- 直播间公屏区 -->
              <LivePanePublicScreen ref="livePanePublicScreen"/>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 调试面板 -->
    <DebugPanel ref="debugPanel"/>
  </div>
</template>
<script>
import LivePaneHeader from './LivePaneHeader.vue'
import LivePaneControlButtons from './LivePaneControlButtons.vue'
import LivePanePlatformCheck from './LivePanePlatformCheck.vue'
import LivePaneSegmentTable from './LivePaneSegmentTable.vue'
import LivePaneCurrentSegment from './LivePaneCurrentSegment.vue'
import LivePanePublicScreen from './LivePanePublicScreen.vue'
import DebugPanel from './components/debugPanel.vue'
import WebSocketUtil from "@/utils/websocket";

export default {
  name: 'LivePane',
  data() {
    return {
      activeName: 'first',
      wsManager: null,
      messages: []
    }
  },
  components: {
    LivePaneHeader,
    LivePaneControlButtons,
    LivePanePlatformCheck,
    LivePaneSegmentTable,
    LivePaneCurrentSegment,
    LivePanePublicScreen,
    DebugPanel
  },
  mounted() {
    this.$refs.livePaneControlButtons.init(this)
    this.$refs.livePaneSegmentTable.init(this)
    this.$refs.livePaneCurrentSegment.init(this)
    this.$refs.livePaneHeader.init(this)
    this.$refs.livePanePublicScreen.init(this)
    this.initWebSocket();
  },
  beforeDestroy() {
    if (this.wsManager) {
      this.wsManager.close();
    }
  },
  methods: {
    handleClick(e) {
      // Handle tab click
    },
    initWebSocket() {
      this.wsManager = new WebSocketUtil({
        url: process.env.userConfig.WEBSOCKET_HOST,
        userId: localStorage.getItem("userId"),
        heartbeatIntervalTime: 10000,
        maxReconnectAttempts: 5,
      });

      // 建立连接
      this.wsManager.connect();
    }
  }
}
</script>
<style lang="scss" scoped>
.live-pane-container {
  min-height: 100vh;
  width: 100%;
}

.live-pane-content {
  display: flex;
  flex-direction: row;
  width: 100%;
}

.live-pane-left {
  width: 75%;
}

.live-pane-right {
  width: 25%;
  display: flex;
  flex-direction: column;
}

.live-pane-segment-table {
  margin-bottom: 16px;
}

.live-pane-side {
  margin-right: 15px;
}
</style>
