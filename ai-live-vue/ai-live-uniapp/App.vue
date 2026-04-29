<script>
	export default {
		onLaunch: function() {
			// 设置2.4秒后主动关闭启动页，最多设置6秒
			setTimeout(() => {
				plus.navigator.closeSplashscreen();
			}, 2400);
		},
		onShow: function() {
			// console.log('App Show')
		},
		onHide: function() {
			// console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/*每个页面公共css */
	@import "@/uni_modules/uview-ui/index.scss";

	page {
		background-color: #F6F9FB;
	}

	.ipt-placeholder {
		color: #A5A5A5;
	}
</style>