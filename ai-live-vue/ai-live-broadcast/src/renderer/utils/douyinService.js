/**
 * 连接Dy服务端的WS
 * 主要负责接收来自Dy服务端的消息
 */
const douyinUtil = require("@/utils/douyinUtil");
const parseDouyinMsg = require("@/utils/douyinMsg");
const remote = require("@electron/remote");
const {ipcRenderer} = require("electron");

class DouyinService {
    constructor() {
        this.webview = null;
        this.debuggerAttached = true

        this.liveUrl = null;
        this.ws = null;
    }

    //----------------Electron窗口监听-----------------------
    async initWebSocketMonitoring(webview) {
        this.webview = webview;
        const webContents = remote.webContents.fromId(webview.getWebContentsId());

        try {
            // 附加调试器
            await webContents.debugger.attach('1.3');
            this.debuggerAttached = true;
            // console.log('调试器附加成功');

            // 启用网络监听
            await webContents.debugger.sendCommand('Network.enable');

            // 监听WebSocket事件
            webContents.debugger.on('message', (event, method, params) => {
                if (method === 'Network.webSocketCreated') {
                    // console.log('WebSocket已创建:', params.url);
                }

                if (method === 'Network.webSocketFrameReceived') {
                    const message = params.response.payloadData;
                    // console.log('收到WebSocket消息:', message);
                    const binaryData = Buffer.from(message, 'base64');
                    try {
                        const response = parseDouyinMsg(binaryData);
                        // response.forEach(item => {
                        //     console.log('解析消息:', item);
                        // })

                        // 发送数据
                        ipcRenderer.send("send-douyin-msg", response);

                        // 开启GPT互动
                        douyinUtil.enableGPTInteractive(response, webview);
                    } catch (e) {
                    }
                }
            });
        } catch (error) {
            console.error('调试器附加失败:', error);
        }
    }

    async cleanup() {
        if (this.debuggerAttached) {
            const webContents = remote.webContents.fromId(this.webview.getWebContentsId());
            try {
                await webContents.debugger.detach();
                this.debuggerAttached = false;
                console.log('调试器已分离');
            } catch (error) {
                console.error('调试器分离失败:', error);
            }
        }
    }

    /**
     * 发送弹幕
     * @param {string} danmakuContent 弹幕内容
     * @param webview 弹幕所在的webview
     */
    async sendDanmaku(danmakuContent, webview = null) {
        if (webview != null) {
            this.webview = webview;
        }
        if (!this.webview) {
            throw new Error('Webview未初始化');
        }

        try {
            const result = await this.webview.executeJavaScript(`
            (function() {
                return new Promise((resolve, reject) => {
                    try {
                        // 1. 查找抖音直播间输入框
                        const inputSelector = '.zone-container.editor-kit-container[contenteditable="true"]';
                        const inputElement = document.querySelector(inputSelector);
                        
                        if (!inputElement) {
                            reject('未找到弹幕输入框');
                            return;
                        }

                        // 2. 清空输入框并设置新内容
                        inputElement.focus();
                        inputElement.innerHTML = '';
                        
                        const textNode = document.createTextNode('${danmakuContent.replace(/'/g, "\\'")}');
                        inputElement.appendChild(textNode);
                        
                        // 3. 触发输入事件以确保抖音检测到输入[1](@ref)
                        const inputEvent = new Event('input', { 
                            bubbles: true, 
                            cancelable: true 
                        });
                        inputElement.dispatchEvent(inputEvent);
                        
                        const changeEvent = new Event('change', { 
                            bubbles: true, 
                            cancelable: true 
                        });
                        inputElement.dispatchEvent(changeEvent);

                        // 4. 短暂延迟后查找可用的发送按钮[1](@ref)
                        setTimeout(() => {
                            const sendButtonSelector = '.webcast-chatroom___send-btn:not(.disable)';
                            const sendButton = document.querySelector(sendButtonSelector);
                            
                            if (sendButton && !sendButton.disabled) {
                                // 5. 直接触发按钮的点击事件[1,4](@ref)
                                const clickEvent = new Event('click', {
                                    bubbles: true,
                                    cancelable: true
                                });
                                sendButton.dispatchEvent(clickEvent);
                                resolve('弹幕发送成功（按钮点击）');
                            } else {
                                reject('发送按钮不可用或未找到');
                            }
                        }, 150);
                        
                    } catch (error) {
                        reject('发送过程中出错: ' + error.message);
                    }
                });
            })();
        `);
            console.log('弹幕发送结果:', result);
            return result;
        } catch (error) {
            console.error('弹幕发送失败:', error);
            throw error;
        }
    }

    // ------------原生websocket--------------
    /**
     * 初始化WebSocket连接
     */
    async init(liveUrl) {
        this.liveUrl = liveUrl
        const {roomId, uniqueId} = await douyinUtil.fetchLiveRoomInfo(this.liveUrl);
        const url = douyinUtil.getDyWsUrl(roomId, uniqueId);

        this.ws = new WebSocket(url);
        this.ws.binaryType = "arraybuffer";

        this.ws.addEventListener('open', (ev) => {
            console.log("连接成功");
        });

        this.ws.addEventListener('close', (ev) => {
            this.destroy();
        });

        this.ws.addEventListener('error', (ev) => {
            this.destroy();
        });

        this.ws.addEventListener('message', (ev) => {
            const msgContentList = parseDouyinMsg(ev.data);
            msgContentList.forEach((msgObj) => {
                console.log("解析后的消息:", msgObj); // 检查解析结果
            });
        });
    }

    /**
     * 销毁WebSocket连接
     */
    destroy() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
}

module.exports = DouyinService;