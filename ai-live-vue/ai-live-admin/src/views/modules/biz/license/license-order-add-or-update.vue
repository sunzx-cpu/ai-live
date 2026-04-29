<template>
	<el-dialog :close-on-click-modal="false" :title="!dataForm.id ? '新增' : '修改'" :visible.sync="visible">
		<el-form ref="dataForm" :model="dataForm" :rules="dataRule" label-width="120px" @keyup.enter.native="saveHandle">
			<el-form-item label="订单号" prop="orderId">
				<el-input v-model="dataForm.orderId" placeholder="订单号"></el-input>
			</el-form-item>
			<el-form-item label="用户ID" prop="userId">
				<el-input-number v-model="dataForm.userId" placeholder="用户ID"></el-input-number>
			</el-form-item>
			<el-form-item label="套餐名称" prop="packageName">
				<el-input v-model="dataForm.packageName" placeholder="套餐名称"></el-input>
			</el-form-item>
			<el-form-item label="金额" prop="amount">
				<el-input-number v-model="dataForm.amount" :precision="2" :step="0.01" placeholder="金额"></el-input-number>
			</el-form-item>
			<el-form-item label="状态" prop="status">
				<el-radio-group v-model="dataForm.status">
					<el-radio :label="1">待支付</el-radio>
					<el-radio :label="2">已支付</el-radio>
					<el-radio :label="3">已取消</el-radio>
				</el-radio-group>
			</el-form-item>
		</el-form>
		<span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="saveHandle">确定</el-button>
    </span>
	</el-dialog>
</template>

<script>
export default {
	data() {
		return {
			visible: false, dataForm: {
				id: 0, orderId: '', userId: 0, packageName: '', amount: 0, status: 1
			}, dataRule: {
				orderId: [{required: true, message: '订单号不能为空', trigger: 'blur'}],
				userId: [{required: true, message: '用户ID不能为空', trigger: 'blur'}],
				packageName: [{required: true, message: '套餐名称不能为空', trigger: 'blur'}],
				amount: [{required: true, message: '金额不能为空', trigger: 'blur'}]
			}
		}
	}, methods: {
		init(id) {
			this.dataForm.id = id || 0
			this.visible = true
			if (this.dataForm.id) {
				this.$http({
					url: this.$http.adornUrl(`/biz/license-order/info/${this.dataForm.id}`), method: 'get'
				}).then(({data}) => {
					if (data && data.code === 200) {
						this.dataForm = data.licenseOrder
					}
				})
			}
		}, // 表单提交
		saveHandle() {
			this.$refs.dataForm.validate((valid) => {
				if (valid) {
					const url = !this.dataForm.id ? '/biz/license-order/save' : '/biz/license-order/update'
					this.$http({
						url: this.$http.adornUrl(url), method: 'post', data: this.$http.adornData(this.dataForm)
					}).then(({data}) => {
						if (data && data.code === 200) {
							this.$message({
								message: '操作成功', type: 'success', duration: 1500, onClose: () => {
									this.visible = false
									this.$emit('refreshDataList')
								}
							})
						} else {
							this.$message.error(data.msg)
						}
					})
				}
			})
		}
	}
}
</script>
