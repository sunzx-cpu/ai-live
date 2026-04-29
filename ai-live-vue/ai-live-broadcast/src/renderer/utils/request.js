import axios from 'axios'
import { Message } from 'element-ui'
import { useRouter } from "@/hooks/use-router";

const router = useRouter()
console.log(process.env.userConfig)
const serves = axios.create({
  baseURL: process.env.userConfig.API_HOST,
  timeout: 50000
})

// 设置请求发送之前的拦截器
serves.interceptors.request.use(config => {
  config.headers['token'] = localStorage.getItem('token') // 请求头带上token
  // 设置发送之前数据需要做什么处理
  return config
}, err => Promise.reject(err))

// 设置请求接受拦截器
serves.interceptors.response.use(res => {
  if (res.data && res.data.code === 401) { // 401, token失效
    localStorage.setItem("token", "");
    localStorage.setItem("roles", JSON.stringify([]));
    localStorage.setItem("name", "");
    localStorage.setItem("userId", "");
    Message.error(res.data.msg)
    router.push('/login')
  }
  return res
}, err => {
  // 判断请求异常信息中是否含有超时timeout字符串
  if (err.message.includes('timeout')) {
    console.log('错误回调', err)
    Message.error('网络超时')
  }
  if (err.message.includes('Network Error')) {
    console.log('错误回调', err)
    Message.error('服务端未启动，或网络连接错误')
  }
  return Promise.reject(err)
})

// 将serves抛出去
export default serves
