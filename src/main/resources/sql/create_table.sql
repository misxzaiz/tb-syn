-- 创建数据库
CREATE DATABASE IF NOT EXISTS syn_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE syn_db;

-- 创建采购入库表
CREATE TABLE IF NOT EXISTS purchase_in (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    io_id VARCHAR(50) NOT NULL COMMENT '入库单ID',
    purchase_no VARCHAR(100) COMMENT '采购单号',
    supplier_name VARCHAR(200) COMMENT '供应商名称',
    product_name VARCHAR(200) COMMENT '产品名称',
    product_code VARCHAR(100) COMMENT '产品编码',
    quantity INT COMMENT '数量',
    unit_price DECIMAL(18,2) COMMENT '单价',
    total_amount DECIMAL(18,2) COMMENT '总金额',
    purchase_type VARCHAR(50) COMMENT '采购类型',
    status VARCHAR(50) COMMENT '状态',
    purchase_date DATETIME COMMENT '采购日期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark TEXT COMMENT '备注',
    cid VARCHAR(50) NOT NULL COMMENT '租户ID',
    UNIQUE KEY uk_io_id_cid (io_id, cid) COMMENT '入库单ID和租户ID联合唯一索引',
    KEY idx_purchase_no (purchase_no) COMMENT '采购单号索引',
    KEY idx_purchase_date (purchase_date) COMMENT '采购日期索引',
    KEY idx_cid (cid) COMMENT '租户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购入库表';

-- 创建同步配置表（可选，如果需要将Redis中的配置持久化到MySQL）
CREATE TABLE IF NOT EXISTS syn_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    cid VARCHAR(50) NOT NULL COMMENT '租户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_cid_key (cid, config_key) COMMENT '租户ID和配置键联合唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步配置表';

-- 创建同步日志表（可选，用于记录同步历史）
CREATE TABLE IF NOT EXISTS syn_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    cid VARCHAR(50) NOT NULL COMMENT '租户ID',
    sync_type VARCHAR(50) COMMENT '同步类型',
    sync_count INT DEFAULT 0 COMMENT '同步数量',
    sync_status VARCHAR(20) COMMENT '同步状态：SUCCESS-成功，FAILED-失败',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_cid (cid) COMMENT '租户ID索引',
    KEY idx_sync_time (start_time) COMMENT '同步时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步日志表';