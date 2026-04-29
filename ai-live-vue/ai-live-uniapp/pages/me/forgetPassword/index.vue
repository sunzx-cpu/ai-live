<template>
	<view class="container">
		<u-icon name="arrow-leftward" color="#fff" size="28" @click="$u.route({type:'back'})" />
		<view class="main">
			<view class="title">忘记密码？</view>
			<view class="explain">输入您的手机号找回密码</view>
			<view class="form-wrap">
				<view class="form-item">
					<view class="form-label">手机号</view>
					<input type="text" v-model="phone" placeholder="手机号">
				</view>
			</view>
			<button class="submit" @click="gotoVerificationCode" :disabled="loading" :loading="loading">继续</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				loading: false,
				phone: ''
			}
		},
		methods: {
			gotoVerificationCode() {
				if (!this.email) {
					this.$util.showToast('Please Enter Email')
					return
				}

				this.loading = true
				this.$api.codeForgetPassword({
					email: this.email
				}).then(res => {
					uni.navigateTo({
						url: '/pages/me/verificationCode/index?email=' + this.email
					})
				}).finally(() => {
					this.loading = false
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
		position: relative;

		.u-icon {
			position: absolute;
			left: 40rpx;
			top: calc(40rpx + var(--status-bar-height));
		}

		.main {
			padding: 60rpx 60rpx 200rpx;
			background: #fff;
			border-radius: 32rpx;

			.title {
				font-size: 40rpx;
				text-align: center;
				color: $uni-color-primary;
				padding-top: 60rpx;
			}

			.explain {
				width: 550rpx;
				text-align: center;
				font-size: 24rpx;
				color: #A5A5A5;
				margin-top: 24rpx;
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
	}
</style>