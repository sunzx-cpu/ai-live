<template>
	<view class="container">
		<u-icon name="arrow-leftward" color="#fff" size="28" @click="$u.route({type:'back'})" />
		<view class="main">
			<view class="title">Verification code</view>
			<view class="explain">An 6 digit code has been sent {{email}}</view>
			<u-code-input class="code-input" v-model="code" :maxlength="6"></u-code-input>
			<button class="submit" @click="gotoChangePassword" :disabled="loading" :loading="loading">Continue</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
        loading: false,
        
        email: '',
        code:''
			}
		},
    onLoad(e) {
      this.email = e.email
    },
		methods: {
			gotoChangePassword() {
        if(!this.email) {
          this.$util.submitModal('Notification', 'Email Error, Please Restart App')
          return
        }
        if(!this.code) {
          this.$util.showToast('Please Enter Verification Code')
          return
        }
        this.loading = true
        this.$api.codeVerifyForgetPassword({
          email: this.email,
          code: this.code
        }).then(res => {
          uni.navigateTo({
            url: '/pages/me/resetPassword/index?email=' + this.email + "&code=" + this.code
          })
        }).finally(() => {
          this.loading = false
        })
      }
		}
	}
</script>

<style lang="scss">
	.container{
		padding: 150rpx 40rpx 40rpx;
		background: url("@/static/images/login-bg.png") no-repeat;
		background-size: 100% 380rpx;
		position: relative;
		.u-icon{
			position: absolute;
			left: 40rpx;
			top: calc(40rpx + var(--status-bar-height));
		}
		.main{
			padding: 60rpx 60rpx 200rpx;
			background: #fff;
			border-radius: 32rpx;
			.title{
				font-size: 40rpx;
				text-align: center;
				color: $uni-color-primary;
				padding-top: 60rpx;
			}
			.explain{
				width: 550rpx;
				text-align: center;
				font-size: 24rpx;
				color: #A5A5A5;
				margin-top: 24rpx;
			}
			.code-input{
				margin-top: 90rpx;
				:v-deep .u-code-input__item{
					width: 76rpx !important;
					height: 96rpx !important;
				}
			}
			.submit{
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
