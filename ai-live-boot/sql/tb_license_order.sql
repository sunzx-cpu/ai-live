CREATE TABLE `tb_license_order` (
	`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
	`order_no` varchar(50) NOT NULL COMMENT '订单编号',
	`package_id` bigint NOT NULL COMMENT '关联套餐ID',
	`user_id` bigint NOT NULL COMMENT '用户ID',
	`amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
	`status` tinyint DEFAULT NULL COMMENT '订单状态：1-待支付 2-已支付 3-已取消',
	`pay_channel` varchar(50) DEFAULT NULL COMMENT '支付渠道',
	`pay_time` datetime DEFAULT NULL COMMENT '支付时间',
	`create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
	`create_time` datetime DEFAULT NULL COMMENT '创建时间',
	`update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
	`update_time` datetime DEFAULT NULL COMMENT '修改时间',
	`merchant_id` bigint DEFAULT NULL COMMENT '商户id',
	PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT 'license订单信息表';