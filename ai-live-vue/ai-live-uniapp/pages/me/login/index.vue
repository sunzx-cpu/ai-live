<template>
	<view class="container">
		<view class="main">
			<view class="title">登录</view>
			<view class="form-wrap">
				<view class="form-item">
					<view class="form-label">用户名</view>
					<input type="text" v-model="form.username" placeholder="用户名">
				</view>
				<view class="form-item">
					<view class="form-label">密码</view>
					<input type="password" v-model="form.password" placeholder="密码">
				</view>
			</view>
			<view class="forgot" @click="$u.route('pages/me/forgetPassword/index')">忘记密码?</view>
			<button class="submit" @click="submit" :disabled="loading" :loading="loading">登 录</button>
		</view>
		<view class="hint">还没有账号? <text @click="$u.route('pages/me/register/index')">注册</text></view>
	</view>
</template>

<script>
	import {
		checkUpdate
	} from "@/components/yzhua006-update/js/app-update-check.js";

	export default {
		data() {
			return {
				loading: false,
				form: {
					username: 'test',
					password: '123456'
				}
			}
		},
		onLoad() {
			// this.checkAppUpdate();
		},
		methods: {
			submit() {
				if (!this.form.username || !this.form.password) {
					this.$util.showToast('用户名或密码不能为空');
					return;
				}
				this.loading = true
				this.$api.login(this.form).then(res => {
					this.$u.vuex("vuex_token", res.token);
					this.$api.userProfile().then(ress => {
						this.$u.vuex("vuex_user", ress.user)
						this.$util.showToastSuc("登录成功", () => {
							uni.reLaunch({
								url: "/pages/home/index/index"
							})
						})
					})
				}).finally(() => {
					this.loading = false
				})
			},

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
		padding: 150rpx 40rpx 40rpx;
		background: url("@/static/images/login-bg.png") no-repeat;
		background-size: 100% 380rpx;

		.main {
			padding: 60rpx;
			background: #fff;
			border-radius: 32rpx;

			.title {
				font-size: 40rpx;
				text-align: center;
				color: $uni-color-primary;
				padding-top: 60rpx;
			}

			.form-wrap {
				margin-top: 90rpx;

				.form-item {
					margin-bottom: 40rpx;

					.form-label {
						font-size: 28rpx;
						text-indent: 30rpx;
						color: #A5A5A5;
					}

					input {
						margin-top: 30rpx;
						background: #F5F9FA;
						border: 2rpx solid #E8EEF6;
						border-radius: 44rpx;
						height: 88rpx;
						line-height: 88rpx;
						padding: 0 20rpx;
					}
				}
			}

			.forgot {
				font-size: 24rpx;
				color: $uni-color-primary;
				text-align: right;
			}

			.submit {
				width: 320rpx;
				height: 112rpx;
				background: $uni-color-primary;
				border-radius: 56rpx;
				font-size: 30rpx;
				color: #fff;
				margin: 80rpx auto;
				display: flex;
				align-items: center;
				justify-content: center;
			}
		}

		.hint {
			text-align: center;
			margin: 40rpx 0;
			font-size: 28rpx;
			color: #333;

			text {
				color: $uni-color-primary;
				padding-left: 5rpx;
			}
		}
	}
</style>