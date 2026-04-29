'use strict'

import {app} from 'electron'
import initWindow from './services/windowManager'
import DisableButton from './config/DisableButton'
import electronDevtoolsInstaller, {VUEJS_DEVTOOLS} from 'electron-devtools-installer'
import voiceServiceManager from './services/voiceServiceManager'

const log = require('electron-log')
const path = require('path')
const fs = require('fs')

// 用于标记是否正在退出（防止重复触发清理）
let isQuitting = false

// 1. 生成按日期命名的日志文件名
function getDailyLogFileName() {
    const date = new Date()
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `ai_live_${year}-${month}-${day}.log`
}

// 2. 初始化日志系统
function initDailyLogger() {
    log.initialize({
        preload: true, // 启用预加载脚本注入
        spyRendererConsole: true // 启用渲染进程 console.log 捕获，方便调试
    });

    // 设置日志级别（同时输出到文件和控制台）
    log.transports.file.level = 'info'
    log.transports.console.level = 'debug' // 控制台显示所有日志

    // 确定日志目录（当前应用所在目录下的logs文件夹）
    const logsDir = path.join(process.cwd(), 'logs')

    // 创建目录
    if (!fs.existsSync(logsDir)) {
        fs.mkdirSync(logsDir, {recursive: true})
    }

    // 配置每日日志文件路径
    log.transports.file.resolvePathFn = () => {
        const filename = getDailyLogFileName()
        return path.join(logsDir, filename)
    }
}

// 3. 立即初始化日志系统
initDailyLogger()

function onAppReady() {
    log.info('======== 应用启动 ========')
    log.info('日志系统已初始化', {
        path: log.transports.file.getFile().path,
        level: log.transports.file.level
    })
    initWindow()
    DisableButton.Disablef12()
    if (process.env.NODE_ENV === 'development') {
        electronDevtoolsInstaller(VUEJS_DEVTOOLS)
            .then((name) => console.log(`installed: ${name}`))
            .catch(err => console.log('Unable to install `vue-devtools`: \n', err))
    }
}

//禁止程序多开，此处需要单例锁的同学打开注释即可
// const gotTheLock = app.requestSingleInstanceLock()
// if(!gotTheLock){
//   app.quit()
// }
app.isReady() ? onAppReady() : app.on('ready', onAppReady)
// 解决9.x跨域异常问题
app.commandLine.appendSwitch('disable-features', 'OutOfBlinkCors')

app.on('window-all-closed', () => {
    // 所有平台均为所有窗口关闭就退出软件
    app.quit()
})

// 在应用退出前清理所有服务
app.on('before-quit', async (e) => {
    // 如果已经在清理中，直接返回
    if (isQuitting) {
        return
    }

    // 阻止立即退出，先清理服务
    e.preventDefault()
    isQuitting = true

    log.info('======== 应用正在退出，清理服务... ========')

    try {
        // 停止所有语音服务（VSA、F5TTS、COSYVOICE 等）
        const result = await voiceServiceManager.stopAllServices()
        log.info('所有服务已停止', result)
    } catch (error) {
        log.error('停止服务失败:', error)
    }

    log.info('======== 应用退出完成 ========')

    // 清理完成后真正退出
    app.exit(0)
})

app.on('browser-window-created', () => {
    console.log('window-created')
})

if (process.defaultApp) {
    if (process.argv.length >= 2) {
        app.removeAsDefaultProtocolClient('electron-vue-template')
        console.log('有于框架特殊性开发环境下无法使用')
    }
} else {
    app.setAsDefaultProtocolClient('electron-vue-template')
}
