export const UseStartupChart = true
export const IsUseSysTitle = false
export const BuiltInServerPort = 25565
export const hotPublishUrl = ""
export const hotPublishConfigName = "update-config"

// 🎯 根据构建类型决定是否自动打开开发者工具
// 开发版本（npm run build:win64:dev）: 自动打开
// 生产版本（npm run build:win64）: 不自动打开，但可以通过菜单或F12手动打开
export const isDevBuild = process.env.DEV_BUILD === 'true'
export const openDevTools = true  // 始终允许开发者工具（可以手动打开）
export const autoOpenDevTools = isDevBuild  // 是否自动打开开发者工具
export const DisableF12 = false    // 不禁用F12