<template>
	<view class="container">
		<view class="header">
			<view class="change" v-if="info.agentStatus == 2" @click="changeRole">
				<image src="@/static/images/mine-change.png" />
				<text v-if="this.vuex_role===0">To Marketer</text>
				<text v-if="this.vuex_role===1">To Customer</text>
			</view>
			<view class="user">
				<image src="@/static/images/logo.png" />
				<text>{{info.username}}</text>
			</view>
		</view>
		<view class="list">
			<template v-if="this.vuex_role===0">
				<view class="list-item" @click="gotoOrder">
					<image src="@/static/images/me-order.png" />
					<text>{{this.vuex_role===1 ? 'Visits Center': 'Order Center'}}</text>
				</view>
				<view class="list-item" @click="gotoJoinAgent">
					<image src="@/static/images/me-join.png" />
					<text>Become a Marketer</text>
				</view>
				<view class="list-item" @click="gotoAddress">
					<image src="@/static/images/me-address.png" />
					<text>Address Book</text>
				</view>
			</template>
			<template v-else>
				<view class="list-item" @click="gotoOrderAgentOrder">
					<image src="@/static/images/me-order.png" />
					<text>Order Center</text>
				</view>
				<view class="list-item" @click="gotoLeagueAgentOrder">
					<image src="@/static/images/tabBar-agent-order.png" />
					<text>Customer Visit</text>
				</view>
			</template>

			<view class="list-item" @click="$u.route('/pages/me/setting/setting')">
				<image src="@/static/images/setting.png" />
				<text>Setting</text>
			</view>
		</view>
		<view class="exit" @click="exit">退出登录</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				info: {},
			}
		},
		onShow() {
			if (!this.vuex_token) {
				uni.reLaunch({
					url: '/pages/me/login/index'
				})
				return
			}
			this.prifile();
		},
		methods: {
			gotoOrder() {
				uni.reLaunch({
					url: '/pages/order/userOrder/index'
				})
			},
			gotoJoinAgent() {
				uni.reLaunch({
					url: '/pages/league/index/index'
				})
			},
			gotoAddress() {
				uni.navigateTo({
					url: '/pages/me/addressManagement/index'
				})
			},
			gotoOrderAgentOrder() {
				uni.reLaunch({
					url: '/pages/order/agentOrder/index'
				})
			},
			gotoLeagueAgentOrder() {
				uni.reLaunch({
					url: '/pages/league/agentOrder/index'
				})
			},
			changeRole() {
				this.$api.userProfile().then(({
					data
				}) => {
					this.info = data;

					if (this.vuex_role == 0 && this.info.agentStatus != 2) {
						this.$util.submitModal('Notification', 'Agent is valid')
						return
					}

					if (this.vuex_role === 0) {
						this.$u.vuex('vuex_role', 1);
					} else {
						this.$u.vuex('vuex_role', 0);
					}
					this.$util.changeRoleType(this.vuex_role)
					this.$util.showToastSuc('Change Role Success')
				})

			},
			prifile() {
				this.$api.userProfile().then(({
					data
				}) => {
					this.info = data;
				})
			},
			exit() {
				this.$util.showModal('Notification', 'Continue Logging out?', () => {
					this.$u.vuex("vuex_token", '');
					this.$u.vuex("vuex_user", {});
					uni.reLaunch({
						url: '/pages/me/login/index'
					})
				})
			}
		}
	}
</script>

<style lang="scss">
	.container {
		.header {
			height: 650rpx;
			background: url("@/static/images/mine-head-bg.png");
			background-size: 100% 100%;
			padding-top: var(--status-bar-height);

			.change {
				font-size: 32rpx;
				color: #FFFFFF;
				display: flex;
				align-items: center;
				float: right;
				margin: 30px 40rpx 0 0;

				image {
					width: 34rpx;
					height: 34rpx;
					margin-right: 12rpx;
				}
			}

			.user {
				display: flex;
				flex-direction: column;
				justify-content: center;
				align-items: center;
				clear: both;
				padding-top: 70rpx;
				font-size: 34rpx;
				color: #FFFFFF;

				image {
					border-radius: 50%;
					border: 8rpx solid rgba(255, 255, 255, 0.3);
					width: 172rpx;
					height: 172rpx;
					object-fit: cover;
					margin-bottom: 40rpx;
				}
			}
		}

		.list {
			margin-top: -80rpx;
			padding: 0 40rpx 0 40rpx;

			.list-item {
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

				image {
					width: 55rpx;
					height: 55rpx;
					margin-right: 40rpx;
				}
			}
		}

		.exit {
			width: 320rpx;
			height: 112rpx;
			background: $uni-color-primary;
			border-radius: 56rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 30rpx;
			color: #fff;
			margin: 0 auto;
		}
	}
</style>