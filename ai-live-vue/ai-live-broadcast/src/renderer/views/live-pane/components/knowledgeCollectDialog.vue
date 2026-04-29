<template>
  <div>
    <el-dialog
      title="直播间商品知识库采集"
      :visible.sync="dialogVisible"
      width="70%"
      :close-on-click-modal="false"
      @close="handleClose">

      <!-- 使用说明 -->
      <div class="usage-tips">
        <span style="color: #606266;">使用说明：</span>
        <span style="color: #409EFF; margin-left: 10px;">
          1.输入直播间地址并打开 → 2.在下方窗口登录抖音账号 → 3.手动点击小黄车并向下浏览完所有商品 → 4.点击"商品知识库采集"按钮
        </span>
      </div>

      <!-- 直播间地址 -->
      <el-row :gutter="12" style="margin-top: 20px;">
        <el-col :span="4">
          <div class="label-text">直播间地址：</div>
        </el-col>
        <el-col :span="14">
          <el-input
            v-model="liveRoomUrl"
            placeholder="请输入抖音直播间地址"
            size="small">
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-button
            type="success"
            size="small"
            :loading="isOpeningRoom"
            @click="openLiveRoom">
            {{ isRoomOpened ? '重新打开' : '打开直播间' }}
          </el-button>
        </el-col>
      </el-row>

      <!-- webview直播间容器 -->
      <div v-if="isRoomOpened" class="webview-container">
        <webview
          ref="liveWebview"
          :src="liveRoomUrl"
          class="live-webview"
          :class="{ 'webview-disabled': isCollecting }">
        </webview>
        <!-- 采集时的遮罩层 -->
        <div v-if="isCollecting" class="webview-mask">
          <div class="mask-content">
            <i class="el-icon-loading" style="font-size: 32px; color: #409EFF;"></i>
            <div style="margin-top: 12px; font-size: 16px; color: #409EFF;">
              正在采集商品知识库，请勿操作...
            </div>
          </div>
        </div>
      </div>

      <!-- 知识库采集 -->
      <el-row :gutter="12" style="margin-top: 20px;">
        <el-col :span="4">
          <div class="label-text">知识库采集：</div>
        </el-col>
        <el-col :span="14">
          <el-button
            type="success"
            size="small"
            :loading="isCollecting"
            :disabled="!isRoomOpened"
            @click="startCollect">
            商品知识库采集
          </el-button>
          <span style="color: #f56c6c; margin-left: 10px; font-size: 12px;">
            {{ collectStatus || '请确保已在直播间中登录、打开小黄车并浏览完所有商品后再点击采集！' }}
          </span>
        </el-col>
      </el-row>

      <!-- 知识库导出 -->
      <el-row :gutter="12" style="margin-top: 20px;">
        <el-col :span="4">
          <div class="label-text">知识库导出：</div>
        </el-col>
        <el-col :span="14">
          <el-button
            type="success"
            size="small"
            :disabled="collectedProducts.length === 0"
            @click="exportKnowledge">
            商品知识库导出
          </el-button>
          <span style="color: #f56c6c; margin-left: 10px; font-size: 12px;">
            {{ exportStatus || '采集过程中请不要导出，直到采集自动结束！' }}
          </span>
        </el-col>
      </el-row>

      <!-- 采集结果显示区域 -->
      <div class="result-area">
        <div v-if="collectedProducts.length === 0" class="empty-text">
          暂无采集数据，请先打开直播间并进行商品采集
        </div>
        <el-table
          v-else
          :data="collectedProducts"
          border
          max-height="400"
          size="small">
          <el-table-column
            type="index"
            label="序号"
            width="60"
            align="center">
          </el-table-column>
          <el-table-column
            prop="id"
            label="商品链接号"
            width="120"
            align="center">
          </el-table-column>
          <el-table-column
            prop="name"
            label="商品名称"
            width="200"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="detail"
            label="商品详情"
            min-width="300"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            label="操作"
            width="100"
            align="center">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="small"
                style="color: #f56c6c"
                @click="deleteProduct(scope.$index)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="danger" @click="clearCollectedData">清空采集数据</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'KnowledgeCollectDialog',
  data() {
    return {
      dialogVisible: false,
      liveRoomUrl: '',
      isOpeningRoom: false,
      isRoomOpened: false,
      isCollecting: false,
      collectStatus: '',
      exportStatus: '',
      collectedProducts: [],
      liveRoomWindow: null  // 保存打开的直播间窗口引用
    }
  },
  watch: {
    dialogVisible(val) {
      if (val) {
        // 对话框打开时，加载上次保存的直播间地址
        this.loadLastLiveRoomUrl()
      }
    }
  },
  methods: {
    /**
     * 加载上次保存的直播间地址
     */
    loadLastLiveRoomUrl() {
      const savedUrl = localStorage.getItem('lastLiveRoomUrl')
      if (savedUrl) {
        this.liveRoomUrl = savedUrl
        console.log('[KnowledgeCollect] 加载上次的直播间地址:', savedUrl)
      }
    },

    /**
     * 保存直播间地址到localStorage
     */
    saveLiveRoomUrl() {
      if (this.liveRoomUrl) {
        localStorage.setItem('lastLiveRoomUrl', this.liveRoomUrl)
        console.log('[KnowledgeCollect] 保存直播间地址:', this.liveRoomUrl)
      }
    },
    /**
     * 打开直播间
     */
    async openLiveRoom() {
      if (!this.liveRoomUrl || !this.liveRoomUrl.trim()) {
        this.$message.warning('请输入直播间地址')
        return
      }

      // 验证URL格式
      if (!this.liveRoomUrl.startsWith('http://') && !this.liveRoomUrl.startsWith('https://')) {
        this.$message.warning('请输入完整的直播间地址（以 http:// 或 https:// 开头）')
        return
      }

      this.isOpeningRoom = true

      try {
        // 保存直播间地址
        this.saveLiveRoomUrl()

        // 设置状态
        this.isRoomOpened = true
        this.collectStatus = '直播间已打开，请在下方窗口中登录账号、打开小黄车并浏览完所有商品'

        // 等待webview加载
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 监听webview加载完成
        this.$nextTick(() => {
          const webview = this.$refs.liveWebview
          if (webview) {
            webview.addEventListener('did-finish-load', () => {
              console.log('[KnowledgeCollect] 直播间页面加载完成')
              this.$message.success('直播间已打开，请登录账号并浏览商品')
            })

            webview.addEventListener('did-fail-load', (event) => {
              console.error('[KnowledgeCollect] 页面加载失败:', event)
              this.$message.error('页面加载失败，请检查网络连接')
            })
          }
        })

      } catch (error) {
        console.error('[KnowledgeCollect] 打开直播间失败:', error)
        this.$message.error('打开直播间失败: ' + error.message)
        this.isRoomOpened = false
      } finally {
        this.isOpeningRoom = false
      }
    },

    /**
     * 开始采集商品信息
     */
    async startCollect() {
      if (!this.isRoomOpened) {
        this.$message.warning('请先打开直播间')
        return
      }

      this.$confirm('请确认已在直播间中：\n1. 登录了抖音账号\n2. 打开了小黄车\n3. 向下浏览完所有商品\n\n是否开始采集？', '采集确认', {
        confirmButtonText: '开始采集',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.isCollecting = true
        this.collectStatus = '正在采集商品信息，请稍候...'

        try {
          // 执行采集脚本
          const products = await this.executeCollectScript()

          if (products && products.length > 0) {
            this.collectedProducts = products
            this.collectStatus = `采集完成！共采集到 ${products.length} 个商品`
            this.$message.success(`采集完成！共采集到 ${products.length} 个商品`)
          } else {
            this.collectStatus = '未采集到商品数据，请确保小黄车已打开并浏览完商品'
            this.$message.warning('未采集到商品数据')
          }

        } catch (error) {
          console.error('[KnowledgeCollect] 采集失败:', error)
          this.collectStatus = '采集失败: ' + error.message
          this.$message.error('采集失败: ' + error.message)
        } finally {
          this.isCollecting = false
        }
      }).catch(() => {
        // 用户取消
      })
    },

    /**
     * 执行商品采集脚本
     */
    async executeCollectScript() {
      console.log('[KnowledgeCollect] ========== 开始执行采集脚本 ==========')

      const webview = this.$refs.liveWebview
      if (!webview) {
        console.error('[KnowledgeCollect] webview未加载')
        throw new Error('webview未加载')
      }

      console.log('[KnowledgeCollect] webview已找到，准备注入采集脚本')

      // 专门针对抖音小黄车的采集脚本 - 点击详情版本
      const collectScript = `
        (async function() {
          try {
            console.log('[采集脚本] ========== 脚本开始执行 ==========');
            console.log('[采集脚本] 开始采集商品信息...');
            console.log('[采集脚本] 注意：将点击每个商品，打开详情面板采集完整信息');

            const products = [];
            let productIndex = 1;

            // 等待函数
            const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

            // ============ 步骤1: 查找商品列表容器并滚动到顶部 ============
            console.log('[采集脚本] ========== 步骤1: 查找商品列表容器 ==========');

            // 查找包含商品列表的滚动容器（不写死class名）
            let listContainer = null;

            // 策略1: 通过 data-e2e 属性查找
            listContainer = document.querySelector('[data-e2e="live-promotion-list"]');

            if (!listContainer) {
              // 策略2: 查找包含多个商品的 ul 容器
              const allULs = document.querySelectorAll('ul');
              for (const ul of allULs) {
                const items = ul.querySelectorAll('li');
                if (items.length >= 2) { // 至少有2个商品
                  listContainer = ul;
                  break;
                }
              }
            }

            if (!listContainer) {
              console.error('[采集脚本] 未找到商品列表容器');
              return [];
            }

            console.log('[采集脚本] 找到商品列表容器:', listContainer.tagName);

            // 滚动到商品列表顶部
            console.log('[采集脚本] 滚动到商品列表顶部...');
            listContainer.scrollTop = 0;
            await sleep(500);

            // ============ 步骤2: 查找所有商品卡片 ============
            console.log('[采集脚本] ========== 步骤2: 查找商品卡片 ==========');

            // 使用更通用的方式查找商品卡片（不写死class名）
            let productCards = [];

            // 从列表容器中查找所有 li 元素
            productCards = Array.from(listContainer.querySelectorAll('li'))
              .filter(li => {
                // 筛选条件：必须包含图片和标题
                const hasImage = li.querySelector('img[alt], img[src]');
                const hasTitle = li.querySelector('[data-e2e="promotion-title"]') ||
                                li.querySelector('span[class*="title"]');
                const isVisible = li.offsetWidth > 0 && li.offsetHeight > 0;
                return hasImage && hasTitle && isVisible;
              });

            console.log('[采集脚本] 找到', productCards.length, '个商品卡片');

            if (productCards.length === 0) {
              console.error('[采集脚本] 没有找到任何商品卡片!');
              return [];
            }


            // ============ 步骤3: 逐个点击商品，采集详情 ============
            console.log('[采集脚本] ========== 步骤3: 开始逐个点击商品采集详情 ==========');

            for (let i = 0; i < productCards.length; i++) {
              const productCard = productCards[i];

              try {
                console.log('[采集脚本] ---------- 处理第', (i + 1), '/', productCards.length, '个商品 ----------');

                // 滚动到当前商品（确保可见）
                productCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
                await sleep(500);

                // 记录点击前的页面状态
                const beforeClickDivCount = document.querySelectorAll('div').length;
                console.log('[采集脚本] 点击前页面div数量:', beforeClickDivCount);

                // 查找真正可点击的元素（在li内部）
                console.log('[采集脚本] 查找可点击元素...');

                // 策略1: 点击 div.JKCoNWIb (内容容器)
                let clickTarget = productCard.querySelector('.JKCoNWIb, div[class*="JKCoN"]');

                if (!clickTarget) {
                  // 策略2: 点击 div.DlO0vxdk (商品区域)
                  clickTarget = productCard.querySelector('.DlO0vxdk, div[class*="DlO0v"]');
                }

                if (!clickTarget) {
                  // 策略3: 点击图片
                  clickTarget = productCard.querySelector('img[alt]');
                }

                if (!clickTarget) {
                  // 策略4: 点击商品标题
                  clickTarget = productCard.querySelector('[data-e2e="promotion-title"]');
                }

                if (!clickTarget) {
                  // 策略5: 使用整个li
                  clickTarget = productCard;
                }

                console.log('[采集脚本] 点击目标: 标签=' + clickTarget.tagName + ', 类名=' + (clickTarget.className || '无').substring(0, 50));

                // 点击商品打开详情
                console.log('[采集脚本] 执行点击...');

                // 触发点击（使用多种方式确保兼容性）
                clickTarget.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
                await sleep(50);
                clickTarget.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true }));
                await sleep(50);
                clickTarget.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                clickTarget.click();

                console.log('[采集脚本] 点击完成，等待详情面板加载...');
                await sleep(2000); // 等待详情面板加载

                // 检查页面是否有变化
                const afterClickDivCount = document.querySelectorAll('div').length;
                console.log('[采集脚本] 点击后页面div数量:', afterClickDivCount, '(增加了', (afterClickDivCount - beforeClickDivCount), '个)');

                // ============ 提取商品详情信息 ============
                console.log('[采集脚本] 开始查找商品详情容器...');

                // 动态查找包含"产品参数"+"保障"+"物流"的容器
                let detailContainer = null;

                // 策略1: 查找同时包含三个关键词的容器（最严格）
                const allDivs = Array.from(document.querySelectorAll('div'));
                const candidates = allDivs
                  .map(el => {
                    const text = el.innerText || '';
                    return {
                      element: el,
                      text: text,
                      length: text.length,
                      hasParam: text.includes('产品参数'),
                      hasGuarantee: text.includes('保障'),
                      hasLogistics: text.includes('物流'),
                      hasShop: text.includes('官方旗舰店') || text.includes('店铺')
                    };
                  })
                  .filter(item =>
                    item.hasParam &&
                    item.hasGuarantee &&
                    item.hasLogistics &&
                    item.length > 500 &&
                    item.length < 10000
                  )
                  .sort((a, b) => a.length - b.length); // 优先选择较小的容器

                console.log('[采集脚本] 找到', candidates.length, '个候选容器');

                if (candidates.length > 0) {
                  detailContainer = candidates[0].element;
                  console.log('[采集脚本] ✓ 找到详情容器，内容长度:', candidates[0].length);
                } else {
                  console.error('[采集脚本] ✗ 未找到详情容器');
                  // 尝试关闭详情页返回
                  const backButton = document.querySelector('.xRcNm_F4');
                  if (backButton) backButton.click();
                  await sleep(500);
                  continue;
                }

                // 提取商品名称
                let productName = '';
                const titleEl = document.querySelector('span.vs9hmvGz, span[class*="vs9hmv"]');
                if (titleEl) {
                  productName = titleEl.textContent.trim();
                  console.log('[采集脚本] ✓ 商品名称:', productName);
                } else {
                  // 备用方案: 从商品卡片中获取
                  const cardTitle = productCard.querySelector('[data-e2e="promotion-title"]');
                  if (cardTitle) {
                    productName = cardTitle.textContent.trim();
                    console.log('[采集脚本] ✓ 商品名称(备用):', productName);
                  } else {
                    console.warn('[采集脚本] ⚠ 未找到商品名称');
                  }
                }

                // 提取容器的完整文本作为商品详情
                let productDetail = detailContainer.innerText || detailContainer.textContent || '';

                // 清理详情文本
                const lines = productDetail.split('\\n').map(line => line.trim());
                const cleanedLines = [];
                let shouldStop = false;

                for (const line of lines) {
                  // 遇到"价格说明"就停止收集
                  if (line.includes('价格说明')) {
                    console.log('[采集脚本] 遇到"价格说明"，停止收集后续内容');
                    shouldStop = true;
                    break;
                  }

                  // 过滤掉空行
                  if (!line) continue;

                  // 过滤掉直播间相关的干扰文字
                  if (line.includes('欢迎来到直播间')) continue;
                  if (line.includes('抖音严禁')) continue;
                  if (line.includes('请打开抖音APP')) continue;
                  if (line.includes('扫描二维码')) continue;
                  if (line.includes('在线观众')) continue;
                  if (line.includes('全部') && line.length < 5) continue;

                  cleanedLines.push(line);
                }

                productDetail = cleanedLines.join('\\n');

                console.log('[采集脚本] 商品详情长度:', productDetail.length, '字符');
                console.log('[采集脚本] 商品详情预览:', productDetail.substring(0, 300));

                // 保存商品信息
                products.push({
                  id: productIndex++,
                  name: productName,
                  detail: productDetail
                });

                console.log('[采集脚本] ✓ 第', i + 1, '个商品采集完成');

                // ============ 关键：关闭详情面板，返回商品列表 ============
                console.log('[采集脚本] 关闭详情面板，返回商品列表...');

                // 查找返回按钮
                let backButton = null;

                // 策略1: 通过class名查找返回按钮
                backButton = document.querySelector('.xRcNm_F4');

                if (!backButton) {
                  // 策略2: 查找包含返回箭头SVG的元素
                  const allDivs = Array.from(document.querySelectorAll('div'));
                  backButton = allDivs.find(div => {
                    const svg = div.querySelector('svg[width="24"][height="24"]');
                    if (!svg) return false;
                    const path = svg.querySelector('path[d*="M16 4"]');
                    return !!path;
                  });
                }

                if (backButton) {
                  console.log('[采集脚本] 找到返回按钮，点击返回...');

                  // 点击返回按钮
                  backButton.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
                  await sleep(50);
                  backButton.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true }));
                  await sleep(50);
                  backButton.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true }));
                  backButton.click();

                  console.log('[采集脚本] 已点击返回按钮');
                } else {
                  console.warn('[采集脚本] 未找到返回按钮，尝试按ESC键...');
                  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true }));
                }

                await sleep(1000);

                // 确认列表已经重新显示
                const listVisible = listContainer.offsetWidth > 0 && listContainer.offsetHeight > 0;
                console.log('[采集脚本] 商品列表是否可见:', listVisible);

                if (!listVisible) {
                  console.warn('[采集脚本] 列表未显示，再次尝试返回...');
                  if (backButton) {
                    backButton.click();
                  } else {
                    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true }));
                  }
                  await sleep(800);
                }

                // 等待列表稳定
                await sleep(500);
                console.log('[采集脚本] 已返回商品列表，准备处理下一个商品...');

              } catch (error) {
                console.error('[采集脚本] ✗ 处理商品时出错:', error);
                console.error('[采集脚本] 错误信息:', error.message);

                // 尝试关闭可能打开的面板，返回列表
                console.log('[采集脚本] 尝试返回商品列表...');
                for (let retry = 0; retry < 3; retry++) {
                  document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true }));
                  await sleep(500);
                }
              }
            }

            // ============ 步骤3: 返回采集结果 ============
            console.log('[采集脚本] ========== 采集完成 ==========');
            console.log('[采集脚本] 共采集到', products.length, '个商品');
            return products;

          } catch (error) {
            console.error('[采集脚本] 执行失败:', error);
            return [];
          }
        })();
      `;

      // 执行脚本并获取结果
      try {
        console.log('[KnowledgeCollect] 开始执行 executeJavaScript...')
        console.log('[KnowledgeCollect] 脚本长度:', collectScript.length, '字符')

        const result = await webview.executeJavaScript(collectScript)

        console.log('[KnowledgeCollect] executeJavaScript 执行完成')
        console.log('[KnowledgeCollect] 返回结果类型:', typeof result)
        console.log('[KnowledgeCollect] 采集到', result?.length || 0, '个商品')

        if (result && result.length > 0) {
          console.log('[KnowledgeCollect] 第一个商品示例:', result[0])
        }

        return result || []
      } catch (error) {
        console.error('[KnowledgeCollect] 执行脚本失败 (catch):', error)
        console.error('[KnowledgeCollect] 错误详情:', error.message)
        console.error('[KnowledgeCollect] 错误堆栈:', error.stack)
        throw new Error('执行采集脚本失败: ' + error.message)
      }
    },

    /**
     * 删除模拟采集方法（已不需要）
     */
    async mockCollectProducts() {
      // 已被 executeCollectScript 替代
    },

    /**
     * 导出知识库
     */
    async exportKnowledge() {
      if (this.collectedProducts.length === 0) {
        this.$message.warning('暂无可导出的数据')
        return
      }

      try {
        const { ipcRenderer } = require('electron')
        const fs = require('fs')

        // 选择保存位置
        const result = await ipcRenderer.invoke('dialog:saveFile', {
          defaultPath: `商品知识库_${Date.now()}.xlsx`,
          filters: [
            { name: 'Excel Files', extensions: ['xlsx'] }
          ]
        })

        if (result.canceled || !result.filePath) {
          return
        }

        // 使用 xlsx 导出
        const XLSX = require('xlsx')

        // 准备数据
        const data = [
          ['商品链接号', '商品名称', '商品详情'],
          ...this.collectedProducts.map(p => [p.id, p.name, p.detail])
        ]

        // 创建工作簿
        const worksheet = XLSX.utils.aoa_to_sheet(data)
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, '商品知识库')

        // ✅ 使用 fs 直接写入文件，避免二次弹窗
        const buffer = XLSX.write(workbook, { type: 'buffer', bookType: 'xlsx' })
        fs.writeFileSync(result.filePath, buffer)

        this.exportStatus = '导出成功！'
        this.$message.success('知识库已成功导出到: ' + result.filePath)

        setTimeout(() => {
          this.exportStatus = ''
        }, 3000)

      } catch (error) {
        console.error('[KnowledgeCollect] 导出失败:', error)
        this.exportStatus = '导出失败: ' + error.message
        this.$message.error('导出失败: ' + error.message)
      }
    },

    /**
     * 删除单个商品
     */
    deleteProduct(index) {
      this.$confirm('确定要删除这个商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.collectedProducts.splice(index, 1)
        this.$message.success('已删除')
      }).catch(() => {})
    },

    /**
     * 清空采集数据
     */
    clearCollectedData() {
      if (this.collectedProducts.length === 0) {
        this.$message.info('暂无数据需要清空')
        return
      }

      this.$confirm('确定要清空所有采集数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.collectedProducts = []
        this.collectStatus = ''
        this.exportStatus = ''
        this.$message.success('已清空采集数据')
      }).catch(() => {})
    },

    /**
     * 关闭对话框
     */
    handleClose() {
      // 如果有正在采集的任务，提示用户
      if (this.isCollecting) {
        this.$message.warning('采集正在进行中，请等待完成后再关闭')
        return
      }

      // 清空数据和状态（但保留直播间地址）
      this.collectedProducts = []
      this.collectStatus = ''
      this.exportStatus = ''
      this.isRoomOpened = false
      this.isOpeningRoom = false
      this.isCollecting = false

      // ⚠️ 注意：不清空 liveRoomUrl，保留给下次使用
      console.log('[KnowledgeCollect] 关闭对话框，已清空数据，保留地址:', this.liveRoomUrl)

      this.dialogVisible = false
    }
  },
  beforeDestroy() {
    // 组件销毁时关闭直播间窗口
    if (this.liveRoomWindow) {
      try {
        this.liveRoomWindow.close()
      } catch (e) {
        console.error('关闭直播间窗口失败:', e)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.usage-tips {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
}

.label-text {
  padding-top: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.webview-container {
  margin-top: 20px;
  border: 2px solid #409eff;
  border-radius: 4px;
  height: 400px;
  overflow: hidden;
  background-color: #f5f7fa;
  position: relative;

  .live-webview {
    width: 100%;
    height: 100%;
    border: none;
  }

  // 采集时禁用webview
  .webview-disabled {
    pointer-events: none;
  }

  // 采集时的遮罩层
  .webview-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;

    .mask-content {
      text-align: center;
    }
  }
}

.result-area {
  margin-top: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  min-height: 300px;
  background-color: #ffffff;
  padding: 12px;

  .empty-text {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 276px;
    color: #909399;
    font-size: 14px;
  }
}
</style>
