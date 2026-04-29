import App from './App'

import uView from '@/uni_modules/uview-ui'
Vue.use(uView)

let vuexStore = require("@/store/$u.mixin.js");
Vue.mixin(vuexStore);
import store from '@/store';

import util from '@/config/utils.js';
import api from '@/config/api.js';
Vue.prototype.$util = util;
Vue.prototype.$api = api;

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
	store,
    ...App
})
require('@/config/request.js')(app)
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif