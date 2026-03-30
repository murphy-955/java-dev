-- 学生管理系统数据库初始化脚本 (H2兼容版本)

-- 创建班级表
CREATE TABLE IF NOT EXISTS s_class (
    id INT AUTO_INCREMENT PRIMARY KEY,
    class_name VARCHAR(20) NOT NULL
);

-- 创建学生表
CREATE TABLE IF NOT EXISTS s_student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    age INT NOT NULL,
    name VARCHAR(20) NOT NULL,
    class_id INT,
    FOREIGN KEY (class_id) REFERENCES s_class(id)
);

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS s_operation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    method_name VARCHAR(100) NOT NULL,
    operation_time TIMESTAMP NOT NULL,
    operation_desc VARCHAR(255),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_operation_time ON s_operation_log(operation_time);
CREATE INDEX IF NOT EXISTS idx_method_name ON s_operation_log(method_name);
