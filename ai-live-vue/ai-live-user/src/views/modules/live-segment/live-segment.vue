<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="search()">
      <el-form-item>
        <el-select v-model="dataForm.scriptId" clearable filterable placeholder="选择脚本">
          <el-option v-for="item in scriptList" :key="item.id" :label="item.name" :value="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.name" placeholder="片段名称"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="search()">搜索</el-button>
        <el-button @click="resetSearch()">重置</el-button>
        <el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button type="warning" @click="exportExcelHandle()" :disabled="dataListSelections.length <= 0">导出</el-button>
        <el-button type="info" @click="importExcelHandle()">导入</el-button>
        <el-button type="danger" @click="deleteHandle()" :disabled="dataListSelections.length <= 0">批量删除</el-button>
      </el-form-item>
    </el-form>
    <el-table
      :data="dataList"
      border
      v-loading="dataListLoading"
      @selection-change="selectionChangeHandle"
      style="width: 100%;">
      <el-table-column
        type="selection"
        header-align="center"
        align="center"
        width="50">
      </el-table-column>
      <el-table-column
        prop="id"
        header-align="center"
        align="center"
        label="片段编号">
      </el-table-column>
      <el-table-column
        prop="scriptName"
        header-align="center"
        align="center"
        label="脚本名称">
      </el-table-column>
      <el-table-column
        prop="name"
        header-align="center"
        align="center"
        label="片段名称">
      </el-table-column>
      <el-table-column
        prop="content"
        header-align="center"
        align="center"
        :show-overflow-tooltip="true"
        label="片段话术">
      </el-table-column>
      <el-table-column
        prop="createTime"
        header-align="center"
        align="center"
        label="创建时间">
      </el-table-column>
      <el-table-column
        fixed="right"
        header-align="center"
        align="center"
        width="150"
        label="操作">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
          <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"
      :current-page="pageIndex"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper">
    </el-pagination>

    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>

    <!-- 隐藏的Excel文件上传控件 -->
    <input ref="importExcelInput" type="file" accept=".xlsx,.xls" style="display: none" @change="handleExcelFileImport">

  </div>
</template>

<script>
  import AddOrUpdate from './live-segment-add-or-update'
  export default {
    data () {
      return {
        dataForm: {
          scriptId: '',
          name: ''
        },
        dataList: [],
        pageIndex: 1,
        pageSize: 10,
        totalPage: 0,
        dataListLoading: false,
        dataListSelections: [],
        addOrUpdateVisible: false,

        scriptList: []
      }
    },
    components: {
      AddOrUpdate
    },
    activated () {
      this.getDataList()
      this.getScriptList()
    },
    methods: {
      // 搜索
      search () {
        this.pageIndex = 1
        this.getDataList()
      },
      // 重置搜索条件
      resetSearch () {
        this.pageIndex = 1
        Object.assign(this.dataForm, {
          scriptId: '',
          name: ''
        })
        this.getDataList()
      },
      // 获取脚本列表
      getScriptList () {
        this.$http.get('/apps/live-script/all').then(({data}) => {
          this.scriptList = data.list
        })
      },
      // 获取数据列表
      getDataList () {
        this.dataListLoading = true
        this.$http.get('/apps/live-segment/list', {
          'page': this.pageIndex,
          'limit': this.pageSize,
          ...this.dataForm
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.dataList = data.page.list
            this.totalPage = data.page.totalCount
          } else {
            this.dataList = []
            this.totalPage = 0
          }
          this.dataListLoading = false
        })
      },
      // 每页数
      sizeChangeHandle (val) {
        this.pageSize = val
        this.pageIndex = 1
        this.getDataList()
      },
      // 当前页
      currentChangeHandle (val) {
        this.pageIndex = val
        this.getDataList()
      },
      // 多选
      selectionChangeHandle (val) {
        this.dataListSelections = val
      },
      // 新增 / 修改
      addOrUpdateHandle (id) {
        this.addOrUpdateVisible = true
        this.$nextTick(() => {
          this.$refs.addOrUpdate.init(id)
        })
      },
      // 删除
      deleteHandle (id) {
        var ids = id ? [id] : this.dataListSelections.map(item => {
          return item.id
        })
        this.$confirm(`确定对进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
        this.$http.delete('/apps/live-segment/delete', ids).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: '操作成功',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        })
      },
      // 导出Excel
      exportExcelHandle () {
        if (this.dataListSelections.length <= 0) {
          this.$message.warning('请选择要导出的片段')
          return
        }

        this.$confirm('确定要导出选中的片段到Excel吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          // 获取选中片段的ID
          const ids = this.dataListSelections.map(item => item.id).join(',')

          // 使用HTTP请求下载Excel文件
          this.$http({
            url: this.$http.adornUrl('/apps/live-segment/exportExcel'),
            method: 'get',
            params: this.$http.adornParams({
              ids: ids
            }),
            responseType: 'blob'
          }).then(response => {
            // 检查响应类型，如果是JSON则表示出错了
            if (response.headers['content-type'] && response.headers['content-type'].includes('application/json')) {
              // 读取错误信息
              const reader = new FileReader()
              reader.onload = function() {
                try {
                  const errorData = JSON.parse(reader.result)
                  console.error('Excel导出错误:', errorData)
                  this.$message.error(errorData.error || 'Excel导出失败')
                } catch (e) {
                  console.error('Excel导出失败:', e)
                  this.$message.error('Excel导出失败')
                }
              }.bind(this)
              reader.readAsText(new Blob([response.data]))
              return
            }

            // 正常的Excel文件处理
            const blob = new Blob([response.data], {
              type: 'application/vnd.ms-excel'
            })
            const url = window.URL.createObjectURL(blob)
            const link = document.createElement('a')
            link.href = url
            link.download = `live-segments-${new Date().getTime()}.xls`
            document.body.appendChild(link)
            link.click()
            document.body.removeChild(link)
            window.URL.revokeObjectURL(url)

            this.$message.success('导出成功')
          }).catch(error => {
            console.error('Excel导出失败:', error)
            this.$message.error('Excel导出失败')
          })
        })
      },
      // 导入Excel
      importExcelHandle () {
        this.$refs.importExcelInput.click()
      },
      // 处理Excel文件导入
      handleExcelFileImport (event) {
        const file = event.target.files[0]
        if (!file) {
          return
        }

        if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
          this.$message.error('请选择Excel格式的文件(.xlsx或.xls)')
          return
        }

        const formData = new FormData()
        formData.append('file', file)

        this.$confirm('确定要导入Excel片段吗？导入后将创建新的片段记录。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/apps/live-segment/importExcel'),
            method: 'post',
            data: formData,
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message.success(data.msg || '导入成功')
              this.getDataList()
            } else {
              this.$message.error(data.msg || '导入失败')
            }
          }).catch(error => {
            console.error('Excel导入失败:', error)
            this.$message.error('Excel导入失败')
          })
        })

        // 清空input的值
        event.target.value = ''
      }
    }
  }
</script>
