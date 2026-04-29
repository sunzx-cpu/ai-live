/**
 * WebSocket管理类 - 支持心跳检测、自动重连
 */
class WebSocketUtil {
    constructor(options = {}) {
        // 配置参数
        this.url = options.url || '';
        this.userId = options.userId || '';
        this.heartbeatIntervalTime = options.heartbeatIntervalTime || 30000; // 心跳间隔30秒
        this.maxReconnectAttempts = options.maxReconnectAttempts || 5; // 最大重连次数
        this.reconnectInterval = options.reconnectInterval || 5000; // 重连间隔5秒

        // 内部状态
        this.websocket = null;
        this.heartbeatInterval = null;
        this.reconnectAttempts = 0;
        this.reconnectTimer = null;
        this.isManualClose = false; // 是否手动关闭

        // 回调函数
        this.onOpen = options.onOpen || (() => {
        });
        this.onMessage = options.onMessage || (() => {
        });
        this.onClose = options.onClose || (() => {
        });
        this.onError = options.onError || (() => {
        });
        this.onReconnect = options.onReconnect || (() => {
        });
    }

    /**
     * 初始化WebSocket连接
     */
    connect() {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            console.log('WebSocket已连接，无需重复连接');
            return;
        }

        try {
            const wsUrl = `${this.url}${this.userId}`;
            this.websocket = new WebSocket(wsUrl);

            this.websocket.onopen = (event) => {
                console.log('WebSocket连接已建立', event);
                this.reconnectAttempts = 0; // 重置重连计数
                this.startHeartbeat();
                this.onOpen(event);
            };

            this.websocket.onmessage = (event) => {
                this.handleMessage(event);
            };

            this.websocket.onclose = (event) => {
                console.log('WebSocket连接已关闭', event);
                this.stopHeartbeat();
                this.onClose(event);

                // 非手动关闭且未达到最大重连次数时自动重连
                if (!this.isManualClose && this.reconnectAttempts < this.maxReconnectAttempts) {
                    this.scheduleReconnect();
                }
            };

            this.websocket.onerror = (error) => {
                console.error('WebSocket发生错误:', error);
                this.onError(error);
            };

        } catch (error) {
            console.error('创建WebSocket连接失败:', error);
            this.onError(error);
        }
    }

    /**
     * 处理接收到的消息
     */
    handleMessage(event) {
        const message = JSON.parse(event.data);

        if (message.type === 'interactKeyword_ack') {
            // console.log('心跳正常，同时收到互动关键词数据:', message.data);
            this.handleBusinessData(message);
        } else {
            // 处理其他类型的业务消息
            // console.log('收到其他业务消息:', message);
            this.onMessage(message);
        }
    }

    /**
     * 处理业务数据
     */
    handleBusinessData(data) {
        localStorage.setItem("interactKeyword", data.data);
        this.onMessage(data);
    }

    /**
     * 开始发送心跳
     */
    startHeartbeat() {
        this.stopHeartbeat();

        // 立即发送一次心跳
        this.sendHeartbeat();

        // 设置定时器
        this.heartbeatInterval = setInterval(() => {
            this.sendHeartbeat();
        }, this.heartbeatIntervalTime);
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    /**
     * 发送心跳包
     */
    sendHeartbeat() {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            const heartbeatMsg = {
                type: 'interactKeyword',
                userId: this.userId,
                timestamp: Date.now()
            };
            this.websocket.send(JSON.stringify(heartbeatMsg));
            // console.log('发送心跳包', heartbeatMsg);
        }
    }

    /**
     * 安排重连
     */
    scheduleReconnect() {
        if (this.reconnectTimer) return;

        this.reconnectAttempts++;
        console.log(`准备第${this.reconnectAttempts}次重连，等待${this.reconnectInterval}ms`);

        this.reconnectTimer = setTimeout(() => {
            this.reconnectTimer = null;
            this.onReconnect(this.reconnectAttempts, this.maxReconnectAttempts);
            this.connect();
        }, this.reconnectInterval);
    }

    /**
     * 发送消息
     */
    send(message) {
        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            const messageData = typeof message === 'object' ?
                JSON.stringify(message) : message;
            this.websocket.send(messageData);
            return true;
        } else {
            console.warn('WebSocket未连接，消息发送失败');
            return false;
        }
    }

    /**
     * 关闭连接
     */
    close() {
        this.isManualClose = true;
        this.stopHeartbeat();

        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }

        if (this.websocket) {
            this.websocket.close();
        }
    }

    /**
     * 获取连接状态
     */
    getReadyState() {
        return this.websocket ? this.websocket.readyState : WebSocket.CLOSED;
    }

    /**
     * 检查是否已连接
     */
    isConnected() {
        return this.websocket && this.websocket.readyState === WebSocket.OPEN;
    }
}

export default WebSocketUtil;