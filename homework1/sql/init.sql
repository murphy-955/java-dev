-- 学生管理系统数据库初始化脚本

-- 创建班级表
CREATE TABLE IF NOT EXISTS `s_class` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `class_name` VARCHAR(20) NOT NULL COMMENT '班级名称'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- 创建学生表
CREATE TABLE IF NOT EXISTS `s_student` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `age` INT NOT NULL COMMENT '年龄',
    `name` VARCHAR(20) NOT NULL COMMENT '姓名',
    `class_id` INT COMMENT '班级id',
    FOREIGN KEY (`class_id`) REFERENCES `s_class`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `s_operation_log` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `method_name` VARCHAR(100) NOT NULL COMMENT '操作方法名',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `operation_desc` VARCHAR(255) COMMENT '操作描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(255) COMMENT '用户代理',
    INDEX `idx_operation_time` (`operation_time`),
    INDEX `idx_method_name` (`method_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 插入班级示例数据
INSERT INTO `s_class` (`class_name`) VALUES
('计算机一班'),
('计算机二班'),
('软件工程一班');

-- 插入学生示例数据
INSERT INTO `s_student` (`age`, `name`, `class_id`) VALUES
(20, '张三', 1),
(21, '李四', 1),
(19, '王五', 2),
(22, '赵六', 3);
