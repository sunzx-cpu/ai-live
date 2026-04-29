/**
 * 安全的二进制数据处理工具类
 */
class BinaryUtils {
	// 创建安全的DataView，避免越界访问[1](@ref)
	static createSafeDataView(buffer, byteOffset = 0, byteLength) {
		if (byteLength === undefined) {
			byteLength = buffer.byteLength - byteOffset;
		}

		// 边界检查
		if (byteOffset < 0 || byteOffset + byteLength > buffer.byteLength) {
			throw new Error(
				`DataView范围越界: offset=${byteOffset}, length=${byteLength}, bufferLength=${buffer.byteLength}`);
		}

		return new DataView(buffer, byteOffset, byteLength);
	}

	// 安全写入Uint32
	static writeUint32(view, offset, value, littleEndian = false) {
		if (offset < 0 || offset + 4 > view.byteLength) {
			throw new Error(`写入Uint32越界: offset=${offset}, viewLength=${view.byteLength}`);
		}
		view.setUint32(offset, value, littleEndian);
	}

	// 安全写入Uint16
	static writeUint16(view, offset, value, littleEndian = false) {
		if (offset < 0 || offset + 2 > view.byteLength) {
			throw new Error(`写入Uint16越界: offset=${offset}, viewLength=${view.byteLength}`);
		}
		view.setUint16(offset, value, littleEndian);
	}

	// 安全写入Uint8
	static writeUint8(view, offset, value) {
		if (offset < 0 || offset + 1 > view.byteLength) {
			throw new Error(`写入Uint8越界: offset=${offset}, viewLength=${view.byteLength}`);
		}
		view.setUint8(offset, value);
	}

	// 写入字符串到DataView
	static writeString(view, offset, string) {
		for (let i = 0; i < string.length; i++) {
			this.writeUint8(view, offset + i, string.charCodeAt(i));
		}
	}
}

export default BinaryUtils;