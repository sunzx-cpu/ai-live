// StreamPlayer.js
export class StreamPlayer {
	constructor({
		inputSampleRate = 16000,
		numChannels = 1,
		bitDepth = 16,
		littleEndian = true,
		pcmType = 'int',
		onStateChange = () => {}
	} = {}) {
		this.audioContext = new (window.AudioContext || window.webkitAudioContext)({ latencyHint: 'playback' });
		this.inputSampleRate = inputSampleRate;
		this.numChannels = numChannels;
		this.bitDepth = bitDepth;
		this.littleEndian = littleEndian;
		this.pcmType = pcmType;
		this.audioQueue = [];
		this.playbackEndTime = 0;
		this.currentSource = null;
		this.lastBlockTail = null;
		this.isPlaying = false;
		this.onStateChange = onStateChange;
	}

	// 添加音频数据块
	appendChunk(audioData) {
		this.audioQueue.push(audioData);
		if (!this.isPlaying) {
			this._processQueue();
		}
	}

	// 处理音频队列
	async _processQueue() {
		if (this.isPlaying || this.audioQueue.length === 0) return;
		this.isPlaying = true;
		this.onStateChange('playing');
		try {
			while (this.audioQueue.length > 0) {
				const buffer = this._mergeArrayBuffers(this.audioQueue);
				this.audioQueue = [];
				const audioBuffer = this._convertPCM(buffer);
				await this._schedulePlay(audioBuffer);
			}
		} catch (error) {
			console.error('音频处理失败:', error);
		} finally {
			this.isPlaying = false;
			this.onStateChange('idle');
		}
	}

	// PCM数据转换（核心优化）
	_convertPCM(buffer) {
		const bytesPerSample = this.bitDepth / 8;
		const requiredBytes = bytesPerSample * this.numChannels;
		const validByteLength = Math.floor(buffer.byteLength / requiredBytes) * requiredBytes;
		const validBuffer = buffer.slice(0, validByteLength);
		const dataView = new DataView(validBuffer);
		const numSamples = validByteLength / requiredBytes;

		// 创建音频缓冲区
		const audioBuffer = this.audioContext.createBuffer(this.numChannels, numSamples, this.inputSampleRate);

		// 解析PCM数据并应用优化
		for (let ch = 0; ch < this.numChannels; ch++) {
			const channelData = audioBuffer.getChannelData(ch);

			// 1. 解析原始数据
			for (let i = 0; i < numSamples; i++) {
				const pos = (i * this.numChannels * bytesPerSample) + (ch * bytesPerSample);
				let value;
				if (this.bitDepth === 16) {
					value = dataView.getInt16(pos, this.littleEndian) / 32768;
				} else {
					if (this.pcmType === 'int') {
						value = dataView.getInt32(pos, this.littleEndian) / 2147483648;
					} else {
						value = dataView.getFloat32(pos, this.littleEndian);
					}
				}
				channelData[i] = value;
			}

			// 2. 消除直流偏移
			let sum = 0;
			for (let i = 0; i < channelData.length; i++) {
				sum += channelData[i];
			}
			const dcOffset = sum / channelData.length;
			for (let i = 0; i < channelData.length; i++) {
				channelData[i] -= dcOffset;
			}

			// 3. 块内淡入淡出（减少边界突变）
			const fadeLength = Math.min(100, Math.floor(channelData.length * 0.1));
			for (let i = 0; i < fadeLength; i++) {
				channelData[i] *= (i / fadeLength);
			}
			const startFadeOut = Math.max(0, channelData.length - fadeLength);
			for (let i = startFadeOut; i < channelData.length; i++) {
				const factor = 1 - (i - startFadeOut) / fadeLength;
				channelData[i] *= factor;
			}
		}

		// 4. 块间交叉淡入淡出（解决重音关键点）
		const crossfadeLength = 50;
		if (this.lastBlockTail && this.lastBlockTail.length === this.numChannels) {
			for (let ch = 0; ch < this.numChannels; ch++) {
				const channelData = audioBuffer.getChannelData(ch);
				const prevTail = this.lastBlockTail[ch];
				if (prevTail && prevTail.length >= crossfadeLength && channelData.length >= crossfadeLength) {
					for (let i = 0; i < crossfadeLength; i++) {
						const prevWeight = (crossfadeLength - i) / crossfadeLength;
						const currWeight = i / crossfadeLength;
						channelData[i] = channelData[i] * currWeight + prevTail[i] * prevWeight;
					}
				}
			}
		}

		// 保存当前块尾部用于下一次交叉
		this.lastBlockTail = [];
		for (let ch = 0; ch < this.numChannels; ch++) {
			const channelData = audioBuffer.getChannelData(ch);
			const tailStart = Math.max(0, channelData.length - crossfadeLength);
			this.lastBlockTail[ch] = channelData.slice(tailStart);
		}
		return audioBuffer;
	}

	// 调度播放
	_schedulePlay(audioBuffer) {
		return new Promise(resolve => {
			const source = this.audioContext.createBufferSource();
			source.buffer = audioBuffer;
			source.connect(this.audioContext.destination);
			const plannedStartTime = this.playbackEndTime;
			const now = this.audioContext.currentTime;
			const startTime = Math.max(plannedStartTime, now);
			source.start(startTime);
			this.playbackEndTime = startTime + audioBuffer.duration;
			source.onended = () => {
				this.currentSource = null;
				resolve();
			};
			this.currentSource = source;
		});
	}

	// 合并ArrayBuffer
	_mergeArrayBuffers(buffers) {
		let totalLength = 0;
		for (let buffer of buffers) {
			totalLength += buffer.byteLength;
		}
		const result = new Uint8Array(totalLength);
		let offset = 0;
		for (let buffer of buffers) {
			result.set(new Uint8Array(buffer), offset);
			offset += buffer.byteLength;
		}
		return result.buffer;
	}

	// 暂停播放
	pause() {
		this.isPlaying = false;
		this.audioQueue = [];
		if (this.currentSource) {
			this.currentSource.stop();
			this.currentSource = null;
		}
		this.onStateChange('paused');
	}

	// 清理资源
	destroy() {
		this.audioQueue = [];
		this.playbackEndTime = 0;
		if (this.currentSource) {
			this.currentSource.stop();
			this.currentSource = null;
		}
		this.isPlaying = false;
	}
}
