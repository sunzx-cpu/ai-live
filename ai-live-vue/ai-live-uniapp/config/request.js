// 此vm参数为页面的实例，可以通过它引用vuex中的变量
module.exports = (vm) => {
	// 初始化请求配置
	uni.$u.http.setConfig((config) => {
    config.custom.catch = true;
    config.header['Accept-Language'] = 'en-ZH';
		// config.baseURL = 'http://117.72.100.80:8201/ai-live-api'
		config.baseURL = 'http://localhost:8080/ai-live-api'
		// config.websocketURL = 'ws://117.72.100.80:8201/ai-live-api/websocket/liveInfo'
		config.websocketURL = 'ws://localhost:8080/ai-live-api/websocket/liveInfo'
		return config
	})

	// 请求拦截
	uni.$u.http.interceptors.request.use((config) => { // 可使用async await 做异步操作
		// 初始化请求拦截器时，会执行此方法，此时data为undefined，赋予默认{}
		config.data = config.data || {}
		// 根据custom参数中配置的是否需要token，添加对应的请求头
		if (vm.$store.state?.vuex_token) {
			// 可以在此通过vm引用vuex中的变量，具体值在vm.$store.state中
			config.header.token = vm.$store.state.vuex_token;
		}
		return config
	}, config => { // 可使用async await 做异步操作
		return Promise.reject(config)
	})

	// 响应拦截
	uni.$u.http.interceptors.response.use((response) => {
		/* 对响应成功做点什么 可使用async await 做异步操作*/
		const data = response.data
		// 自定义参数
		const custom = response.config?.custom
		if (data.code !== 0) {
			if(data.code === 401 || data.code === 10020 || data.code === 10021 || data.code === 10022){
        vm.$u.vuex("vuex_token", '');
        vm.$u.vuex("vuex_user", {});
        
        uni.showToast({
        	title: 'Unauthorized',
          duration: 1500,
          mask: true,
        	icon: "none",
        	complete:()=>{
            setTimeout(()=>{
              uni.reLaunch({
                url: '/pages/me/login/index'
              });
            },1500);
        	}
        });
        
			} else if(data.code == 10052) {
        uni.showToast({
        	title: 'Franchise Invalid',
          duration: 1500,
          mask: true,
        	icon: "none",
        	complete:()=>{
            setTimeout(()=>{
              vm.$u.vuex('vuex_role', 0);
              vm.$util.changeRoleType(0);
              uni.reLaunch({
                url: '/pages/home/index/index'
              });
            }, 1500);
        	}
        });
      } else if (custom.toast !== false) {
				// 如果没有显式定义custom的toast参数为false的话，默认对报错进行toast弹出提示
				uni.$u.toast(data.msg)
			}

			// 如果需要catch返回，则进行reject
			if (custom?.catch) {
				return Promise.reject(data)
			} else {
				// 否则返回一个pending中的promise，请求不会进入catch中
				return new Promise(() => {})
			}
		}
		return data;
	}, (response) => {
		// 对响应错误做点什么 （statusCode !== 200）
		return Promise.reject(response)
	})
}
