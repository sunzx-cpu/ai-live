import {app, BrowserWindow, dialog, ipcMain, screen, shell} from 'electron'
import Server from '../server/index'
import {winURL} from '../config/StaticPath'
import downloadFile from './downloadFile'
import Update from './checkupdate'
import {updater} from './HotUpdater'
import path from 'path'
import fs from 'fs'
import voiceServiceManager from './voiceServiceManager'
import ipcDouyin from './ipcDouyin'
import audioStreamService from './audioStreamService'  // 导入音频流推送服务

// 日志
const log = require('electron-log')
let tokenMap = new Map();
// 存储窗口原始状态
let windowStates = new Map();
// 动态存储主播名称到模型ID的映射（根据VSA加载顺序）
let speakerIdMap = {}

export default {
    Mainfunc(IsUseSysTitle) {
        const allUpdater = new Update();

        // 初始化音频流服务
        audioStreamService.init(ipcMain)

        ipcMain.handle('IsUseSysTitle', async () => {
            return IsUseSysTitle
        })

        ipcMain.handle('windows-mini', (event, args) => {
            BrowserWindow.fromWebContents(event.sender)?.minimize()
        })

        ipcMain.handle('window-max', async (event, args) => {
            if (BrowserWindow.fromWebContents(event.sender)?.isMaximized()) {
                BrowserWindow.fromWebContents(event.sender)?.unmaximize()
                return {status: false}
            } else {
                BrowserWindow.fromWebContents(event.sender)?.maximize()
                return {status: true}
            }
        })

        ipcMain.handle('window-close', (event, args) => {
            const mainWindow = BrowserWindow.fromWebContents(event.sender)
            if (windowStates.has(mainWindow.id)) {
                windowStates.delete(mainWindow.id);
            }
            mainWindow?.close()
        })

        ipcMain.handle('start-download', (event, msg) => {
            downloadFile.download(BrowserWindow.fromWebContents(event.sender), msg.downloadUrL)
        })

        ipcMain.handle('check-update', (event, args) => {
            allUpdater.checkUpdate(BrowserWindow.fromWebContents(event.sender))
        })

        ipcMain.handle('confirm-downloadUpdate', (event, args) => {
            allUpdater.confirmDownloadUpdate(BrowserWindow.fromWebContents(event.sender))
        })

        ipcMain.handle('confirm-update', () => {
            allUpdater.quitInstall()
        })

        ipcMain.handle('hot-update', (event, arg) => {
            updater(BrowserWindow.fromWebContents(event.sender))
        })

        ipcMain.handle('open-messagebox', async (event, arg) => {
            const res = await dialog.showMessageBox(BrowserWindow.fromWebContents(event.sender), {
                type: arg.type || 'info',
                title: arg.title || '',
                buttons: arg.buttons || [],
                message: arg.message || '',
                noLink: arg.noLink || true
            })
            return res
        })
        ipcMain.handle('open-errorbox', (event, arg) => {
            dialog.showErrorBox(
                arg.title,
                arg.message
            )
        })
        ipcMain.handle('statr-server', async () => {
            try {
                const serveStatus = await Server.StatrServer()
                return serveStatus
            } catch (error) {
                dialog.showErrorBox(
                    '错误',
                    error
                )
            }
        })
        ipcMain.handle('stop-server', async (event, arg) => {
            try {
                const serveStatus = await Server.StopServer()
                return serveStatus
            } catch (error) {
                dialog.showErrorBox(
                    '错误',
                    error
                )
            }
        })
        let childWin = null;
        let cidArray = [];
        ipcMain.handle('open-win', (event, arg) => {
            let cidJson = {id: null, url: ''}
            let data = cidArray.filter((currentValue) => {
                if (currentValue.url === arg.url) {
                    return currentValue
                }
            })
            if (data.length > 0) {
                //获取当前窗口
                let currentWindow = BrowserWindow.fromId(data[0].id)
                //聚焦窗口
                currentWindow.focus();
            } else {
                //获取主窗口ID
                let parentID = event.sender.id
                // 是否是第三方地址
                let isExternalUrl = arg?.isExternalUrl ?? false
                // 是否是抓取公屏窗口（需要加载外部直播间网站）
                let isGrabPublicScreen = arg?.url === '/grabPublicScreen'

                //创建窗口
                childWin = new BrowserWindow({
                    width: arg?.width || 1024,
                    height: arg?.height || 796,
                    //width 和 height 将设置为 web 页面的尺寸(译注: 不包含边框), 这意味着窗口的实际尺寸将包括窗口边框的大小，稍微会大一点。
                    useContentSize: true,
                    //自动隐藏菜单栏，除非按了Alt键。
                    autoHideMenuBar: true,
                    //窗口大小是否可调整
                    resizable: arg?.resizable ?? false,
                    //窗口的最小高度
                    minWidth: arg?.minWidth || 842,
                    show: arg?.show ?? false,
                    //窗口透明度
                    opacity: arg?.opacity || 1.0,
                    //当前窗口的父窗口ID
                    parent: parentID,
                    frame: isExternalUrl ? true : IsUseSysTitle,
                    // 设置窗口始终置顶
                    alwaysOnTop: arg?.alwaysOnTop ?? false,
                    webPreferences: {
                        nodeIntegration: true,
                        // ✅ 抓取公屏窗口启用 webSecurity，避免安全警告且能正常加载 HTTPS 直播间
                        webSecurity: isGrabPublicScreen ? true : false,
                        //使用webview标签 必须开启
                        webviewTag: isGrabPublicScreen ? true : (arg?.webview ?? false),
                        // 始终允许开发者工具
                        devTools: true,
                        // 在macos中启用橡皮动画
                        scrollBounce: process.platform === 'darwin',
                        // 临时修复打开新窗口报错
                        contextIsolation: false,
                        enableRemoteModules: true
                    }
                })
                require('@electron/remote/main').enable(childWin.webContents);
                if (isExternalUrl) {
                    const token = tokenMap.get('token');
                    childWin.loadURL(process.env.userConfig.USER_HOST + `#${arg.url}${token ? '?token=' + token : ''}`)
                } else {
                    childWin.loadURL(winURL + `#${arg.url}`)
                }
                cidJson.id = childWin?.id
                cidJson.url = arg.url
                cidArray.push(cidJson)
                childWin.webContents.once('dom-ready', () => {
                    childWin.show()
                    childWin.webContents.send('send-data', arg.sendData)
                    if (arg.IsPay) {
                        // 检查支付时候自动关闭小窗口
                        const testUrl = setInterval(() => {
                            const Url = childWin.webContents.getURL()
                            if (Url.includes(arg.PayUrl)) {
                                childWin.close()
                            }
                        }, 1200)
                        childWin.on('close', () => {
                            clearInterval(testUrl)
                        })
                    }
                })
                childWin.on('closed', () => {
                    childWin = null
                    let index = cidArray.indexOf(cidJson)
                    if (index > -1) {
                        cidArray.splice(index, 1);
                    }
                })
            }
            childWin.on('maximize', () => {
                if (cidJson.id != null) {
                    BrowserWindow.fromId(cidJson.id).webContents.send("w-max", true)
                }
            })
            childWin.on('unmaximize', () => {
                if (cidJson.id != null) {
                    BrowserWindow.fromId(cidJson.id).webContents.send("w-max", false)
                }
            })
        })

        // 保存token
        ipcMain.handle('save-token', (event, token) => {
            tokenMap.set('token', token);
        });

        // 打开日志
        ipcMain.handle('open-logs-folder', () => {
            const logPath = log.transports.file.getFile().path
            const logDir = path.dirname(logPath)
            return shell.openPath(logDir)
        })

        // 收缩窗口到右下角
        ipcMain.handle('collapse-to-bottom-right', (event) => {
            const mainWindow = BrowserWindow.fromWebContents(event.sender);
            // 获取窗口ID作为存储的key
            const windowId = mainWindow.id;

            // 保存窗口原始状态（只在第一次收缩时保存）
            if (!windowStates.has(windowId)) {
                const bounds = mainWindow.getBounds();
                const isMaximized = mainWindow.isMaximized();

                windowStates.set(windowId, {
                    bounds: bounds,
                    isMaximized: isMaximized,
                    originalWidth: bounds.width,
                    originalHeight: bounds.height,
                    originalX: bounds.x,
                    originalY: bounds.y
                });
            }

            // 获取主显示器的可用工作区大小
            const {workAreaSize} = screen.getPrimaryDisplay();

            // 定义收缩后的窗口尺寸
            const collapsedWidth = 200;
            const collapsedHeight = 150;

            // 计算窗口在右下角时，其左上角的坐标
            const x = workAreaSize.width - collapsedWidth;
            const y = workAreaSize.height - collapsedHeight;

            // 调整窗口大小和位置
            mainWindow.setBounds({
                x: x,
                y: y,
                width: collapsedWidth,
                height: collapsedHeight
            });
        });

        // 还原窗口到原始状态
        ipcMain.handle('restore-from-bottom-right', (event) => {
            const mainWindow = BrowserWindow.fromWebContents(event.sender);
            const windowId = mainWindow.id;

            if (!windowStates.has(windowId)) {
                // 如果没有保存的状态，使用默认大小
                mainWindow.setBounds({
                    width: 800,
                    height: 600,
                    x: 100,
                    y: 100
                });
            }

            // 获取保存的原始状态
            const originalState = windowStates.get(windowId);

            // 恢复窗口到原始状态
            if (originalState.isMaximized) {
                // 如果窗口之前是最大化的，先恢复到原始大小再最大化
                mainWindow.setBounds(originalState.bounds);
                mainWindow.maximize();
            } else {
                mainWindow.setBounds(originalState.bounds);
            }

            // 移除保存的状态（可选，也可以保留用于多次切换）
            // windowStates.delete(windowId);
        });

        // 打开模型文件夹
        ipcMain.handle('open-models-folder', async (event, arg) => {
            try {
                const folderType = arg?.folderType || 'vsa' // 默认是 vsa

                // 获取应用根目录
                let appPath
                if (app.isPackaged) {
                    // 生产环境：打包后的应用（Windows部署时使用）
                    appPath = path.dirname(process.execPath)
                } else {
                    // 开发环境：直接使用项目根目录
                    // __dirname 在开发环境下指向 src/main/services/
                    // 需要往上3级到达项目根目录 ai-live-broadcast/
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                // 根据不同类型拼接不同的路径
                let modelsPath
                if (folderType === 'vsa') {
                    // 本地语音服务: extra/vsa/data/models
                    modelsPath = path.join(appPath, 'extra', 'vsa', 'data', 'models')
                } else if (folderType === 'f5tts') {
                    // F5TTS语音服务: extra/f5tts/models
                    modelsPath = path.join(appPath, 'extra', 'f5tts', 'models')
                } else if (folderType === 'extra') {
                    // 插件安装目录: extra
                    modelsPath = path.join(appPath, 'extra')
                }

                // 确保文件夹存在，如果不存在则创建
                if (!fs.existsSync(modelsPath)) {
                    fs.mkdirSync(modelsPath, {recursive: true})
                }

                // 打开文件夹
                const result = await shell.openPath(modelsPath)

                if (result) {
                    // 如果返回非空字符串，表示打开失败
                    return {success: false, error: result, path: modelsPath}
                } else {
                    return {success: true, path: modelsPath}
                }
            } catch (error) {
                return {success: false, error: error.message}
            }
        })

        // 扫描主播模型列表
        ipcMain.handle('scan-host-models', async (event, arg) => {
            try {
                // 获取应用根目录
                let appPath
                if (app.isPackaged) {
                    // 生产环境：打包后的应用（Windows部署时使用）
                    appPath = path.dirname(process.execPath)
                } else {
                    // 开发环境：直接使用项目根目录
                    // __dirname 在开发环境下指向 src/main/services/
                    // 需要往上3级到达项目根目录 ai-live-broadcast/
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                // VSA模型目录：extra/vsa/data/models
                const modelsPath = path.join(appPath, 'extra', 'vsa', 'data', 'models')

                // 如果目录不存在，返回空列表
                if (!fs.existsSync(modelsPath)) {
                    return {success: true, models: [], path: modelsPath, count: 0}
                }

                // 读取目录下的所有文件夹，并按名称排序（与VSA加载顺序一致）
                const items = fs.readdirSync(modelsPath, {withFileTypes: true})
                    .filter(item => item.isDirectory())
                    .sort((a, b) => a.name.localeCompare(b.name))

                const models = []
                const newSpeakerIdMap = {}  // 临时存储新的映射
                let modelIdCounter = 0      // 模型ID计数器

                // 遍历每个文件夹，验证是否为有效的模型文件夹
                for (const item of items) {
                    const modelFolderName = item.name
                    const modelPath = path.join(modelsPath, modelFolderName)

                    // 验证是否包含必需的文件：config.json 和 .pth 文件
                    const configPath = path.join(modelPath, 'config.json')
                    const hasConfigJson = fs.existsSync(configPath)

                    // 查找 .pth 文件（PyTorch模型权重）
                    const files = fs.readdirSync(modelPath)
                    const hasPthFile = files.some(file => file.endsWith('.pth'))

                    // 如果包含必需文件，则认为是有效模型
                    if (hasConfigJson && hasPthFile) {
                        try {
                            // 读取 config.json 文件
                            const configContent = fs.readFileSync(configPath, 'utf-8')
                            const config = JSON.parse(configContent)

                            // 获取 spk2id 中的 key 作为主播名称
                            const spk2id = config?.data?.spk2id || {}
                            const speakerNames = Object.keys(spk2id)

                            // 为每个 speaker 创建一个模型项，并分配 modelId
                            speakerNames.forEach(speakerName => {
                                // 建立 speaker 到 modelId 的映射
                                newSpeakerIdMap[speakerName] = modelIdCounter

                                models.push({
                                    name: speakerName,           // 主播名称（spk2id 的 key）
                                    label: speakerName,          // 显示标签
                                    value: speakerName,          // 选择值
                                    modelId: modelIdCounter,     // VSA 模型ID
                                    path: modelPath,             // 模型路径
                                    folderName: modelFolderName  // 文件夹名（备用）
                                })
                            })

                            // 每个文件夹对应一个 modelId
                            modelIdCounter++
                        } catch (error) {
                            console.error(`解析 ${modelFolderName} 的 config.json 失败:`, error.message)
                        }
                    }
                }

                // 更新全局的 speakerIdMap
                speakerIdMap = newSpeakerIdMap

                console.log('[IPC] 动态生成主播ID映射:', speakerIdMap)

                return {
                    success: true,
                    models: models,
                    path: modelsPath,
                    count: models.length
                }
            } catch (error) {
                console.error('扫描主播模型失败:', error)
                return {
                    success: false,
                    error: error.message,
                    models: [],
                    count: 0
                }
            }
        })

        // 清理音频缓存
        ipcMain.handle('clear-audio-cache', async (event) => {
            try {
                console.log('[IPC] 清理音频缓存')

                // 获取 audio_temp 目录路径
                let appPath
                if (app.isPackaged) {
                    appPath = path.dirname(process.execPath)
                } else {
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                const audioTempDir = path.join(appPath, 'audio_temp')
                console.log('[IPC] audio_temp 目录:', audioTempDir)

                // 如果目录不存在，直接返回
                if (!fs.existsSync(audioTempDir)) {
                    console.log('[IPC] audio_temp 目录不存在，无需清理')
                    return { success: true, count: 0 }
                }

                // 读取目录中的所有文件
                const files = fs.readdirSync(audioTempDir)
                console.log(`[IPC] 找到 ${files.length} 个缓存文件`)

                // 删除所有文件
                let count = 0
                for (const file of files) {
                    const filePath = path.join(audioTempDir, file)
                    try {
                        fs.unlinkSync(filePath)
                        count++
                    } catch (err) {
                        console.error(`[IPC] 删除文件失败: ${file}`, err)
                    }
                }

                console.log(`[IPC] ✓ 成功清理 ${count} 个缓存文件`)
                return { success: true, count: count }
            } catch (error) {
                console.error('[IPC] ✗ 清理音频缓存失败:', error)
                return { success: false, error: error.message }
            }
        })

        // 选择背景音乐目录
        ipcMain.handle('select-music-directory', async (event) => {
            try {
                const result = await dialog.showOpenDialog({
                    properties: ['openDirectory'],
                    title: '选择背景音乐目录'
                })

                if (result.canceled || result.filePaths.length === 0) {
                    return { success: false, canceled: true }
                }

                const selectedPath = result.filePaths[0]
                console.log('[IPC] 选择的背景音乐目录:', selectedPath)

                return { success: true, path: selectedPath }
            } catch (error) {
                console.error('[IPC] 选择背景音乐目录失败:', error)
                return { success: false, error: error.message }
            }
        })

        // 扫描音乐文件
        ipcMain.handle('scan-music-files', async (event, arg) => {
            try {
                const { musicDir } = arg

                console.log('[IPC] 扫描音乐文件:', musicDir)

                if (!fs.existsSync(musicDir)) {
                    return { success: false, error: '目录不存在' }
                }

                // 支持的音乐文件格式
                const supportedFormats = ['.mp3', '.wav', '.ogg', '.m4a', '.flac', '.aac']

                // 读取目录中的所有文件
                const files = fs.readdirSync(musicDir)
                const musicFiles = []

                for (const file of files) {
                    const ext = path.extname(file).toLowerCase()
                    if (supportedFormats.includes(ext)) {
                        const fullPath = path.join(musicDir, file)
                        musicFiles.push(fullPath)
                    }
                }

                console.log(`[IPC] 找到 ${musicFiles.length} 个音乐文件`)

                return {
                    success: true,
                    files: musicFiles,
                    count: musicFiles.length
                }
            } catch (error) {
                console.error('[IPC] 扫描音乐文件失败:', error)
                return { success: false, error: error.message }
            }
        })

        // 扫描句间音效文件
        ipcMain.handle('scan-end-sound-files', async (event) => {
            try {
                // 获取应用路径
                let appPath
                if (app.isPackaged) {
                    appPath = path.dirname(process.execPath)
                } else {
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                const endSoundDir = path.join(appPath, 'extra', 'space')
                console.log('[IPC] 扫描句间音效目录:', endSoundDir)

                // 检查目录是否存在
                if (!fs.existsSync(endSoundDir)) {
                    console.warn('[IPC] 句间音效目录不存在:', endSoundDir)
                    return {
                        success: true,
                        files: [],
                        count: 0
                    }
                }

                // 支持的音频格式
                const supportedFormats = ['.mp3', '.wav', '.ogg', '.m4a', '.flac', '.aac']

                // 读取目录下的所有文件
                const files = fs.readdirSync(endSoundDir)
                const endSoundFiles = files
                    .filter(file => supportedFormats.includes(path.extname(file).toLowerCase()))
                    .map(file => path.join(endSoundDir, file))

                console.log(`[IPC] 找到 ${endSoundFiles.length} 个句间音效文件`)

                return {
                    success: true,
                    files: endSoundFiles,
                    count: endSoundFiles.length
                }
            } catch (error) {
                console.error('[IPC] 扫描句间音效文件失败:', error)
                return {
                    success: false,
                    error: error.message,
                    files: [],
                    count: 0
                }
            }
        })

        // TTS 语音合成 IPC 处理器
        ipcMain.handle('generate-tts-audio', async (event, arg) => {
            try {
                const {text, speaker, format = 'wav', lang = 'auto'} = arg

                // 验证参数
                if (!text || !speaker) {
                    return {
                        success: false,
                        error: '缺少必要参数: text 和 speaker'
                    }
                }

                // 读取 AI 服务器配置
                const serverUrl = arg.serverUrl || 'http://127.0.0.1:23456/'

                // 使用动态生成的 speakerIdMap（由 scan-host-models 创建）
                if (Object.keys(speakerIdMap).length === 0) {
                    console.error('[IPC] ✗ 主播ID映射未初始化，请先扫描模型列表')
                    return {
                        success: false,
                        error: '主播ID映射未初始化，请先扫描模型列表'
                    }
                }

                // 获取 speaker 对应的 id
                const speakerId = speakerIdMap[speaker]
                if (speakerId === undefined) {
                    console.error('[IPC] ✗ 未知的主播名称:', speaker)
                    console.error('[IPC] 支持的主播:', Object.keys(speakerIdMap))
                    return {
                        success: false,
                        error: `未知的主播名称: ${speaker}，请选择正确的主播`
                    }
                }

                // 构建 TTS API URL（使用正确的接口）
                const apiUrl = `${serverUrl}voice/bert-vits2`

                // 构建请求参数（使用 id 而不是 speaker，与原版完全一致）
                const params = {
                    text: text,
                    id: speakerId,
                    format: format,
                    lang: lang,
                    length: arg.length || 1.0,
                    noise: arg.noise || 0.33,
                    noisew: arg.noisew || 0.4,
                    emotion: arg.emotion || 5,
                    sdp_ratio: arg.sdp_ratio || 1.0
                    // 注意：原版代码移除了 segment_size 参数，我们也不使用
                }

                // 构建 URL 查询字符串
                const queryString = Object.keys(params)
                    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
                    .join('&')

                const fullUrl = `${apiUrl}?${queryString}`

                console.log('[IPC] 生成 TTS 音频')
                console.log('[IPC]   请求URL:', fullUrl)
                console.log('[IPC]   文本:', text.substring(0, 50))
                console.log('[IPC]   主播:', speaker, '(id=' + speakerId + ')')

                // 下载音频文件到本地
                const crypto = require('crypto')
                const https = require('https')
                const http = require('http')

                // 创建 audio_temp 目录
                let appPath
                if (app.isPackaged) {
                    appPath = path.dirname(process.execPath)
                } else {
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                const audioTempDir = path.join(appPath, 'audio_temp')
                if (!fs.existsSync(audioTempDir)) {
                    console.log('[IPC] 创建 audio_temp 目录:', audioTempDir)
                    fs.mkdirSync(audioTempDir, {recursive: true})
                }

                // 生成音频文件名（使用 MD5 哈希）
                const hash = crypto.createHash('md5').update(text + speaker).digest('hex')
                const audioFileName = `${hash}.wav`
                const audioFilePath = path.join(audioTempDir, audioFileName)

                console.log('[IPC]   音频文件路径:', audioFilePath)

                // 检查文件是否已存在
                if (fs.existsSync(audioFilePath)) {
                    console.log('[IPC] ✓ 音频文件已缓存，直接使用')
                    return {
                        success: true,
                        audioUrl: audioFilePath,  // 返回本地文件路径
                        speaker: speaker,
                        text: text,
                        format: format,
                        cached: true
                    }
                }

                // 下载音频文件
                console.log('[IPC] 开始下载音频文件...')
                const httpModule = fullUrl.startsWith('https') ? https : http

                await new Promise((resolve, reject) => {
                    httpModule.get(fullUrl, (response) => {
                        console.log('[IPC] VSA 响应状态码:', response.statusCode)
                        console.log('[IPC] VSA 响应头:', response.headers)

                        if (response.statusCode !== 200) {
                            // 读取错误响应体
                            let errorBody = ''
                            response.on('data', (chunk) => {
                                errorBody += chunk.toString()
                            })
                            response.on('end', () => {
                                console.error('[IPC] ✗ VSA 返回错误')
                                console.error('[IPC]   状态码:', response.statusCode)
                                console.error('[IPC]   错误信息:', errorBody)
                                reject(new Error(`HTTP ${response.statusCode}: ${errorBody || response.statusMessage}`))
                            })
                            return
                        }

                        const fileStream = fs.createWriteStream(audioFilePath)
                        response.pipe(fileStream)

                        fileStream.on('finish', () => {
                            fileStream.close()
                            console.log('[IPC] ✓ 音频文件下载完成')
                            const fileSize = fs.statSync(audioFilePath).size
                            console.log('[IPC]   文件大小:', fileSize, 'bytes')
                            resolve()
                        })

                        fileStream.on('error', (err) => {
                            fs.unlink(audioFilePath, () => {
                            })  // 删除不完整的文件
                            reject(err)
                        })
                    }).on('error', (err) => {
                        console.error('[IPC] ✗ HTTP 请求失败:', err.message)
                        reject(err)
                    })
                })

                // 返回本地文件路径
                return {
                    success: true,
                    audioUrl: audioFilePath,  // 返回本地文件路径
                    speaker: speaker,
                    text: text,
                    format: format,
                    cached: false
                }

            } catch (error) {
                console.error('[IPC] ✗ 生成 TTS 音频失败:', error)
                return {
                    success: false,
                    error: error.message
                }
            }
        })

        // 启动语音服务
        ipcMain.handle('start-voice-service', async (event, arg) => {
            try {
                const {serviceType, options} = arg
                console.log(`[IPC] 启动语音服务: ${serviceType}`, options)

                let result
                switch (serviceType) {
                    case 'vsa':
                    case 'local':
                        result = await voiceServiceManager.startVSAService(options)
                        break
                    case 'f5tts':
                        // TODO: F5TTS 启动逻辑
                        result = {success: false, error: 'F5TTS 服务暂未实现'}
                        break
                    case 'cosyvoice':
                        // TODO: COSYVOICE 启动逻辑
                        result = {success: false, error: 'COSYVOICE 服务暂未实现'}
                        break
                    default:
                        result = {success: false, error: `未知的服务类型: ${serviceType}`}
                }

                return result
            } catch (error) {
                console.error('[IPC] 启动语音服务失败:', error)
                return {success: false, error: error.message}
            }
        })

        // 停止语音服务
        ipcMain.handle('stop-voice-service', async (event, arg) => {
            try {
                const {serviceType} = arg
                console.log(`[IPC] 停止语音服务: ${serviceType}`)

                let result
                switch (serviceType) {
                    case 'vsa':
                    case 'local':
                        result = await voiceServiceManager.stopVSAService()
                        break
                    case 'f5tts':
                        // TODO: F5TTS 停止逻辑
                        result = {success: false, error: 'F5TTS 服务暂未实现'}
                        break
                    case 'cosyvoice':
                        // TODO: COSYVOICE 停止逻辑
                        result = {success: false, error: 'COSYVOICE 服务暂未实现'}
                        break
                    default:
                        result = {success: false, error: `未知的服务类型: ${serviceType}`}
                }

                return result
            } catch (error) {
                console.error('[IPC] 停止语音服务失败:', error)
                return {success: false, error: error.message}
            }
        })

        // 检查语音服务状态
        ipcMain.handle('check-voice-service-status', async (event, arg) => {
            try {
                const {serviceType} = arg || {}

                if (serviceType) {
                    // 检查单个服务状态
                    switch (serviceType) {
                        case 'vsa':
                        case 'local':
                            return await voiceServiceManager.checkVSAServiceStatus()
                        default:
                            return {success: false, error: `未知的服务类型: ${serviceType}`}
                    }
                } else {
                    // 获取所有服务状态
                    return {
                        success: true,
                        services: voiceServiceManager.getAllServiceStatus()
                    }
                }
            } catch (error) {
                console.error('[IPC] 检查语音服务状态失败:', error)
                return {success: false, error: error.message}
            }
        })

        // 停止所有语音服务
        ipcMain.handle('stop-all-voice-services', async (event, arg) => {
            try {
                console.log('[IPC] 停止所有语音服务')
                const result = await voiceServiceManager.stopAllServices()
                return result
            } catch (error) {
                console.error('[IPC] 停止所有语音服务失败:', error)
                return {success: false, error: error.message}
            }
        })

        // 检查 VSA 服务是否已安装
        ipcMain.handle('check-vsa-installed', async (event, arg) => {
            try {
                console.log('[IPC] 检查 VSA 服务安装状态')
                const result = voiceServiceManager.checkVSAInstalled()
                return result
            } catch (error) {
                console.error('[IPC] 检查 VSA 安装状态失败:', error)
                return {
                    installed: false,
                    error: '检查失败',
                    message: error.message
                }
            }
        })

        // VSA 服务诊断
        ipcMain.handle('diagnose-vsa-service', async (event, arg) => {
            try {
                console.log('[IPC] 开始 VSA 服务诊断')

                // 直接调用 voiceServiceManager 的诊断方法
                const report = await voiceServiceManager.diagnoseVSAService()

                return {
                    success: true,
                    report: report
                }
            } catch (error) {
                console.error('[IPC] VSA 服务诊断失败:', error)
                return {
                    success: false,
                    error: error.message
                }
            }
        })

        // 打开诊断报告文件夹
        ipcMain.handle('open-diagnostic-report', async (event, arg) => {
            try {
                let appPath
                if (app.isPackaged) {
                    appPath = path.dirname(process.execPath)
                } else {
                    appPath = path.join(__dirname, '..', '..', '..')
                }

                const reportPath = path.join(appPath, 'vsa-diagnostic-report.json')

                if (fs.existsSync(reportPath)) {
                    // 打开文件所在目录并选中文件
                    const result = await shell.showItemInFolder(reportPath)
                    return { success: true, path: reportPath }
                } else {
                    return { success: false, error: '诊断报告不存在' }
                }
            } catch (error) {
                console.error('[IPC] 打开诊断报告失败:', error)
                return { success: false, error: error.message }
            }
        })

        // ============ GPT功能相关IPC ============

        // 打开文件对话框
        ipcMain.handle('dialog:openFile', async (event, options) => {
            try {
                const result = await dialog.showOpenDialog(
                    BrowserWindow.fromWebContents(event.sender),
                    options
                )
                return result
            } catch (error) {
                console.error('[IPC] 打开文件对话框失败:', error)
                return {canceled: true, error: error.message}
            }
        })

        // 保存文件对话框
        ipcMain.handle('dialog:saveFile', async (event, options) => {
            try {
                const result = await dialog.showSaveDialog(
                    BrowserWindow.fromWebContents(event.sender),
                    options
                )
                return result
            } catch (error) {
                console.error('[IPC] 打开保存对话框失败:', error)
                return {canceled: true, error: error.message}
            }
        })

        // 引入抖音IPC
        ipcDouyin.initDouyinService()
    }
}
