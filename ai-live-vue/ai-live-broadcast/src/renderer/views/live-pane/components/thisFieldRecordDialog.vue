<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" title="本场直播间公屏用户发言记录" width="60%">
    <el-divider content-position="left">本场用户发言记录，您可以进行梳理后，进行关键词互动的优化。
      <el-button class="m-l-10" size="mini" type="text" @click="clearMessages">[清空]</el-button>
    </el-divider>
    <el-input v-model="computeUserMessages" :rows="20" placeholder="本场用户发言记录，您可以进行梳理后，进行关键词互动优化。" type="textarea"></el-input>
    <el-divider content-position="left">以上仅显示近1000条记录，如需查看更多或历史记录，请进入程序安装目录的logs文件夹中查看相应记录文件。
      <el-button class="m-l-10" size="mini" type="text" @click="openLogsFolder">[打开日志文件夹]</el-button>
    </el-divider>
  </el-dialog>
</template>

<script>
import {ipcRenderer} from "electron";
const moment = require('moment');
const path = require('path');
const fs = require('fs');
import { filterMsg } from "@/utils/douyinUtil";

export default {
  name: "ThisFieldRecordDialog",
  data() {
    return {
      userSpeeches: [],
      dialogVisible: false
    }
  },
  computed: {
    computeUserMessages() {
      if (this.userSpeeches.length <= 1000) {
        if (this.userSpeeches) {
          let str = "";
          this.userSpeeches.forEach(message => {
            const now = moment();
            const timestamp = now.format('YYYY-MM-DD HH:mm:ss');
            str += `${timestamp} [用户发言] ${message.nickname}: ${message.content}\n`;
            this.generateUserMessagesFile(str);
          });
          return str;
        }
      }
    }
  },
  mounted() {
    ipcRenderer.on('receive-douyin-msg', async (event, data) => {
      for (let item of data) {
        // 如果存在屏蔽自己&敏感词配置，则过滤掉该消息
        item = await filterMsg(item);
        switch (item.type) {
          case 'chat':
            this.userSpeeches.push(item);
            break;
        }
      }
    });
  },
  methods: {
    // 生成用户发言记录txt文件
    generateUserMessagesFile(message) {
      const logPath = path.join(process.cwd(), 'logs', this.getFileName());
      // 确保目录存在
      const logDir = path.dirname(logPath);
      if (!fs.existsSync(logDir)) {
        fs.mkdirSync(logDir, {recursive: true});
      }
      // 写入文件
      fs.writeFile(logPath, message, (err) => {
        if (err) {
          this.$message.error('写入文件失败，请重试');
        }
      });
    },
    getFileName() {
      const now = moment();
      return `用户发言记录_${now.format('YYYY-MM-DD')}.txt`;
    },
    clearMessages() {
      this.$emit("clearMessages");
    },
    openLogsFolder() {
      this.$emit("openLogsFolder");
    },
    callback() {
      this.dialogVisible = false
      this.$emit("close")
    },
  }
}
</script>

<style scoped>
</style>