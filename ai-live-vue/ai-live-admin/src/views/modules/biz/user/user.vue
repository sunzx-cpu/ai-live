<template>
  <div class="mod-config">
    <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
      <el-form-item>
        <el-input v-model="dataForm.username" clearable placeholder="用户名"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.mobile" clearable placeholder="手机号"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="dataForm.nickname" clearable placeholder="昵称"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button @click="getDataList()">查询</el-button>
        <!--        <el-button v-if="isAuth('biz:user:save')" type="primary" @click="addOrUpdateHandle()">新增</el-button>-->
        <el-button v-if="isAuth('biz:user:delete')" :disabled="dataListSelections.length <= 0" type="danger"
          @click="deleteHandle()">批量删除
        </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="dataListLoading" :data="dataList" border style="width: 100%;"
      @selection-change="selectionChangeHandle">
      <el-table-column align="center" header-align="center" type="selection" width="50"></el-table-column>
      <el-table-column align="center" header-align="center" label="用户名" prop="username"></el-table-column>
      <el-table-column align="center" header-align="center" label="昵称" prop="nickname"></el-table-column>
      <el-table-column align="center" header-align="center" label="手机号" prop="mobile"></el-table-column>
      <el-table-column align="center" header-align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0">未激活</el-tag>
          <el-tag v-if="scope.row.status === 1" type="success">激活</el-tag>
          <el-tag v-if="scope.row.status === 2" type="warning">到期</el-tag>
          <el-tag v-if="scope.row.status === 3" type="danger">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" header-align="center" label="创建时间" prop="createTime"></el-table-column>
      <el-table-column align="center" fixed="right" header-align="center" label="操作" width="150">
        <template slot-scope="scope">
          <el-button size="small" type="text" @click="detailHandle(scope.row.id)">详情
          </el-button>
          <!--					<el-button size="small" type="text" @click="addOrUpdateHandle(scope.row.id)">修改</el-button>-->
          <!--					<el-button size="small" type="text" @click="deleteHandle(scope.row.id)">删除</el-button>-->
        </template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="pageIndex" :page-size="pageSize" :page-sizes="[10, 20, 50, 100]" :total="totalPage"
      layout="total, sizes, prev, pager, next, jumper" @size-change="sizeChangeHandle"
      @current-change="currentChangeHandle"></el-pagination>
    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
    <!-- 弹窗, 详情 -->
    <user-detail v-if="detailVisible" ref="detail" @refreshDataList="getDataList"></user-detail>
  </div>
</template>

<script>
import AddOrUpdate from "./user-add-or-update";
import UserDetail from "./user-detail";

export default {
  data() {
    return {
      dataForm: {
        username: "",
        mobile: "",
        nickname: ""
      },
      dataList: [],
      pageIndex: 1,
      pageSize: 10,
      totalPage: 0,
      dataListLoading: false,
      dataListSelections: [],
      addOrUpdateVisible: false,
      detailVisible: false
    };
  },
  components: {
    AddOrUpdate,
    UserDetail
  },
  activated() {
    this.getDataList();
  },
  methods: {
    // 获取数据列表
    getDataList() {
      this.dataListLoading = true;
      this.$http
        .get("/biz/user/list", {
          page: this.pageIndex,
          limit: this.pageSize,
          ...this.dataForm
        })
        .then(({ data }) => {
          if (data && data.code === 0) {
            this.dataList = data.page.list;
            this.totalPage = data.page.totalCount;
          } else {
            this.dataList = [];
            this.totalPage = 0;
          }
          this.dataListLoading = false;
        });
    },
    // 每页数
    sizeChangeHandle(val) {
      this.pageSize = val;
      this.pageIndex = 1;
      this.getDataList();
    },
    // 当前页
    currentChangeHandle(val) {
      this.pageIndex = val;
      this.getDataList();
    },
    // 多选
    selectionChangeHandle(val) {
      this.dataListSelections = val;
    },
    // 新增 / 修改
    addOrUpdateHandle(id) {
      this.addOrUpdateVisible = true;
      this.$nextTick(() => {
        this.$refs.addOrUpdate.init(id);
      });
    },
    // 详情
    detailHandle(id) {
      this.detailVisible = true;
      this.$nextTick(() => {
        this.$refs.detail.init(id);
      });
    },
    // 删除
    deleteHandle(id) {
      var ids = id
        ? [id]
        : this.dataListSelections.map(item => {
          return item.id;
        });
      this.$confirm(
        `确定对[id=${ids.join(",")}]进行[${id ? "删除" : "批量删除"}]操作?`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
      ).then(() => {
        this.$http.delete("/biz/user/delete", ids).then(({ data }) => {
          if (data && data.code === 0) {
            this.$message({
              message: "操作成功",
              type: "success",
              duration: 1500,
              onClose: () => {
                this.getDataList();
              }
            });
          } else {
            this.$message.error(data.msg);
          }
        });
      });
    }
  }
};
</script>
