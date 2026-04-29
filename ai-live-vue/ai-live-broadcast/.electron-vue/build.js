'use strict'
process.env.NODE_ENV = 'production'
const { say } = require('cfonts')
const chalk = require('chalk')
const del = require('del')
const webpack = require('webpack')
const { Listr } = require('listr2')
const fs = require('fs-extra')
const path = require('path')


const mainConfig = require('./webpack.main.config')
const rendererConfig = require('./webpack.renderer.config')

const doneLog = chalk.bgGreen.white(' DONE ') + ' '
const errorLog = chalk.bgRed.white(' ERROR ') + ' '
const okayLog = chalk.bgBlue.white(' OKAY ') + ' '
const isCI = process.env.CI || false

if (process.env.BUILD_TARGET === 'web') web()
else build()

function clean() {
  del.sync(['dist/electron/*', 'build/*', '!build/icons', '!build/lib', '!build/lib/electron-build.*', '!build/icons/icon.*'])
  // 清理构建时创建的临时文件夹
  // 注意：不删除 extra 目录，因为用户可能手动放置了文件
  // prepareExtraFiles() 会根据需要处理 extra 目录
  del.sync(['audio_temp', 'backup_temp'])
  console.log(`\n${doneLog}clear done`)
  if (process.env.BUILD_TARGET === 'onlyClean') process.exit()
}

/**
 * 准备打包所需的文件夹和文件
 */
function prepareExtraFiles() {
  const projectRoot = path.resolve(__dirname, '..')
  const localExtraDir = path.join(projectRoot, 'extra')

  // 网络路径列表（按优先级尝试）
  const networkExtraPaths = [
    '/Volumes/192.168.31.136-1/AI直播系统 Setup 3.3.5/FLYAILIVE/extra',
    '/Volumes/192.168.31.136/FLYAILIVE/extra',
    '/Volumes/192.168.31.132/software/FLYAILIVE/extra'
  ]

  console.log(`\n${okayLog}准备打包文件夹...`)

  // 创建 audio_temp 和 backup_temp 文件夹
  fs.ensureDirSync(path.join(projectRoot, 'audio_temp'))
  fs.ensureDirSync(path.join(projectRoot, 'backup_temp'))
  console.log(`  ${doneLog}创建 audio_temp 和 backup_temp 文件夹`)

  // 文件计数函数
  const countFiles = (dir) => {
    let count = 0
    try {
      const items = fs.readdirSync(dir)
      items.forEach(item => {
        const fullPath = path.join(dir, item)
        const stat = fs.statSync(fullPath)
        if (stat.isFile()) {
          count++
        } else if (stat.isDirectory()) {
          count += countFiles(fullPath)
        }
      })
    } catch (error) {
      // 目录不存在或无权限
    }
    return count
  }

  // 检查本地 extra 目录是否已存在且有内容
  if (fs.existsSync(localExtraDir)) {
    const fileCount = countFiles(localExtraDir)
    if (fileCount > 0) {
      console.log(`  ${doneLog}检测到本地 extra 目录已存在`)
      console.log(`  ${okayLog}目录路径: ${localExtraDir}`)
      console.log(`  ${okayLog}文件数量: ${fileCount} 个`)
      console.log(`  ${chalk.green('✓')} 跳过复制步骤，直接使用本地文件`)
      console.log(`${doneLog}打包文件夹准备完成\n`)
      return
    } else {
      console.log(`  ${chalk.yellow('!')} 本地 extra 目录为空，将从网络路径复制`)
    }
  } else {
    console.log(`  ${chalk.yellow('!')} 本地 extra 目录不存在，将从网络路径复制`)
  }

  // 尝试从网络路径复制
  let sourceExtraDir = null
  for (const networkPath of networkExtraPaths) {
    if (fs.existsSync(networkPath)) {
      sourceExtraDir = networkPath
      console.log(`  ${okayLog}找到网络目录: ${sourceExtraDir}`)
      break
    }
  }

  if (!sourceExtraDir) {
    console.error(`  ${errorLog}未找到可用的 extra 源目录`)
    console.error(`\n  已尝试以下路径:`)
    networkExtraPaths.forEach(p => console.error(`    - ${p}`))
    console.error(`\n  ${chalk.yellow('解决方案')}:`)
    console.error(`    1. 手动将 extra 目录内容放到: ${localExtraDir}`)
    console.error(`    2. 或挂载网络目录到上述路径之一`)
    throw new Error('找不到 extra 源目录，无法继续打包')
  }

  console.log(`  ${okayLog}开始复制 extra 目录（包括所有子文件夹）...`)

  // 删除本地目标目录（如果存在但为空）
  if (fs.existsSync(localExtraDir)) {
    fs.removeSync(localExtraDir)
  }

  // 递归复制整个 extra 目录
  try {
    fs.copySync(sourceExtraDir, localExtraDir, {
      overwrite: true,
      errorOnExist: false
    })
    console.log(`  ${doneLog}复制 extra 目录完成`)

    const fileCount = countFiles(localExtraDir)
    console.log(`  ${okayLog}已复制 ${fileCount} 个文件`)
  } catch (error) {
    console.error(`  ${errorLog}复制 extra 目录失败: ${error.message}`)
    throw error
  }

  console.log(`${doneLog}打包文件夹准备完成\n`)
}

