<template>
	<view class="audio-player-container">
		<view class="header">
			<text class="title">TTS实时音频播放器</text>
			<text class="subtitle">基于WebSocket的实时音频流播放</text>
		</view>

		<!-- 连接配置区域 -->
		<view class="config-card">
			<view class="config-title">连接配置</view>
			<view class="button-group">
				<button class="btn btn-connect" :class="{disabled: isConnected}" @tap="connectWebSocket"
					:disabled="isConnected">
					{{ isConnected ? '已连接' : '连接服务器' }}
				</button>
				<button class="btn btn-disconnect" :class="{disabled: !isConnected}" @tap="disconnectWebSocket"
					:disabled="!isConnected">
					断开连接
				</button>
			</view>
		</view>

		<!-- 状态显示区域 -->
		<view class="status-card">
			<view class="status-title">连接状态</view>
			<view class="status-info">
				<view class="status-item">
					<text class="status-label">连接状态:</text>
					<text class="status-value" :class="getStatusClass">{{ connectionStatus }}</text>
				</view>
				<view class="status-item" v-if="audioConfig">
					<text class="status-label">音频配置:</text>
					<text class="status-value">{{ audioConfig.sampleRate }}Hz, {{ audioConfig.channels }}声道,
						{{ audioConfig.bitsPerSample }}bit</text>
				</view>
				<view class="status-item" v-if="currentText">
					<text class="status-label">当前文本:</text>
					<text class="status-value">{{ currentText }}</text>
				</view>
				<view class="status-item">
					<text class="status-label">音频块数:</text>
					<text class="status-value">{{ pcmChunks.length }}</text>
				</view>
				<view class="status-item" v-if="lastError">
					<text class="status-label">最后错误:</text>
					<text class="status-value error">{{ lastError }}</text>
				</view>
			</view>
		</view>

		<!-- 控制按钮区域 -->
		<view class="control-card">
			<view class="control-title">播放控制</view>
			<view class="control-buttons">
				<button class="btn btn-play" :class="{disabled: !hasAudioData || isPlaying}" @tap="playAudio"
					:disabled="!hasAudioData || isPlaying">
					{{ isPlaying ? '播放中...' : '播放音频' }}
				</button>
				<button class="btn btn-stop" @tap="stopAudio" :disabled="!isPlaying">
					停止播放
				</button>
				<button class="btn btn-clear" @tap="clearAudioData" :disabled="pcmChunks.length === 0">
					清空数据
				</button>
			</view>
		</view>

		<!-- 音频信息 -->
		<view class="info-card" v-if="audioInfo">
			<view class="info-title">音频信息</view>
			<view class="info-content">
				<text>时长: {{ audioInfo.duration }}秒</text>
				<text>大小: {{ audioInfo.size }}</text>
				<text>采样率: {{ audioInfo.sampleRate }}Hz</text>
			</view>
		</view>

		<!-- 日志显示区域 -->
		<view class="log-card">
			<view class="log-header">
				<text class="log-title">运行日志</text>
				<view>
					<text class="clear-log" @tap="clearLog">清空日志</text>
					<text class="auto-scroll" @tap="toggleAutoScroll">
						{{ autoScroll ? '🔒暂停滚动' : '🔓自动滚动' }}
					</text>
				</view>
			</view>
			<scroll-view class="log-content" scroll-y :scroll-top="scrollTop" @scroll="onScroll">
				<text class="log-text">{{ logContent }}</text>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import BinaryUtils from '@/utils/BinaryUtils.js';
	import WavFileUtil from '@/utils/WavFileUtil.js';
	export default {
		data() {
			return {
				// WebSocket配置
				wsUrl: 'ws://117.72.100.80:8201/ai-live-api/websocket/liveInfo/',
				ws: null,
				isConnected: false,
				connectionStatus: '未连接',

				// 音频数据
				pcmChunks: [],
				audioConfig: null,
				currentText: '',
				currentSessionId: null,
				audioDataUrl: '',
				filename: '',
				hasAudioData: false,

				// 播放状态
				innerAudioContext: null,
				isPlaying: false,
				audioInfo: null,

				// 日志和错误处理
				logContent: '',
				lastError: '',
				autoScroll: true,
				scrollTop: 0,
				scrollTimer: null
			}
		},

		computed: {
			getStatusClass() {
				return this.isConnected ? 'connected' : 'disconnected'
			}
		},

		onUnload() {
			this.disconnectWebSocket()
			this.stopAudio()
			if (this.scrollTimer) {
				clearTimeout(this.scrollTimer)
			}
		},
		methods: {
			// 添加日志
			addLog(message) {
				const timestamp = new Date().toLocaleTimeString()
				this.logContent += `[${timestamp}] ${message}\n`

				// 自动滚动到底部
				if (this.autoScroll) {
					this.$nextTick(() => {
						this.scrollTop = 999999 // 确保滚动到底部
					})
				}
			},

			// WebSocket连接[1,6](@ref)
			connectWebSocket() {
				this.addLog('🔗 正在连接WebSocket服务器...')

				try {
					const fullUrl = `${this.wsUrl}${this.vuex_user.id}`
					this.ws = uni.connectSocket({
						url: fullUrl,
						success: () => {
							this.addLog('✅ WebSocket连接创建成功')
						},
						fail: (err) => {
							this.addLog('❌ WebSocket连接失败: ' + err.errMsg)
							this.lastError = err.errMsg
						}
					})

					this.ws.onOpen(() => {
						this.isConnected = true
						this.connectionStatus = '已连接'
						this.addLog('✅ WebSocket连接已建立')
					})

					this.ws.onMessage((res) => {
						this.handleWebSocketMessage(res)
					})

					this.ws.onError((err) => {
						this.addLog('❌ WebSocket错误: ' + err.errMsg)
						this.connectionStatus = '连接错误'
						this.lastError = err.errMsg
					})

					this.ws.onClose(() => {
						this.isConnected = false
						this.connectionStatus = '已断开'
						this.addLog('❌ WebSocket连接已关闭')
					})

				} catch (error) {
					this.addLog('❌ 连接异常: ' + error.message)
					this.lastError = error.message
				}
			},

			// 断开WebSocket连接
			disconnectWebSocket() {
				if (this.ws) {
					this.ws.close()
					this.ws = null
				}
				this.isConnected = false
				this.connectionStatus = '未连接'
				// #ifdef APP-PLUS
				// 删除文件
				WavFileUtil.deleteAllWavFilesApp()
				// #endif
			},

			// 处理WebSocket消息
			handleWebSocketMessage(res) {
				try {
					const message = JSON.parse(res.data)

					switch (message.type) {
						case 'audio_stream_start':
							this.handleStreamStart(message)
							break
						case 'audio_stream_data':
							this.handleStreamData(message)
							break
						case 'audio_stream_end':
							this.handleStreamEnd(message)
							break
						default:
							this.addLog('📨 收到未知类型消息: ' + message.type)
					}
				} catch (error) {
					this.addLog('❌ 消息解析失败: ' + error.message)
					this.lastError = error.message
				}
			},

			// 处理音频流开始[1](@ref)
			handleStreamStart(message) {
				this.currentSessionId = message.sessionId
				this.audioConfig = message.audioConfig
				this.currentText = message.text || ''
				this.pcmChunks = []
				this.hasAudioData = false
				this.lastError = ''

				this.addLog('🎬 音频流开始接收')
				this.addLog(`📝 文本: ${this.currentText}`)
				this.addLog(
					`🔧 音频配置: ${this.audioConfig.sampleRate}Hz/${this.audioConfig.channels}声道/${this.audioConfig.bitsPerSample}bit`
				)
			},

			// 处理音频数据[1](@ref)
			handleStreamData(message) {
				if (!this.audioConfig) {
					this.addLog('⚠️ 收到音频数据，但音频配置未初始化')
					return
				}

				try {
					// 存储Base64编码的PCM数据
					this.pcmChunks.push(message.data)

					// 每10个块显示一次进度
					if (this.pcmChunks.length % 10 === 0) {
						this.addLog(`📦 已接收音频块: ${this.pcmChunks.length}`)
					}
				} catch (error) {
					this.addLog('❌ 处理音频数据失败: ' + error.message)
					this.lastError = error.message
				}
			},

			// 处理音频流结束
			handleStreamEnd(message) {
				this.addLog(`🏁 音频流接收完成，共 ${this.pcmChunks.length} 个数据块`)

				// 处理音频数据
				this.processAudioData()
			},

			// 安全的PCM数据合并（修复Uint8Array越界问题）
			safeMergePCMBuffers() {
				if (this.pcmChunks.length === 0) {
					throw new Error('没有PCM数据可供合并')
				}

				// 计算总长度
				let totalLength = 0
				const validChunks = []

				// 过滤有效数据并计算总长度
				for (const base64Data of this.pcmChunks) {
					try {
						const arrayBuffer = WavFileUtil.base64ToArrayBuffer(base64Data)
						if (arrayBuffer && arrayBuffer.byteLength > 0) {
							validChunks.push(arrayBuffer)
							totalLength += arrayBuffer.byteLength
						}
					} catch (error) {
						this.addLog('⚠️ 跳过无效的Base64数据块')
					}
				}

				if (totalLength === 0) {
					throw new Error('所有PCM数据块都无效')
				}

				// 确保长度是采样大小的整数倍（重要修复）
				const bytesPerSample = this.audioConfig.bitsPerSample / 8
				if (totalLength % bytesPerSample !== 0) {
					this.addLog(`⚠️ PCM数据总长度${totalLength}不是采样大小的整数倍，进行对齐`)
					totalLength = totalLength - (totalLength % bytesPerSample)
				}

				// 创建足够大的Uint8Array
				const mergedArray = new Uint8Array(totalLength)
				let offset = 0

				// 安全地合并每个buffer
				for (const buffer of validChunks) {
					const chunk = new Uint8Array(buffer)

					// 检查是否会越界
					if (offset + chunk.length > mergedArray.length) {
						const bytesAvailable = mergedArray.length - offset
						if (bytesAvailable > 0) {
							mergedArray.set(chunk.subarray(0, bytesAvailable), offset)
							this.addLog(`⚠️ PCM数据合并: 需要截断，已合并${offset + bytesAvailable}字节`)
						}
						break
					} else {
						mergedArray.set(chunk, offset)
						offset += chunk.length
					}
				}

				this.addLog(`✅ PCM数据安全合并完成: 总长度=${totalLength}, 实际合并=${offset}`)
				return mergedArray.buffer
			},

			// PCM转WAV格式
			pcmToWav(pcmData, sampleRate, channels, bitsPerSample) {
				const dataSize = pcmData.byteLength
				const headerSize = 44 // 标准WAV头部大小
				const totalSize = headerSize + dataSize

				// 创建足够大的ArrayBuffer
				const buffer = new ArrayBuffer(totalSize)
				const view = BinaryUtils.createSafeDataView(buffer)

				// 计算音频参数
				const blockAlign = channels * bitsPerSample / 8
				const byteRate = sampleRate * blockAlign

				try {
					// 写入RIFF header
					BinaryUtils.writeString(view, 0, 'RIFF')
					BinaryUtils.writeUint32(view, 4, 36 + dataSize, true)
					BinaryUtils.writeString(view, 8, 'WAVE')

					// 写入fmt chunk
					BinaryUtils.writeString(view, 12, 'fmt ')
					BinaryUtils.writeUint32(view, 16, 16, true)
					BinaryUtils.writeUint16(view, 20, 1, true)
					BinaryUtils.writeUint16(view, 22, channels, true)
					BinaryUtils.writeUint32(view, 24, sampleRate, true)
					BinaryUtils.writeUint32(view, 28, byteRate, true)
					BinaryUtils.writeUint16(view, 32, blockAlign, true)
					BinaryUtils.writeUint16(view, 34, bitsPerSample, true)

					// 写入data chunk
					BinaryUtils.writeString(view, 36, 'data')
					BinaryUtils.writeUint32(view, 40, dataSize, true)

					// 写入PCM数据
					const pcmBytes = new Uint8Array(pcmData)
					const wavBytes = new Uint8Array(buffer)
					wavBytes.set(pcmBytes, headerSize)

					this.addLog(`✅ WAV文件生成成功: 头部${headerSize}字节 + 数据${dataSize}字节`)
					return buffer

				} catch (error) {
					this.addLog('❌ PCM转WAV失败: ' + error.message)
					throw error
				}
			},

			// 处理音频数据
			async processAudioData() {
				if (this.pcmChunks.length === 0) {
					this.addLog('⚠️ 没有可处理的音频数据')
					return
				}

				this.addLog('⏳ 开始处理音频数据...')

				try {
					// 使用安全的方法合并PCM数据
					const mergedPcmBuffer = this.safeMergePCMBuffers()

					if (mergedPcmBuffer.byteLength === 0) {
						throw new Error('合并后的PCM数据长度为0')
					}

					this.addLog(`📊 PCM数据大小: ${(mergedPcmBuffer.byteLength / 1024).toFixed(2)} KB`)

					// 转换为WAV格式
					const wavBuffer = this.pcmToWav(
						mergedPcmBuffer,
						this.audioConfig.sampleRate,
						this.audioConfig.channels,
						this.audioConfig.bitsPerSample
					)

					// 计算音频信息
					const duration = (mergedPcmBuffer.byteLength /
						(this.audioConfig.sampleRate * this.audioConfig.channels *
							this.audioConfig.bitsPerSample / 8)).toFixed(2)

					this.audioInfo = {
						duration: duration,
						size: (mergedPcmBuffer.byteLength / 1024).toFixed(2) + ' KB',
						sampleRate: this.audioConfig.sampleRate
					}

					// #ifdef APP-PLUS
					const filename = `temp_${Date.now()}.wav`;
					this.filename = filename
					this.audioDataUrl = await WavFileUtil.saveWavFileApp(wavBuffer, filename);
					console.log(this.audioDataUrl)
					// #endif

					// #ifdef H5
					// 转换为Base64 Data URL
					const base64Wav = WavFileUtil.arrayBufferToBase64(wavBuffer)
					this.audioDataUrl = `data:audio/wav;base64,${base64Wav}`
					// #endif

					this.hasAudioData = true
					this.addLog('✅ 音频数据准备完成，可以播放')

				} catch (error) {
					this.addLog('❌ 音频数据处理失败: ' + error.message)
					this.lastError = error.message
				}
			},

			// 播放音频[2,3](@ref)
			playAudio() {
				if (!this.audioDataUrl) {
					this.addLog('⚠️ 没有可播放的音频数据')
					return
				}

				try {
					this.isPlaying = true
					this.addLog('🔊 开始播放音频...')

					// 停止之前的播放
					if (this.innerAudioContext) {
						this.innerAudioContext.destroy()
					}

					// 创建新的音频上下文[3](@ref)
					this.innerAudioContext = uni.createInnerAudioContext()
					this.innerAudioContext.src = this.audioDataUrl
					this.innerAudioContext.autoplay = true

					this.innerAudioContext.onPlay(() => {
						this.addLog('🎵 音频播放开始')
					})

					this.innerAudioContext.onEnded(() => {
						this.isPlaying = false
						this.addLog('✅ 音频播放结束')
						this.innerAudioContext.destroy()
						this.innerAudioContext = null
					})

					this.innerAudioContext.onError((error) => {
						this.isPlaying = false
						this.addLog(`❌ 音频播放错误: code ${error.errCode}, ${error.errMsg}`)
						this.lastError = `Code ${error.errCode}: ${error.errMsg}`
					})

				} catch (error) {
					this.isPlaying = false
					this.addLog('❌ 播放失败: ' + error.message)
					this.lastError = error.message
				}
			},

			// 停止播放[3](@ref)
			stopAudio() {
				if (this.innerAudioContext) {
					this.innerAudioContext.stop()
					this.innerAudioContext.destroy()
					this.innerAudioContext = null
				}
				this.isPlaying = false
				this.addLog('⏹️ 音频播放已停止')
			},

			// 清空音频数据
			async clearAudioData() {
				this.pcmChunks = []
				this.audioDataUrl = ''
				this.hasAudioData = false
				this.currentText = ''
				this.audioInfo = null
				this.stopAudio()
				this.addLog('🧹 已清空音频数据')
				// #ifdef APP-PLUS
				// 删除文件
				WavFileUtil.deleteAllWavFilesApp()
				// #endif
			},

			// 清空日志
			clearLog() {
				this.logContent = ''
				this.scrollTop = 0
			},

			// 滚动处理
			onScroll(event) {
				const {
					scrollTop,
					scrollHeight,
					deltaY
				} = event.detail
				if (deltaY < 0) {
					this.autoScroll = false
				}
			},

			// 切换自动滚动
			toggleAutoScroll() {
				this.autoScroll = !this.autoScroll
				this.addLog(this.autoScroll ? '📜 已启用自动滚动' : '📜 已暂停自动滚动')
			}
		}
	}
