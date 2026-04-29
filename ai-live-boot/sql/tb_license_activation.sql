CREATE TABLE `tb_license_activation` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
	`activation_code` VARCHAR(100) NOT NULL COMMENT '激活码',
	`package_id` BIGINT NOT NULL COMMENT '关联套餐ID',
	`user_id` BIGINT NOT NULL COMMENT '用户ID',
	`device_info` TEXT COMMENT '设备信息',
	`is_used` TINYINT COMMENT '是否已使用：0-未使用 1-已使用',
	`activate_time` DATETIME COMMENT '激活时间',
	`create_by` VARCHAR(50) COMMENT '创建人',
	`create_time` DATETIME COMMENT '创建时间',
	`update_by` VARCHAR(50) COMMENT '修改人',
	`update_time` DATETIME COMMENT '修改时间',
	`merchant_id` BIGINT NOT NULL COMMENT '商户id',
	PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT 'License激活信息表';