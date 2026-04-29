import {BrowserWindow, ipcMain} from 'electron'


class IpcDouyin {
    constructor() {
    }

    initDouyinService() {
        // 转发抖音消息
        ipcMain.on('send-douyin-msg', (event, data) => {
            // 获取所有窗口并转发消息
            const windows = BrowserWindow.getAllWindows();
            windows.forEach(win => {
                if (win.webContents && !win.webContents.isDestroyed()) {
                    if (data.length > 0) {
                        win.webContents.send('receive-douyin-msg', data);
                    }
                }
            });
        });

        // 转发GPT配置消息
        ipcMain.on('send-gpt-config', (event, data) => {
            // 获取所有窗口并转发消息
            const windows = BrowserWindow.getAllWindows();
            windows.forEach(win => {
                if (win.webContents && !win.webContents.isDestroyed()) {
                    win.webContents.send('receive-gpt-config', data);
                }
            });
        });

        // 转发临时话术播放内容
        ipcMain.on('send-temporary-speech', (event, data) => {
            // 获取所有窗口并转发消息
            const windows = BrowserWindow.getAllWindows();
            windows.forEach(win => {
                if (win.webContents && !win.webContents.isDestroyed()) {
                    win.webContents.send('receive-temporary-speech', data);
                }
            });
        });

        // 转发打断并立即互动
        ipcMain.on('send-interact-now', (event, data) => {
            // 获取所有窗口并转发消息
            const windows = BrowserWindow.getAllWindows();
            windows.forEach(win => {
                if (win.webContents && !win.webContents.isDestroyed()) {
                    win.webContents.send('receive-interact-now');
                }
            });
        });
    }
}

const ipcDouyin = new IpcDouyin()
export default ipcDouyin