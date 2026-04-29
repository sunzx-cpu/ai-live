/**
 * WebSocket工具类
 * 用于管理WebSocket连接、消息处理和事件监听
 */
function WebSocketUtil(url, options) {
  options = options || {}
  this.url = url
  this.ws = null
  this.reconnectCount = 0
  this.maxReconnectCount = options.maxReconnectCount || 5
  this.reconnectInterval = options.reconnectInterval || 3000
  this.heartbeatInterval = options.heartbeatInterval || 30000
  this.heartbeatTimer = null
  this.reconnectTimer = null
  this.listeners = {}
  this.isManualClose = false

  // 默认事件处理
  this.defaultHandlers = {
    onOpen: options.onOpen || this.defaultOnOpen.bind(this),
    onMessage: options.onMessage || this.defaultOnMessage.bind(this),
    onError: options.onError || this.defaultOnError.bind(this),
    onClose: options.onClose || this.defaultOnClose.bind(this)
  }
}

/**
 * 连接WebSocket
 */
WebSocketUtil.prototype.connect = function() {
  try {
    this.ws = new WebSocket(this.url)
    this.ws.onopen = this.defaultHandlers.onOpen
    this.ws.onmessage = this.defaultHandlers.onMessage
    this.ws.onerror = this.defaultHandlers.onError
    this.ws.onclose = this.defaultHandlers.onClose
  } catch (error) {
    console.error('WebSocket连接失败:', error)
    this.emit('error', error)
  }
}

/**
 * 默认连接成功处理
 */
WebSocketUtil.prototype.defaultOnOpen = function(event) {
  console.log('WebSocket连接成功', event)
  this.reconnectCount = 0
  this.startHeartbeat()
  this.emit('open', event)
}

/**
 * 默认消息接收处理
 */
WebSocketUtil.prototype.defaultOnMessage = function(event) {
  try {
    var data = JSON.parse(event.data)
    console.log('WebSocket接收消息:', data)
    this.emit('message', data)

    // 根据消息类型分发事件
    if (data.type) {
      this.emit(data.type, data)
    }
  } catch (error) {
    console.error('解析WebSocket消息失败:', error)
    this.emit('parseError', { error: error, rawData: event.data })
  }
}

/**
 * 默认错误处理
 */
WebSocketUtil.prototype.defaultOnError = function(error) {
  console.error('WebSocket错误:', error)
  this.emit('error', error)
}

/**
 * 默认连接关闭处理
 */
WebSocketUtil.prototype.defaultOnClose = function(event) {
  console.log('WebSocket连接关闭:', event)
  this.stopHeartbeat()
  this.emit('close', event)

  // 如果不是手动关闭且未达到最大重连次数，则自动重连
  if (!this.isManualClose && this.reconnectCount < this.maxReconnectCount) {
    this.reconnect()
  }
}

/**
 * 发送消息
 */
WebSocketUtil.prototype.send = function(data) {
  if (this.ws && this.ws.readyState === WebSocket.OPEN) {
    try {
      var message = typeof data === 'object' ? JSON.stringify(data) : data
      this.ws.send(message)
      console.log('WebSocket发送消息:', message)
    } catch (error) {
      console.error('WebSocket发送消息失败:', error)
      this.emit('sendError', error)
    }
  } else {
    console.warn('WebSocket未连接，无法发送消息')
    this.emit('notConnected', data)
  }
}

/**
 * 关闭连接
 */
WebSocketUtil.prototype.close = function() {
  this.isManualClose = true
  this.stopHeartbeat()
  this.clearReconnectTimer()

  if (this.ws) {
    this.ws.close()
    this.ws = null
  }
}

/**
 * 重连
 */
WebSocketUtil.prototype.reconnect = function() {
  var self = this
  if (this.reconnectCount >= this.maxReconnectCount) {
    console.error('达到最大重连次数，停止重连')
    this.emit('maxReconnectReached')
    return
  }

  this.reconnectCount++
  console.log('尝试第' + this.reconnectCount + '次重连...')

  this.reconnectTimer = setTimeout(function() {
    self.connect()
  }, this.reconnectInterval)
}

/**
 * 清除重连定时器
 */
WebSocketUtil.prototype.clearReconnectTimer = function() {
  if (this.reconnectTimer) {
    clearTimeout(this.reconnectTimer)
    this.reconnectTimer = null
  }
}

/**
 * 开始心跳
 */
WebSocketUtil.prototype.startHeartbeat = function() {
  var self = this
  this.stopHeartbeat()
  this.heartbeatTimer = setInterval(function() {
    self.send({ type: 'ping', timestamp: Date.now() })
  }, this.heartbeatInterval)
}

/**
 * 停止心跳
 */
WebSocketUtil.prototype.stopHeartbeat = function() {
  if (this.heartbeatTimer) {
    clearInterval(this.heartbeatTimer)
    this.heartbeatTimer = null
  }
}

/**
 * 添加事件监听器
 */
WebSocketUtil.prototype.on = function(event, callback) {
  if (!this.listeners[event]) {
    this.listeners[event] = []
  }
  this.listeners[event].push(callback)
}

/**
 * 移除事件监听器
 */
WebSocketUtil.prototype.off = function(event, callback) {
  if (!this.listeners[event]) return

  var index = this.listeners[event].indexOf(callback)
  if (index > -1) {
    this.listeners[event].splice(index, 1)
  }
}

/**
 * 触发事件
 */
WebSocketUtil.prototype.emit = function(event, data) {
  if (!this.listeners[event]) return

  var self = this
  this.listeners[event].forEach(function(callback) {
    try {
      callback(data)
    } catch (error) {
      console.error('事件监听器执行错误 [' + event + ']:', error)
    }
  })
}

/**
 * 获取连接状态
 */
WebSocketUtil.prototype.getReadyState = function() {
  if (!this.ws) return WebSocket.CLOSED
  return this.ws.readyState
}

/**
 * 检查是否已连接
 */
WebSocketUtil.prototype.isConnected = function() {
  return this.ws && this.ws.readyState === WebSocket.OPEN
}

export default WebSocketUtil
