import Vue from 'vue'
import VueI18n from 'vue-i18n'
import Cookies from 'js-cookie'
import zhCNLocale from 'element-ui/lib/locale/lang/zh-CN'
import enLocale from 'element-ui/lib/locale/lang/en'
import enUS from './en-US'
import zhCN from './zh-CN'

Vue.use(VueI18n)

export const messages = {
  'zh-CN': {
    '_lang': '简体中文',
    '_langChange': 'English',
    ...zhCN,
    ...zhCNLocale
  },
  'en-US': {
    '_lang': 'English',
    '_langChange': '简体中文',
    ...enUS,
    ...enLocale
  },
}

export default new VueI18n({
  locale: Cookies.get('language') || 'zh-CN',
  messages
})