</script>

<style scoped>
	.audio-player-container {
		padding: 20rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		min-height: 100vh;
	}

	.header {
		text-align: center;
		margin-bottom: 30rpx;
		background: rgba(255, 255, 255, 0.1);
		border-radius: 20rpx;
		padding: 30rpx;
		backdrop-filter: blur(10px);
	}

	.title {
		display: block;
		font-size: 36rpx;
		font-weight: bold;
		color: white;
		margin-bottom: 10rpx;
	}

	.subtitle {
		display: block;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
	}

	.config-card,
	.status-card,
	.control-card,
	.log-card,
	.info-card {
		background: rgba(255, 255, 255, 0.95);
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		backdrop-filter: blur(10px);
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
	}

	.config-title,
	.status-title,
	.control-title,
	.log-title,
	.info-title {
		font-size: 28rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
		border-left: 6rpx solid #667eea;
		padding-left: 20rpx;
	}

	.input-group {
		margin-bottom: 20rpx;
	}

	.label {
		display: block;
		font-size: 24rpx;
		color: #666;
		margin-bottom: 10rpx;
		font-weight: 500;
	}

	.input {
		border: 2rpx solid #e0e0e0;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 26rpx;
		width: 100%;
		box-sizing: border-box;
		background: white;
		transition: all 0.3s;
	}

	.input:focus {
		border-color: #667eea;
		box-shadow: 0 0 0 3rpx rgba(102, 126, 234, 0.1);
	}

	.button-group {
		display: flex;
		gap: 20rpx;
		margin-top: 20rpx;
	}

	.btn {
		flex: 1;
		padding: 20rpx;
		border-radius: 12rpx;
		font-size: 26rpx;
		text-align: center;
		border: none;
		font-weight: 500;
		transition: all 0.3s;
	}

	.btn-connect {
		background: linear-gradient(135deg, #4CAF50, #45a049);
		color: white;
	}

	.btn-disconnect {
		background: linear-gradient(135deg, #ff6b6b, #ee5a52);
		color: white;
	}

	.btn-play {
		background: linear-gradient(135deg, #667eea, #764ba2);
		color: white;
	}

	.btn-stop {
		background: linear-gradient(135deg, #ffa726, #ff9800);
		color: white;
	}

	.btn-clear {
		background: linear-gradient(135deg, #78909c, #546e7a);
		color: white;
	}

	.btn.disabled {
		background: #bdc3c7;
		color: #7f8c8d;
		transform: none !important;
	}

	.btn:not(.disabled):active {
		transform: scale(0.98);
	}

	.status-info {
		background: #f8f9fa;
		padding: 20rpx;
		border-radius: 12rpx;
	}

	.status-item {
		display: flex;
		justify-content: space-between;
		margin-bottom: 15rpx;
		font-size: 24rpx;
	}

	.status-label {
		color: #666;
		font-weight: 500;
	}

	.status-value {
		color: #333;
		font-weight: 500;
	}

	.status-value.connected {
		color: #4CAF50;
	}

	.status-value.disconnected {
		color: #ff6b6b;
	}

	.status-value.error {
		color: #e74c3c;
		font-size: 22rpx;
	}

	.control-buttons {
		display: flex;
		gap: 15rpx;
	}

	.info-content {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
		font-size: 24rpx;
		color: #666;
	}

	.log-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.clear-log,
	.auto-scroll {
		color: #667eea;
		font-size: 24rpx;
		margin-left: 20rpx;
		padding: 8rpx 16rpx;
		background: rgba(102, 126, 234, 0.1);
		border-radius: 8rpx;
	}

	.log-content {
		height: 300rpx;
		background: #1a1a1a;
		border-radius: 12rpx;
		padding: 20rpx;
		font-family: 'Courier New', monospace;
	}

	.log-text {
		color: #00ff00;
		font-size: 22rpx;
		line-height: 1.4;
		white-space: pre-wrap;
	}
</style>