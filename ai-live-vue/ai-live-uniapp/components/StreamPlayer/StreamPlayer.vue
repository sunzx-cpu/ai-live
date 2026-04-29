<template>
	<view class="container">
		<!-- 头部标题 -->
		<view class="header">
			<text class="title">音频流实时播放（PCM转WAV）</text>
			<text class="subtitle">零格式转换 · 智能缓冲 · 低延迟</text>
		</view>
		<!-- 连接状态 -->
		<view class="custom-card status-card">
			<view class="card-header">
				<u-icon name="server-fill" size="20" :color="connectionColor"></u-icon>
				<text class="card-title">连接状态</text>
			</view>
			<view class="card-body status-body">
				<u-tag :text="connectionStatus" :type="connectionType" size="mini" />
				<text class="status-detail">{{ connectionDetail }}</text>
			</view>
			<view class="performance-stats">
				<text class="stat">缓冲块数: {{ audioBuffer.length }}</text>
				<text class="stat">接收包数: {{ performance.packetsReceived }}</text>
				<text class="stat">缓冲健康度: {{ bufferHealth }}%</text>
			</view>
		</view>
		<!-- 音频控制 -->
		<view class="custom-card control-card">
			<view class="card-header">
				<text class="card-title">播放控制</text>
			</view>
			<view class="card-body control-buttons">
				<u-button 
					type="primary" 
					:disabled="!isConnected" 
					@click="togglePlayback"
					:icon="isPlaying ? 'pause' : 'play-right'" 
					:loading="isLoading"
					:text="playbackButtonText"
				/>
				<u-button type="warning" @click="reconnect" icon="refresh" :loading="isReconnecting">
					重连
				</u-button>
				<u-button type="success" @click="clearBuffer" icon="trash">
					清空缓冲
				</u-button>
			</view>
		</view>
		<!-- 缓冲区监控 -->
		<view class="custom-card buffer-card">
			<view class="card-header">
				<text class="card-title">缓冲区状态</text>
			</view>
			<view class="card-body buffer-content">
				<view class="buffer-info">
					<text class="buffer-label">实时缓冲: {{ audioBuffer.length }}/{{ targetBufferSize }} 块</text>
					<text class="buffer-percentage" :style="{ color: bufferHealthColor }">{{ Math.round(bufferPercentage) }}%</text>
				</view>
				<u-line-progress 
					:percentage="bufferPercentage" 
					height="12" 
					:showText="false" 
					:activeColor="bufferHealthColor" 
				/>
				<view class="buffer-stats">
					<text>网络质量: {{ networkQuality }}</text>
					<text>音频状态: {{ audioStateText }}</text>
				</view>
			</view>
		</view>
		<!-- 音频信息 -->
		<view class="custom-card info-card">
			<view class="card-header">
				<text class="card-title">流信息</text>
			</view>
			<view class="card-body info-grid">
				<view class="info-item">
					<text class="info-label">当前文本</text>
					<text class="info-value">{{ currentText || '等待数据...' }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">采样率</text>
					<text class="info-value">{{ audioConfig.sampleRate || 16000 }} Hz</text>
				</view>
				<view class="info-item">
					<text class="info-label">音频状态</text>
					<text class="info-value" :class="audioStateClass">{{ audioStateText }}</text>
				</view>
				<view class="info-item">
					<text class="info-label">数据接收</text>
					<text class="info-value">{{ performance.packetsReceived }} 包</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const http = uni.$u.http;
	import { StreamPlayer } from './StreamPlayer.js';

	export default {
		data() {
			return {
				userId: 1,
				socketTask: null,
				streamPlayer: null,
				// 音频状态
				isPlaying: false,
				isLoading: false,
				isReconnecting: false,
				// 智能缓冲管理
				audioBuffer: [],
				targetBufferSize: 10,
				minBufferSize: 3,
				maxBufferSize: 20,
				networkQuality: '良好',
				// 性能监控
				performance: { packetsReceived: 0 },
				// 界面状态
				isConnected: false,
				connectionStatus: '未连接',
				connectionType: 'info',
				connectionDetail: '等待连接...',
				currentText: '',
				audioConfig: { sampleRate: 16000, channels: 1, bitDepth: 16 },
				bufferHealth: 100
			};
		},
		computed: {
			connectionColor() {
				return this.isConnected ? '#19be6b' : '#909399';
			},
			bufferPercentage() {
				return Math.min((this.audioBuffer.length / this.targetBufferSize) * 100, 100);
			},
			bufferHealthColor() {
				if (this.bufferHealth < 30) return '#ff4d4f';
				if (this.bufferHealth < 70) return '#faad14';
				return '#19be6b';
			},
			audioStateText() {
				if (!this.isConnected) return '未连接';
				if (this.streamPlayer && this.streamPlayer.isPlaying) return '播放中';
				if (this.audioBuffer.length < this.minBufferSize) return '缓冲中...';
				return '就绪';
			},
			audioStateClass() {
				if (this.streamPlayer && this.streamPlayer.isPlaying) return 'state-playing';
				if (this.audioBuffer.length < this.minBufferSize) return 'state-buffering';
				return 'state-ready';
			},
			playbackButtonText() {
				return this.isPlaying ? '暂停播放' : '开始播放';
			}
		},
		async mounted() {
			await this.initAudioSystem();
			this.connectWebSocket();
		},
		methods: {
			// 初始化高性能音频系统
			async initAudioSystem() {
				try {
					if (this.streamPlayer) {
						this.streamPlayer.destroy();
					}
					this.streamPlayer = new StreamPlayer({
						inputSampleRate: this.audioConfig.sampleRate,
						numChannels: this.audioConfig.channels,
						bitDepth: this.audioConfig.bitDepth,
						onStateChange: (state) => {
							this.isPlaying = state === 'playing';
						}
					});
					console.log('✅ 音频系统初始化完成');
				} catch (error) {
					console.error('音频系统初始化失败:', error);
				}
			},

			// 连接WebSocket
			connectWebSocket() {
				this.updateConnectionStatus('连接中...', 'warning', '建立WebSocket连接...');
				this.isReconnecting = true;
				this.socketTask = uni.connectSocket({
					url: `${http.config.websocketURL}/${this.userId}`,
					success: () => {
						console.log('WebSocket连接中...');
					}
				});
				this.socketTask.onOpen(() => {
					console.log('✅ WebSocket已连接');
					this.updateConnectionStatus('已连接', 'success', '连接正常');
					this.isReconnecting = false;
				});
				this.socketTask.onMessage(async (res) => {
					try {
						const message = JSON.parse(res.data);
						await this.handleAudioMessage(message);
					} catch (error) {
						console.error('消息处理错误:', error);
					}
				});
				this.socketTask.onError((err) => {
					console.error('WebSocket错误:', err);
					this.handleConnectionError();
				});
				this.socketTask.onClose(() => {
					console.log('WebSocket连接关闭');
					this.handleConnectionClose();
				});
			},

			// 处理音频消息
			async handleAudioMessage(message) {
				const type = message.type;
				switch (type) {
					case 'audio_stream_start':
						await this.handleStreamStart(message);
						break;
					case 'audio_stream_data':
						await this.handleStreamData(message);
						break;
					case 'audio_stream_end':
						this.handleStreamEnd(message);
						break;
				}
			},

			// 处理流开始
			async handleStreamStart(message) {
				console.log('🎬 音频流开始');
				this.currentText = message.text;
				this.audioConfig = { ...this.audioConfig, ...message.audioConfig };
				this.audioBuffer = [];
				this.performance.packetsReceived = 0;

				// 重新初始化播放器以应用新的音频配置
				await this.initAudioSystem();
			},

			// 处理音频数据
			async handleStreamData(message) {
				if (!this.currentText) return;
				this.performance.packetsReceived++;
				try {
					const pcmData = this.base64ToArrayBuffer(message.data);

					// 智能缓冲管理
					this.audioBuffer.push(pcmData);
					this.dynamicBufferAdjustment();

					// 当缓冲区达到最小大小时开始播放
					if (this.audioBuffer.length >= this.minBufferSize && this.streamPlayer) {
						const chunksToPlay = this.audioBuffer.splice(0, this.audioBuffer.length);
						chunksToPlay.forEach(chunk => this.streamPlayer.appendChunk(chunk));
					}
				} catch (error) {
					console.error('音频数据处理错误:', error);
				}
			},

			// 处理流结束
			handleStreamEnd(message) {
				console.log('🏁 音频流结束');

				// 播放剩余缓冲数据
				if (this.audioBuffer.length > 0 && this.streamPlayer) {
					const chunksToPlay = this.audioBuffer.splice(0, this.audioBuffer.length);
					chunksToPlay.forEach(chunk => this.streamPlayer.appendChunk(chunk));
				}

				this.currentText = '';
			},

			// 动态缓冲区调整
			dynamicBufferAdjustment() {
				const currentSize = this.audioBuffer.length;
				let newNetworkQuality = this.networkQuality;
				let newTargetBufferSize = this.targetBufferSize;

				if (currentSize < this.minBufferSize) {
					newNetworkQuality = '较差';
					newTargetBufferSize = Math.min(this.maxBufferSize, 12);
				} else if (currentSize < this.targetBufferSize) {
					newNetworkQuality = '一般';
					newTargetBufferSize = Math.min(10, this.maxBufferSize);
				} else {
					newNetworkQuality = '良好';
					newTargetBufferSize = Math.max(this.minBufferSize, 6);
				}

				// 批量更新状态以减少DOM操作
				if (newNetworkQuality !== this.networkQuality || newTargetBufferSize !== this.targetBufferSize) {
					this.networkQuality = newNetworkQuality;
					this.targetBufferSize = newTargetBufferSize;
				}

				// 计算缓冲健康度
				this.bufferHealth = Math.min(100, Math.round((currentSize / this.targetBufferSize) * 100));
			},

			// 切换播放状态
			togglePlayback() {
				if (!this.isConnected) return;
				if (this.isPlaying) {
					this.pausePlayback();
				} else {
					this.resumePlayback();
				}
			},

			// 暂停播放
			pausePlayback() {
				if (this.streamPlayer) {
					this.streamPlayer.pause();
					this.isPlaying = false;
				}
			},

			// 恢复播放
			resumePlayback() {
				if (this.audioBuffer.length > 0 && this.streamPlayer) {
					const chunksToPlay = this.audioBuffer.splice(0);
					chunksToPlay.forEach(chunk => this.streamPlayer.appendChunk(chunk));
				}
			},

			// 停止播放（清空缓冲）
			stopPlayback() {
				if (this.streamPlayer) {
					this.streamPlayer.pause();
					this.isPlaying = false;
				}
			},

			// 清空缓冲区
			clearBuffer() {
				this.audioBuffer = [];
				console.log('缓冲区已清空');
			},

			// 重新连接
			async reconnect() {
				this.stopPlayback();
				this.audioBuffer = [];
				if (this.socketTask) {
					this.socketTask.close();
				}
				// 重新初始化播放器
				await this.initAudioSystem();
				this.connectWebSocket();
			},

			// 处理连接错误
			handleConnectionError() {
				this.handleConnection('连接错误', 'error', '连接发生错误');
			},

			// 处理连接关闭
			handleConnectionClose() {
				this.handleConnection('已断开', 'info', '连接已断开');
			},

			// 更新连接状态
			updateConnectionStatus(status, type, detail) {
				this.connectionStatus = status;
				this.connectionType = type;
				this.connectionDetail = detail;
			},

			// 处理连接状态变化
			handleConnection(status, type, detail) {
				this.isConnected = false;
				this.updateConnectionStatus(status, type, detail);
				this.isReconnecting = false;
				this.stopPlayback();
			},

			// Base64转ArrayBuffer
			base64ToArrayBuffer(base64) {
				const binaryString = atob(base64);
				const bytes = new Uint8Array(binaryString.length);
				for (let i = 0; i < binaryString.length; i++) {
					bytes[i] = binaryString.charCodeAt(i);
				}
				return bytes.buffer;
			}
		},
		onUnload() {
			// 清理资源
			if (this.socketTask) {
				this.socketTask.close();
			}
			if (this.streamPlayer) {
				this.streamPlayer.destroy();
			}
			console.log('资源已清理');
		}
	};
</script>

<style scoped>
	/* 样式保持不变 */
	.container {
		padding: 20rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		min-height: 100vh;
	}
	.header {
		text-align: center;
		margin-bottom: 30rpx;
		padding: 20rpx;
	}
	.title {
		display: block;
		font-size: 36rpx;
		font-weight: bold;
		color: white;
		margin-bottom: 8rpx;
	}
	.subtitle {
		display: block;
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
	}
	.custom-card {
		background: rgba(255, 255, 255, 0.95);
		backdrop-filter: blur(10px);
		border-radius: 20rpx;
		margin-bottom: 24rpx;
		overflow: hidden;
		box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
	}
	.card-header {
		padding: 24rpx 32rpx;
		border-bottom: 1rpx solid rgba(0, 0, 0, 0.1);
		display: flex;
		align-items: center;
	}
	.card-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #2c3e50;
		margin-left: 12rpx;
	}
	.card-body {
		padding: 32rpx;
	}
	.status-body {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 16rpx;
	}
	.performance-stats {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		padding-top: 16rpx;
		border-top: 1rpx solid rgba(0, 0, 0, 0.1);
	}
	.stat {
		font-size: 24rpx;
		color: #666;
	}
	.control-buttons {
		display: flex;
		gap: 20rpx;
		flex-wrap: wrap;
	}
	.buffer-content {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}
	.buffer-info {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}
	.buffer-label {
		font-size: 26rpx;
		color: #333;
	}
	.buffer-percentage {
		font-size: 24rpx;
		font-weight: bold;
	}
	.buffer-stats {
		display: flex;
		justify-content: space-between;
		font-size: 22rpx;
		color: #666;
	}
	.info-grid {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 20rpx;
	}
	.info-item {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}
	.info-label {
		font-size: 24rpx;
		color: #666;
	}
	.info-value {
		font-size: 26rpx;
		color: #333;
		font-weight: 500;
	}
	.state-playing {
		color: #19be6b;
	}
	.state-buffering {
		color: #ff976a;
	}
	.state-ready {
		color: #1989fa;
	}
	/* 响应式设计 */
	@media (max-width: 768px) {
		.info-grid {
			grid-template-columns: 1fr;
		}
		.control-buttons {
			flex-direction: column;
		}
	}
</style>
