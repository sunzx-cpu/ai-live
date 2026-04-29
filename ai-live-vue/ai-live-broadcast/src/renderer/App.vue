<template>
  <div id="app">
    <c-header></c-header>
    <transition name="fade" mode="out-in">
      <router-view></router-view>
    </transition>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import CHeader from "./components/title"

// 应用启动时自动启动语音服务
onMounted(async () => {
  console.log('[App] 应用启动，检查是否需要自动启动语音服务')

  // 读取 AI 服务器配置
  const configStr = localStorage.getItem('aiServerConfig')
  if (!configStr) {
    console.log('[App] 未找到 AI 服务器配置，跳过自动启动')
    return
  }

  try {
    const config = JSON.parse(configStr)
    const selectedServer = config.selectedServer || 'cloud'

    // 只有选择了本地服务时才自动启动
    if (selectedServer === 'local') {
      console.log('[App] 检测到已选择本地语音服务，准备自动启动...')

      const { ipcRenderer } = require('electron')

      // 先检查服务是否已在运行
      const statusResult = await ipcRenderer.invoke('check-voice-service-status', {
        serviceType: 'local'
      })

      if (statusResult.success && statusResult.running) {
        console.log('[App] 语音服务已在运行，无需重复启动')
        return
      }

      // 启动服务
      console.log('[App] 正在启动语音服务...')
      const startResult = await ipcRenderer.invoke('start-voice-service', {
        serviceType: 'local',
        options: {
          enableSuggestion: config.enableSuggestion || false
        }
      })

      if (startResult.success) {
        console.log('[App] 语音服务自动启动成功')
      } else {
        console.error('[App] 语音服务自动启动失败:', startResult.error)
      }
    } else {
      console.log(`[App] 当前选择的服务类型为: ${selectedServer}，无需启动本地服务`)
    }
  } catch (error) {
    console.error('[App] 解析配置或启动服务时出错:', error)
  }
})

</script>

<style>
/* CSS */
</style>
