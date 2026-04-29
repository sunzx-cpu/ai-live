import { BrowserWindow, Menu, app, dialog } from 'electron'
import { platform } from "os"
import menuconfig from '../config/menu'
import { openDevTools, IsUseSysTitle, UseStartupChart, autoOpenDevTools } from '../config/const'
import setIpc from './ipcMain'
import { winURL, loadingURL } from '../config/StaticPath'

var loadWindow = null
var mainWindow = null
setIpc.Mainfunc(IsUseSysTitle)

function createMainWindow() {
  /**
   * Initial window options
   */
  mainWindow = new BrowserWindow({
    height: 1080,
    useContentSize: true,
    width: 1920,
    minWidth: 1366,
    show: false,
    frame: IsUseSysTitle,
    titleBarStyle: platform().includes('win32') ? 'default' : 'hidden',
    webPreferences: {
      contextIsolation: false,
      nodeIntegration: true,
      webSecurity: false,
      // 始终允许开发者工具（可以通过菜单或F12手动打开）
      devTools: openDevTools,
      // 启用webview标签（用于商品知识库采集）
      webviewTag: true,
      // 在macos中启用橡皮动画
      scrollBounce: process.platform === 'darwin'
    }
  })
  require('@electron/remote/main').initialize();
  require('@electron/remote/main').enable(mainWindow.webContents);

  // 始终添加开发者设置菜单
  menuconfig.push({
    label: '开发者设置',
    submenu: [{
      label: '切换到开发者模式',
      accelerator: 'CmdOrCtrl+I',
      role: 'toggledevtools'
    }]
  })
  // 载入菜单
  const menu = Menu.buildFromTemplate(menuconfig)
  Menu.setApplicationMenu(menu)
  mainWindow.loadURL(winURL)

  mainWindow.webContents.once('dom-ready', () => {
    mainWindow.show()
    // 🎯 只在开发构建版本自动打开开发者工具
    // 生产版本可以通过 Ctrl+I 或菜单手动打开
    if (autoOpenDevTools) {
      console.log('[WindowManager] 开发构建版本，自动打开开发者工具')
      mainWindow.webContents.openDevTools()
    } else {
      console.log('[WindowManager] 生产版本，不自动打开开发者工具（可按 Ctrl+I 手动打开）')
    }
    if (UseStartupChart) loadWindow.destroy()
  })
  mainWindow.on('maximize', () => {
    mainWindow.webContents.send("w-max", true)
  })
  mainWindow.on('unmaximize', () => {
    mainWindow.webContents.send("w-max", false)
  })

  // 🎯 窗口关闭前确认
  mainWindow.on('close', (e) => {
    // 阻止默认的关闭行为
    e.preventDefault()

    // 显示确认对话框
    dialog.showMessageBox(mainWindow, {
      type: 'question',
      buttons: ['取消', '确定'],
      defaultId: 0,
      cancelId: 0,
      title: '确认关闭',
      message: '即将关闭整个程序，是否继续？',
      detail: '关闭后将停止所有服务（VSA、语音服务等）和正在进行的任务'
    }).then(result => {
      if (result.response === 1) {
        // 用户点击了"确定"按钮
        // 销毁窗口并退出应用（会触发 before-quit 事件清理服务）
        mainWindow.destroy()
        app.quit()
      }
      // 如果点击"取消"，什么都不做，窗口保持打开
    })
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

function loadingWindow() {
  loadWindow = new BrowserWindow({
    width: 400,
    height: 600,
    frame: false,
    backgroundColor: '#222',
    skipTaskbar: true,
    transparent: true,
    resizable: false,
    webPreferences: {
      experimentalFeatures: true
    }
  })

  loadWindow.loadURL(loadingURL)

  loadWindow.show()

  setTimeout(() => {
    createMainWindow()
  }, 2000)

  loadWindow.on('closed', () => {
    loadWindow = null
  })
}

function initWindow() {
  if (UseStartupChart) {
    return loadingWindow()
  } else {
    return createMainWindow()
  }
}
export default initWindow
