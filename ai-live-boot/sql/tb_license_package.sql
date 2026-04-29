CREATE TABLE `tb_license_package` (
	`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
	`package_name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
	`description` TEXT COMMENT '套餐描述',
	`price` DECIMAL(10, 2) NOT NULL COMMENT '套餐价格',
	`status` TINYINT COMMENT '状态：1-启用 0-禁用',
	`create_by` VARCHAR(50) COMMENT '创建人',
	`create_time` DATETIME COMMENT '创建时间',
	`update_by` VARCHAR(50) COMMENT '修改人',
	`update_time` DATETIME COMMENT '修改时间'
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT 'License套餐信息表';