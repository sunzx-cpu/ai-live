#!/usr/bin/env node

/**
 * SSH 文件传输脚本
 * 通过 SSH/SCP 协议将打包文件传输到远程 Windows 机器
 *
 * 使用方法:
 *   node scripts/transfer-to-remote-ssh.js
 *   或 npm run transfer
 */

const fs = require('fs')
const path = require('path')
const { execSync, spawn } = require('child_process')

// ==================== 配置 ====================
const CONFIG = {
  // SSH 连接配置
  host: '192.168.31.129',
  username: 'chan',
  password: '021120',
  port: 22,

  // 目标路径（Windows 格式会自动转换）
  targetPath: 'C:\\dev',

  // 本地打包文件目录
  buildDir: path.join(__dirname, '..', 'build'),

  // 源文件匹配模式
  filePattern: /ai-live.*\.zip$/i
}

// ==================== 工具函数 ====================

const log = {
  info: (msg) => console.log(`\x1b[36m[INFO]\x1b[0m ${msg}`),
  success: (msg) => console.log(`\x1b[32m[SUCCESS]\x1b[0m ${msg}`),
  error: (msg) => console.error(`\x1b[31m[ERROR]\x1b[0m ${msg}`),
  warn: (msg) => console.warn(`\x1b[33m[WARN]\x1b[0m ${msg}`)
}

/**
 * 查找最新的打包文件
 */
function findLatestBuildFile() {
  log.info(`搜索打包文件: ${CONFIG.buildDir}`)

  if (!fs.existsSync(CONFIG.buildDir)) {
    throw new Error(`打包目录不存在: ${CONFIG.buildDir}`)
  }

  const files = fs.readdirSync(CONFIG.buildDir)
    .filter(file => CONFIG.filePattern.test(file))
    .map(file => {
      const filePath = path.join(CONFIG.buildDir, file)
      const stats = fs.statSync(filePath)
      return {
        name: file,
        path: filePath,
        size: stats.size,
        mtime: stats.mtime
      }
    })
    .sort((a, b) => b.mtime - a.mtime)

  if (files.length === 0) {
    throw new Error(`未找到匹配的打包文件 (模式: ${CONFIG.filePattern})`)
  }

  return files[0]
}

/**
 * 格式化文件大小
 */
