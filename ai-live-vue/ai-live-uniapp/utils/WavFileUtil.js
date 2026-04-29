/**
 * UniApp App端WAV文件写入工具类
 */

class WavFileUtil {
	/**
	 * 写入WAV文件
	 * @param {ArrayBuffer} arrayBuffer WAV文件数据
	 * @returns {Promise<boolean>} 是否写入成功
	 */
	static saveWavFileApp(arrayBuffer, filename) {
		return new Promise((resolve, reject) => {
			// 1. 获取外部私有目录：/sdcard/Android/data/包名/files/Download
			const rootPath = '_downloads/'; // 5+ 沙盒路径，等价于外部私有 Download 目录
			const filePath = rootPath + filename;

			// 2. 创建文件并写入
			plus.io.requestFileSystem(plus.io.PUBLIC_DOWNLOADS, function(fs) {
				fs.root.getFile(filename, {
					create: true
				}, function(fileEntry) {
					fileEntry.createWriter(function(writer) {
						writer.onwrite = function() {
							console.log('文件已保存：' + filePath);
							// plus.nativeUI.toast('文件已保存：' + filePath);
							resolve(filePath)
							// 3. 打开文件（系统弹窗）
							// plus.runtime.openFile(filePath, {}, function(err) {
							//     if (err) {
							//         plus.nativeUI.toast('打开文件失败：' + err.message);
							//     }
							// });
						};

						writer.onerror = function(e) {
							console.error('写入失败', e);
						};
						console.log('开始写入', arrayBuffer)
						// ✅ 写入 ArrayBuffer
						const base64 = uni.arrayBufferToBase64(arrayBuffer)
						writer.writeAsBinary(base64);

					});
				}, function(e) {
					console.error('文件创建失败', e);
				});
			}, function(e) {
				console.error('文件系统访问失败', e);
			});
		});
	}

	/**
	 * 删除指定目录下的所有WAV文件
	 * @param {string} directoryPath - 要清空的目录路径，默认为'_downloads/'
	 * @returns {Promise<boolean>} 删除操作结果
	 */
	static deleteAllWavFilesApp(directoryPath = '_downloads/') {
	    return new Promise((resolve, reject) => {
	        plus.io.requestFileSystem(plus.io.PUBLIC_DOWNLOADS, function(fs) {
	            // 获取指定目录
	            fs.root.getDirectory(directoryPath, { create: false }, function(dirEntry) {
	                const directoryReader = dirEntry.createReader();
	                
	                // 递归读取并删除所有WAV文件
	                const readEntries = function() {
	                    directoryReader.readEntries(function(entries) {
	                        if (entries.length === 0) {
	                            console.log('所有WAV文件删除完成');
	                            resolve(true);
	                            return;
	                        }
	
	                        let deletePromises = [];
	                        
	                        entries.forEach(entry => {
	                            // 检查是否为WAV文件
	                            if (entry.isFile && entry.name.toLowerCase().endsWith('.wav')) {
	                                deletePromises.push(new Promise((resolveDelete, rejectDelete) => {
	                                    entry.remove(function() {
	                                        console.log('已删除文件:', entry.name);
	                                        resolveDelete(true);
	                                    }, function(error) {
	                                        console.warn('删除文件失败:', entry.name, error);
	                                        // 单个文件删除失败不中断整体流程
	                                        resolveDelete(false);
	                                    });
	                                }));
	                            }
	                        });
	
	                        // 等待当前批次删除操作完成
	                        Promise.all(deletePromises).then(() => {
	                            // 继续读取下一批条目
	                            readEntries();
	                        });
	                    }, function(error) {
	                        console.error('读取目录失败:', error);
	                        reject(error);
	                    });
	                };
	                // 开始读取目录
	                readEntries();
	            }, function(e) {
	                console.error('目录不存在或访问失败', e);
	                reject(e);
	            });
	        }, function(e) {
	            console.error('文件系统访问失败', e);
	            reject(e);
	        });
	    });
	}

	/**
	 * 删除临时文件
	 * @param {string} filePath 文件路径
	 * @returns {Promise<boolean>} 是否删除成功
	 */
	static deleteWavFileApp(filename) {
		return new Promise((resolve, reject) => {
			// 使用与保存时相同的目录路径
			const rootPath = '_downloads/';

			plus.io.requestFileSystem(plus.io.PUBLIC_DOWNLOADS, function(fs) {
				fs.root.getFile(filename, {
					create: false
				}, function(fileEntry) {
					// 获取文件Entry后执行删除[7,8](@ref)
					fileEntry.remove(function() {
						console.log('文件删除成功：' + filename);
						resolve(true);
					}, function(error) {
						console.error('文件删除失败：', error);
						reject(error);
					});
				}, function(e) {
					console.error('文件不存在或访问失败', e);
					reject(e);
				});
			}, function(e) {
				console.error('文件系统访问失败', e);
				reject(e);
			});
		});
	}

	// Base64转ArrayBuffer[1](@ref)
	static base64ToArrayBuffer(base64) {
		try {
			// 移除可能存在的Data URL前缀
			const base64Data = base64.replace(/^data:[^;]+;base64,/, '')
			const binaryString = atob(base64Data)
			const bytes = new Uint8Array(binaryString.length)
			for (let i = 0; i < binaryString.length; i++) {
				bytes[i] = binaryString.charCodeAt(i)
			}
			return bytes.buffer
		} catch (error) {
			this.addLog('❌ Base64解码失败: ' + error.message)
			return new ArrayBuffer(0)
		}
	}

	// ArrayBuffer转Base64
	static arrayBufferToBase64(buffer) {
		const bytes = new Uint8Array(buffer)
		let binary = ''
		for (let i = 0; i < bytes.byteLength; i++) {
			binary += String.fromCharCode(bytes[i])
		}
		return btoa(binary)
	}
}

export default WavFileUtil;