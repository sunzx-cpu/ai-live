<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.key" clearable placeholder="参数名"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">查询</el-button>
        <el-button v-if="isAuth('biz:merchant:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button v-if="isAuth('biz:merchant:delete')" :disabled="dataListSelections.length <= 0" type="danger"
          @click="deleteHandle()">批量删除</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border style="width: 100%;"
      @selection-change="selectionChangeHandle">
      <el-table-column align="center" header-align="center" type="selection" width="50">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="id" prop="id">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="昵称" prop="nickname">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0">未激活</el-tag>
          <el-tag v-if="scope.row.status === 1" type="success">激活</el-tag>
          <el-tag v-if="scope.row.status === 2" type="warning">到期</el-tag>
          <el-tag v-if="scope.row.status === 3" type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" header-align="center" label="激活密钥" prop="activateSecretKey">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="到期时间" prop="expirationTime">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="注册时间" prop="registerTime">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="注册IP" prop="registerIp">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="最后登录时间" prop="lastLoginTime">
      </el-table-column>
      <el-table-column align="center" header-align="center" label="最后登录IP" prop="lastLoginIp">
      </el-table-column>
      <el-table-column align="center" fixed="right" header-align="center" label="操作" width="250">
        <template slot-scope="scope">
          <el-button :disabled="scope.row.status !== 1" size="small" type="text"
            @click="disableHandle(scope.row)">禁用</el-button>
          <el-button :disabled="scope.row.status !== 3" size="small" type="text"
            @click="enableHandle(scope.row)">启用</el-button>
          <el-button size="small" type="text" @click="resetPasswordHandle(scope.row)">重置密码</el-button>
          <el-button :disabled="[1, 3].includes(scope.row.status)" size="small" type="text"
            @click="activateSecretKeyHandle(scope.row)">激活卡密</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="pageIndex" :page-size="pageSize" :page-sizes="[10, 20, 50, 100]" :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper" @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle">
    </el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
  </div>
</template>

<script>
import AddOrUpdate from './merchant-add-or-update'

export default {
  data() {
    return {
      dataForm: {
        key: ''
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false,
    }
  },
  components: {
    AddOrUpdate
  },
  activated() {
    this.getDataList()
  },
  methods: {
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true
      this.$http.get('/biz/merchant/list', {
        'page': this.pageIndex,
        'limit': this.pageSize,
        ...this.dataForm
      }).then(({ data }) => {
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
    sizeChangeHandle(val) {
      this.pageSize = val
      this.pageIndex = 1
      this.getDataList()
    },
    // 当前页
    currentChangeHandle(val) {
      this.pageIndex = val
      this.getDataList()
    },
    // 多选
    selectionChangeHandle(val) {
      this.dataListSelections = val
    },
    // 禁用
    disableHandle(row) {
      this.$confirm(`确定禁用[${row.nickname}]吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/biz/merchant/disable', row.id).then(({ data }) => {
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
    // 启用
    enableHandle(row) {
      this.$confirm(`确定启用[${row.nickname}]吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.post('/biz/merchant/enable', row.id).then(({ data }) => {
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
    // 重置密码
    resetPasswordHandle(row) {
      this.$prompt('请输入新密码', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
      }).then(({ value }) => {
        this.$http.post('/biz/merchant/resetPassword', {
          id: row.id,
          password: value
        }).then(({ data }) => {
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
      }).catch(() => {
      });
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id)
      })
    },
    // 删除
    deleteHandle(id) {
      var ids = id ? [id] : this.dataListSelections.map(item => {
        return item.id
      })
      this.$confirm(`确定对[id=${ids.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete('/biz/merchant/delete', ids).then(({ data }) => {
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
    // 激活卡密
    activateSecretKeyHandle(row) {
      this.$prompt('请输入密钥', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
      }).then(({ value }) => {
        this.$http.post('/biz/merchant/activateSecretKey', {
          id: row.id,
          secretKey: value
        }).then(({ data }) => {
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
      }).catch(() => {
      });
    }
  }
}
</script>
