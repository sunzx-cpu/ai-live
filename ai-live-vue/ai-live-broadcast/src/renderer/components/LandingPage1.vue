<template>
  <div id="wrapper">
    <!--        <img id="logo" :src="logo" alt="electron-vue" />-->
    <main>
      <div class="left-side">
        <span class="title">{{ $t("welcome") }}</span>
        <system-information></system-information>
        <div v-if="textarray.length === 0">
          <span>{{ text }}</span>
        </div>
        <div v-for="(itme, index) in textarray" v-else :key="index">
          <span>{{ itme._id }}</span>
          <span>{{ itme.name }}</span>
          <span>{{ itme.age }}</span>
        </div>
      </div>

      <div class="right-side">
        <div class="doc">
          <div class="title alt">{{ $t("buttonTips") }}</div>
          <el-button round type="primary" @click="open()">{{
              $t("buttons.console")
            }}
          </el-button>
          <el-button round type="primary" @click="CheckUpdate('one')">{{
              $t("buttons.checkUpdate")
            }}
          </el-button>
        </div>
        <div class="doc">
          <el-button round type="primary" @click="CheckUpdate('two')">{{
              $t("buttons.checkUpdate2")
            }}
          </el-button>
          <el-button round type="primary" @click="StartServer">{{
              $t("buttons.startServer")
            }}
          </el-button>
          <el-button round type="primary" @click="StopServer">{{
              $t("buttons.stopServer")
            }}
          </el-button>
          <el-button round type="primary" @click="getMessage">{{
              $t("buttons.viewMessage")
            }}
          </el-button>
        </div>
        <div class="doc">
          <el-button round type="primary" @click="openNewWin">{{
              $t("buttons.openNewWindow")
            }}
          </el-button>
          <el-button round type="primary" @click="openDocument">{{
              $t("buttons.openDocument")
            }}
          </el-button>
          <el-button round type="primary" @click="changeLanguage">{{
              $t("buttons.changeLanguage")
            }}
          </el-button>
        </div>
        <div class="doc">
          <el-pagination :current-page="1" :page-size="100" :page-sizes="[100, 200, 300, 400]"
                         :total="400" layout="total, sizes, prev, pager, next, jumper">
          </el-pagination>
        </div>
      </div>
    </main>
    <el-dialog :before-close="handleClose" :visible.sync="dialogVisible" center title="下载进度" top="45vh" width="14%">
      <div class="conten">
        <el-progress :color="colors" :percentage="percentage" :status="progressStaus" type="circle"></el-progress>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import SystemInformation from "./LandingPage/SystemInformation";
import {message} from "@/api/login";
import {ipcRenderer, shell} from "electron";
const { app } = require('electron');