function formatSize(bytes) {
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(2)} ${units[unitIndex]}`
}

/**
 * 检查 sshpass 是否已安装
 */
function checkSshpass() {
  try {
    execSync('which sshpass', { stdio: 'ignore' })
    return true
  } catch (error) {
    return false
  }
}

/**
 * 使用 SCP 传输文件
 */
async function transferViaSCP(sourceFile) {
  log.info('使用 SCP 传输文件...')

  const targetFile = path.basename(sourceFile.path)
  const remotePath = `${CONFIG.username}@${CONFIG.host}:${CONFIG.targetPath}\\${targetFile}`

  log.info(`源文件: ${sourceFile.path}`)
  log.info(`目标: ${CONFIG.host}:${CONFIG.targetPath}\\${targetFile}`)

  // 检查是否安装了 sshpass
  const hasSshpass = checkSshpass()

  if (!hasSshpass) {
    log.warn('未安装 sshpass，需要手动输入密码')
    log.info('提示: 可运行 "brew install hudochenkov/sshpass/sshpass" 自动输入密码')
  }

  return new Promise((resolve, reject) => {
    log.info('开始传输...\n')

    let command, args

    if (hasSshpass) {
      // 使用 sshpass 自动输入密码
      // 使用系统自带的 scp（/usr/bin/scp）而不是 Homebrew 版本
      command = 'sshpass'
      args = [
        '-p', CONFIG.password,
        '/usr/bin/scp',  // 明确使用系统 scp
        '-o', 'StrictHostKeyChecking=no',
        '-o', 'UserKnownHostsFile=/dev/null',
        '-P', CONFIG.port.toString(),
        sourceFile.path,
        remotePath
      ]
    } else {
      // 不使用 sshpass，需要手动输入密码
      // 使用系统自带的 scp
      command = '/usr/bin/scp'  // 明确使用系统 scp
      args = [
        '-o', 'StrictHostKeyChecking=no',
        '-P', CONFIG.port.toString(),
        sourceFile.path,
        remotePath
      ]
    }

    const proc = spawn(command, args, {
      stdio: ['inherit', 'pipe', 'pipe']
    })

    let output = ''
    let errorOutput = ''

    proc.stdout.on('data', (data) => {
      output += data.toString()
      process.stdout.write(data)
    })

    proc.stderr.on('data', (data) => {
      errorOutput += data.toString()
      // SCP 的进度条会输出到 stderr，所以要显示
      process.stderr.write(data)
    })

    proc.on('close', (code) => {
      console.log() // 换行
      if (code === 0) {
        log.success('文件传输完成！')
        resolve()
      } else {
        log.error('传输失败！')
        if (errorOutput) {
          log.error('错误信息: ' + errorOutput)
        }
        reject(new Error(`SCP 返回错误代码: ${code}`))
      }
    })

    proc.on('error', (error) => {
      reject(new Error(`执行 SCP 失败: ${error.message}`))
    })
  })
}

/**
 * 使用 rsync 传输文件（备用方案，支持断点续传）
 */
async function transferViaRsync(sourceFile) {
  log.info('使用 rsync 传输文件（支持断点续传）...')

  const targetFile = path.basename(sourceFile.path)
  const remotePath = `${CONFIG.username}@${CONFIG.host}:${CONFIG.targetPath}/${targetFile}`

  // 检查是否安装了 rsync
  try {
    execSync('which rsync', { stdio: 'ignore' })
  } catch (error) {
    throw new Error('未安装 rsync。请运行: brew install rsync')
  }

  // 检查是否安装了 sshpass
  const hasSshpass = checkSshpass()

  return new Promise((resolve, reject) => {
    log.info('开始传输...\n')

    let command, args

    if (hasSshpass) {
      command = 'rsync'
      args = [
        '-avz',
        '--progress',
        '-e', `sshpass -p ${CONFIG.password} ssh -o StrictHostKeyChecking=no -p ${CONFIG.port}`,
        sourceFile.path,
        remotePath
      ]
    } else {
      command = 'rsync'
      args = [
        '-avz',
        '--progress',
        '-e', `ssh -o StrictHostKeyChecking=no -p ${CONFIG.port}`,
        sourceFile.path,
        remotePath
      ]
    }

    const proc = spawn(command, args, {
      stdio: ['inherit', 'pipe', 'pipe'],
      shell: true
    })

    let output = ''
    let errorOutput = ''

    proc.stdout.on('data', (data) => {
      output += data.toString()
      process.stdout.write(data)
    })

    proc.stderr.on('data', (data) => {
      errorOutput += data.toString()
      process.stderr.write(data)
    })

    proc.on('close', (code) => {
      console.log()
      if (code === 0) {
        log.success('文件传输完成！')
        resolve()
      } else {
        log.error('传输失败！')
        if (errorOutput) {
          log.error('错误信息: ' + errorOutput)
        }
        reject(new Error(`rsync 返回错误代码: ${code}`))
      }
    })

    proc.on('error', (error) => {
      reject(new Error(`执行 rsync 失败: ${error.message}`))
    })
  })
}

// ==================== 主函数 ====================

async function main() {
  console.log('\n========================================')
  console.log('  AI Live Stream - SSH 文件传输工具')
  console.log('========================================\n')

  try {
    log.info(`目标: ${CONFIG.username}@${CONFIG.host}:${CONFIG.targetPath}`)
    log.info(`端口: ${CONFIG.port}`)

    // 查找最新的打包文件
    const sourceFile = findLatestBuildFile()

    log.success(`找到打包文件: ${sourceFile.name}`)
    log.info(`文件大小: ${formatSize(sourceFile.size)}`)
    log.info(`修改时间: ${sourceFile.mtime.toLocaleString()}`)

    console.log('\n开始传输...\n')

    // 优先使用 SCP，如果失败则尝试 rsync
    try {
      await transferViaSCP(sourceFile)
    } catch (error) {
      log.warn('SCP 传输失败，尝试使用 rsync...')
      await transferViaRsync(sourceFile)
    }

    console.log('\n========================================')
    log.success('✨ 传输完成！')
    console.log(`========================================\n`)

  } catch (error) {
    console.log('\n========================================')
    log.error('❌ 传输失败！')
    log.error(error.message)
    console.log('========================================\n')

    // 提供帮助信息
    if (error.message.includes('Connection refused')) {
      console.log('💡 可能的原因:')
      console.log('   1. SSH 服务未启动')
      console.log('   2. 端口配置错误')
      console.log('   3. 防火墙阻止连接\n')
      console.log('解决方案:')
      console.log('   查看 scripts/WINDOWS_SSH_SETUP.md\n')
    } else if (error.message.includes('Permission denied')) {
      console.log('💡 可能的原因:')
      console.log('   1. 用户名或密码错误')
      console.log('   2. SSH 密钥认证失败\n')
    } else if (error.message.includes('打包目录不存在')) {
      console.log('💡 提示: 请先运行打包命令:')
      console.log('   npm run build:win64\n')
    } else if (error.message.includes('未找到 sshpass')) {
      console.log('💡 安装 sshpass 以自动输入密码:')
      console.log('   brew install hudochenkov/sshpass/sshpass\n')
    }

    process.exit(1)
  }
}

// 运行主函数
main()
