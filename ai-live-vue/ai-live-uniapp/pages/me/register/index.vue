<template>
	<view class="container">
		<view class="main">
			<view class="title">注册</view>
			<view class="form-wrap">
				<view class="form-item">
					<view class="form-label">手机号</view>
					<u-input type="text" v-model="form.phone" placeholder="手机号"></u-input>
				</view>
				<view class="form-item">
					<view class="form-label">验证码</view>
					<u-input type="text" v-model="form.code" placeholder="验证码">
						<template slot="suffix">
							<u-code ref="uCode" @change="codeChange" keep-running @start="isClickCode = true"
								@end="isClickCode = false"></u-code>
							<u-button @tap="getCode" :text="tips" type="primary" size="small"
								:disabled="isClickCode"></u-button>
						</template>
					</u-input>
				</view>
			</view>
			<view class="checkbox-outer">
				<u-checkbox-group v-model="check">
					<u-checkbox shape="circle" :value="1" activeColor="#58B9B9" />
				</u-checkbox-group>
				<view>
					通过注册，您同意我们的
					<text @click="$u.route('pages/me/policy/userPolicy')">条款与条件</text>
					和
					<text @click="$u.route('pages/me/policy/privacyPolicy')">隐私政策</text>
				</view>
			</view>
			<button class="submit" @click="submit" :disabled="loading" :loading="loading">确认</button>
		</view>
		<view class="hint">已有账号? <text @click="$u.route('pages/me/login/index')">登录</text></view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				check: [],
				loading: false,
				tips: '',
				isClickCode: false,
				form: {
					phone: '',
					smsCode: ''
				}
			}
		},
		methods: {
			codeChange(text) {
				this.tips = text;
			},
			getCode() {
				if (!this.form.phone) {
					this.$util.showToast('手机号不能为空');
					return
				}
				if (this.$refs.uCode.canGetCode) {
					uni.showLoading({
						title: '正在获取验证码'
					})
					this.$api.sendSms(this.form).then(({
						data
					}) => {
						uni.hideLoading();
						// 这里此提示会被this.start()方法中的提示覆盖
						uni.$u.toast('验证码已发送');
						// 通知验证码组件内部开始倒计时
						this.$refs.uCode.start();
					})
				} else {
					uni.$u.toast('验证码已发送');
				}
			},
			submit() {
				if (!this.form.phone || !this.form.smsCode) {
					this.$util.showToast('手机号或验证码不能为空');
					return
				}
				if (this.check.length == 0) {
					this.$util.showToast('请先同意我们的条款和条件以及隐私政策');
					return
				}
				this.loading = true
				this.$api.register(this.form).then(({
					data
				}) => {
					this.$util.submitModal('提示', '注册成功', () => {
						uni.navigateBack();
					});
				}).finally(() => {
					this.loading = false
				})
			}
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

					.u-input {
						margin-top: 30rpx;
						background: #F5F9FA;
						border: 2rpx solid #E8EEF6 !important;
						border-radius: 44rpx;
						height: 88rpx;
						line-height: 88rpx;
						padding: 0 20rpx !important;
					}
				}
			}

			.checkbox-outer {
				display: flex;
				align-items: flex-start;

				.u-checkbox {
					flex: none;
				}

				view {
					float: auto;
					font-size: 24rpx;
					color: #A5A5A5;

					text {
						color: $uni-color-primary;
						padding: 0 5rpx;
					}
				}
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