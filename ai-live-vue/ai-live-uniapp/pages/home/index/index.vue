<template>
	<view class="container">
		<!-- 音频流TTS -->
		<StreamPlayerApp></StreamPlayerApp>
	</view>
</template>

<script>
	import {
		checkUpdate
	} from "@/components/yzhua006-update/js/app-update-check.js";
	import StreamPlayer from "@/components/StreamPlayer/StreamPlayer.vue";
	import StreamPlayerApp from "@/components/StreamPlayer/StreamPlayerApp.vue";

	export default {
		components: {
			StreamPlayer,
			StreamPlayerApp
		},
		data() {
			return {}
		},
		onLoad() {
			// this.checkAppUpdate();
		},
		onShow() {
			if (!this.vuex_token) {
				setTimeout(() => {
					this.$util.showToastSuc("请先登录", () => {
						uni.navigateTo({
							url: '/pages/me/login/index'
						})
					})
				}, 200)
			}
		},
		methods: {
			checkAppUpdate() {
				this.$api.appversion_android({}).then(({
					data
				}) => {
					if (data) {
						// #ifdef APP-PLUS
						plus.runtime.getProperty(plus.runtime.appid, (wgtInfo) => {
							if (wgtInfo.versionCode < data.versionCode) {
								setTimeout(() => {
									let info = {
										version: data.version, //线上版本
										now_url: data.url, //更新链接
										silent: 0, //是否是静默更新
										force: 1, //是否是强制更新
										net_check: 0, //非WIfi是否提示
										note: data.content, //更新内容
									}
									checkUpdate(info, 0).then(res => {
										if (res.msg) {
											plus.nativeUI.toast(res.msg);
										}
									});
								}, 200)
							}
						});
						// #endif
					}
				})
			},
		}
	}
</script>

<style lang="scss">
	.container {
		padding: calc(28rpx + var(--status-bar-height)) 40rpx 28rpx 40rpx;
		color: #333333;
		position: relative;
	}
</style>