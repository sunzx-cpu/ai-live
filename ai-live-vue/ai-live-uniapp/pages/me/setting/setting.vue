<template>
	<view class="container">
		<view class="list">
      <view class="list-item" @click="$u.route('pages/me/policy/privacyPolicy')">
        <image src="@/static/images/privacyPolicy.png" />
        <text>Privacy Policy</text>
      </view>
      <view class="list-item" @click="$u.route('pages/me/policy/userPolicy')">
        <image src="@/static/images/termsConditions.png" />
        <text>Terms & Conditions</text>
      </view>
      <view class="list-item" @click="cancellationAccount">
        <image src="@/static/images/cancellation.png" />
        <text>Cancel Your Account</text>
      </view>
    </view>
    
    <view style="margin-top: 100px; text-align: center; color: #666">Version v{{version}}</view>
    
	</view>
</template>

<script>
	export default {
		data() {
			return {
        version : ''
			}
		},
    onLoad() {
      // #ifdef APP-PLUS
      plus.runtime.getProperty(plus.runtime.appid, (wgtInfo)=> {
        this.version = wgtInfo.version
      })
      // #endif
    },
		methods: {
      cancellationAccount() {
        this.$util.showModal('Notification', 'Cancel Your Account?', () => {
          uni.showLoading({
          	title: 'Loading'
          });
          this.$api.userCancellationAccount().then(({data}) => {
            this.$u.vuex("vuex_token", '');
            this.$u.vuex("vuex_user", {});
            this.$util.submitModal('Notification', 'Your request has been submited successfully. Our team will dispose within 24 hours. Thank you.', () => {
              uni.reLaunch({
                url: '/pages/home/index/index'
              })
            });
          }).finally(() => {
            uni.hideLoading()
          })
        })
      }
		}
	}
</script>

<style lang="scss">
	.container{
		.list{
			margin-top: 30rpx;
			padding: 0 40rpx 0 40rpx;
			.list-item{
				height: 132rpx;
				background: #fff;
				border: 2rpx solid #E8EEF6;
				border-radius: 66rpx;
				display: flex;
				padding: 0 60rpx;
				font-size: 28rpx;
				color: #333333;
				display: flex;
				align-items: center;
				margin-bottom: 40rpx;
				image{
					width: 55rpx;
					height: 55rpx;
					margin-right: 40rpx;
				}
			}
		}
	}
</style>