export default {
  name: "landing-page",
  components: {SystemInformation},
  data: () => ({
    newdata: {
      name: "yyy",
      age: "12",
    },
    logo: require("@/assets/logo.png"),
    textarray: [],
    percentage: 0,
    colors: [
      {color: "#f56c6c", percentage: 20},
      {color: "#e6a23c", percentage: 40},
      {color: "#6f7ad3", percentage: 60},
      {color: "#1989fa", percentage: 80},
      {color: "#5cb87a", percentage: 100},
    ],
    dialogVisible: false,
    progressStaus: null,
    filePath: "",
  }),
  created() {
    console.log("环境打印示例");
    console.log("__lib路径", __lib);
    console.log("环境变量", process.env.userConfig);
    /*ipcRenderer.on("download-progress", (event, arg) => {
      this.percentage = Number(arg);
    });
    ipcRenderer.on("download-error", (event, arg) => {
      if (arg) {
        this.progressStaus = "exception";
        this.percentage = 40;
        this.colors = "#d81e06";
      }
    });
    ipcRenderer.on("download-paused", (event, arg) => {
      if (arg) {
        this.progressStaus = "warning";
        this.$alert("下载由于未知原因被中断！", "提示", {
          confirmButtonText: "重试",
          callback: (action) => {
            ipcRenderer.invoke("satrt-download");
          },
        });
      }
    });
    ipcRenderer.on("download-done", (event, age) => {
      this.filePath = age.filePath;
      this.progressStaus = "success";
      console.log("下载完成啦");
      this.$alert("更新下载完成！", "提示", {
        confirmButtonText: "确定",
        callback: (action) => {
          shell.openPath(this.filePath);
        },
      });
    });*/
    ipcRenderer.on("update-msg", (event, age) => {
      console.log("update-msg", age);
      switch (age.state) {
        case -1:
          const msgdata = {
            title: "发生错误",
            message: age.msg,
          };
          this.dialogVisible = false;
          ipcRenderer.invoke("open-errorbox", msgdata);
          break;
        case 0:
          this.$message("正在检查更新");
          break;
        case 1:
          this.$confirm(`发现新版本${age.msg.version}，当前版本${app.getVersion()}，是否立即下载并更新？`, '提示', {
            confirmButtonText: '立即更新',
            cancelButtonText: '稍后再说',
            type: 'warning'
          }).then(() => {
            this.dialogVisible = true;
            ipcRenderer.invoke("confirm-downloadUpdate")
          })
          break;
        case 2:
          this.$message({type: "success", message: "无新版本"});
          break;
        case 3:
          this.percentage = age.msg.percent.toFixed(1);
          break;
        case 4:
          this.progressStaus = "success";
          this.$confirm(`更新下载完成！是否现在重启应用？`, '提示', {
            confirmButtonText: '立即重启',
            cancelButtonText: '稍后再说',
            type: 'info'
          }).then(() => {
            ipcRenderer.invoke("confirm-update");
          })
          break;

        default:
          break;
      }
    });
    // ipcRenderer.on('hot-update-status', (event, arg) => {
    //   console.log(arg);
    //   if (arg.status === 'finished') {
    //     this.$message({
    //       type: 'success',
    //       message: '热更新成功'
    //     });
    //   }
    // })
  },
  methods: {
    openNewWin() {
      let data = {
        url: "/form/index",
        resizable: true,
      };
      ipcRenderer.invoke("open-win", data);
    },
    openDocument() {
      shell.openExternal("https://zh-sky.gitee.io/electron-vue-template-doc/Overview/#%E5%8A%9F%E8%83%BD")
    },
    getMessage() {
      message().then((res) => {
        this.$alert(res.data, "提示", {
          confirmButtonText: "确定",
        });
      });
    },
    StopServer() {
      ipcRenderer.invoke("stop-server").then((res) => {
        this.$message({
          type: "success",
          message: "已关闭",
        });
      });
    },
    StartServer() {
      ipcRenderer.invoke("statr-server").then((res) => {
        if (res) {
          this.$message({
            type: "success",
            message: res,
          });
        }
      });
    },
    // 获取electron方法
    open() {
    },
    CheckUpdate(data) {
      switch (data) {
        case "one":
          ipcRenderer.invoke("check-update").then((res) => {
            console.log("启动检查");
          });

          break;
        case "two":
          ipcRenderer.invoke("start-download").then(() => {
            this.dialogVisible = true;
          });

          break;

        default:
          break;
      }
    },
    handleClose() {
      this.dialogVisible = false;
    },
    changeLanguage() {
      let lang = this.$i18n.locale === "zh-CN" ? "en" : "zh-CN";
      this.$i18n.locale = lang;
    },
  },
  destroyed() {
    console.log("销毁了哦");
    ipcRenderer.removeAllListeners("confirm-message");
    ipcRenderer.removeAllListeners("download-done");
    ipcRenderer.removeAllListeners("download-paused");
    ipcRenderer.removeAllListeners("confirm-stop");
    ipcRenderer.removeAllListeners("confirm-start");
    ipcRenderer.removeAllListeners("confirm-download");
    ipcRenderer.removeAllListeners("download-progress");
    ipcRenderer.removeAllListeners("download-error");
    ipcRenderer.removeAllListeners("update-msg");
  },
  computed: {
    text() {
      return this.$i18n.t("waitDataLoading");
    },
  },
};
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: "Source Sans Pro", sans-serif;
}

#wrapper {
  padding: 60px 80px;
}

#logo {
  height: auto;
  margin-bottom: 20px;
  width: 420px;
}

main {
  display: flex;
  justify-content: space-between;
}

main > div {
  flex-basis: 50%;
}

.left-side {
  display: flex;
  flex-direction: column;
}

.welcome {
  color: #555;
  font-size: 23px;
  margin-bottom: 10px;
}

.title {
  color: #2c3e50;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 6px;
}

.title.alt {
  font-size: 18px;
  margin-bottom: 10px;
}

.doc {
  margin-bottom: 10px;
}

.doc p {
  color: black;
  margin-bottom: 10px;
}

.doc .el-button {
  margin-top: 10px;
  margin-right: 10px;
}

.doc .el-button + .el-button {
  margin-left: 0;
}

.conten {
  text-align: center;
}
</style>