function build() {
  greeting()
  if (process.env.BUILD_TARGET === 'clean' || process.env.BUILD_TARGET === 'onlyClean') clean()
  const tasksLister = new Listr([
    {
      title: 'building main process',
      task: async (_, tasks) => {
        try {
          await pack(mainConfig)
        } catch (error) {
          console.error(`\n${error}\n`)
          console.log(`\n  ${errorLog}failed to build main process`)
          process.exit(1)
        }
      }
    },
    {
      title: "building renderer process",
      task: async (_, tasks) => {
        try {
          await pack(rendererConfig)
          tasks.output = `${okayLog}take it away ${chalk.yellow('`electron-builder`')}\n`
        } catch (error) {
          console.error(`\n${error}\n`)
          console.log(`\n  ${errorLog}failed to build renderer process`)
          process.exit(1)
        }
      },
      options: { persistentOutput: true }
    },
    {
      title: "preparing extra files for packaging",
      task: async (_, tasks) => {
        try {
          prepareExtraFiles()
        } catch (error) {
          console.error(`\n${error}\n`)
          console.log(`\n  ${errorLog}failed to prepare extra files`)
          process.exit(1)
        }
      }
    }
  ], {
    exitOnError: true
  })
  tasksLister.run()
}

function pack(config) {
  return new Promise((resolve, reject) => {
    config.mode = 'production'
    webpack(config, (err, stats) => {
      if (err) reject(err.stack || err)
      else if (stats.hasErrors()) {
        let err = ''

        stats.toString({
          chunks: false,
          colors: true
        })
          .split(/\r?\n/)
          .forEach(line => {
            err += `    ${line}\n`
          })

        reject(err)
      } else {
        resolve(stats.toString({
          chunks: false,
          colors: true
        }))
      }
    })
  })
}

function web() {
  del.sync(['dist/web/*', '!.gitkeep'])
  rendererConfig.mode = 'production'
  webpack(rendererConfig, (err, stats) => {
    if (err || stats.hasErrors()) console.log(err)

    console.log(stats.toString({
      chunks: false,
      colors: true
    }))

    process.exit()
  })
}

function greeting() {
  const cols = process.stdout.columns
  let text = ''

  if (cols > 85) text = `let's-build`
  else if (cols > 60) text = `let's-|build`
  else text = false

  if (text && !isCI) {
    say(text, {
      colors: ['yellow'],
      font: 'simple3d',
      space: false
    })
  } else console.log(chalk.yellow.bold(`\n  let's-build`))
  console.log()
}