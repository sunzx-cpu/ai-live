import { globalShortcut, BrowserWindow } from 'electron'
import { DisableF12 } from "./const"

export default {
  Disablef12() {
    // 注册 Ctrl+Shift+I 打开开发者工具（生产环境也可用）
    globalShortcut.register('CommandOrControl+Shift+I', () => {
      const win = BrowserWindow.getFocusedWindow()
      if (win) {
        win.webContents.toggleDevTools()
      }
    })

    // 注册 F12 打开开发者工具（生产环境也可用）
    globalShortcut.register('F12', () => {
      const win = BrowserWindow.getFocusedWindow()
      if (win) {
        win.webContents.toggleDevTools()
      }
    })

    console.log('[DevTools] 已注册快捷键: F12 和 Ctrl+Shift+I')
  }
}
