-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE student_db;

-- 班级表 c_class
CREATE TABLE IF NOT EXISTS c_class (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) DEFAULT NULL COMMENT '班级名称',
    PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- 学生表 s_student
CREATE TABLE IF NOT EXISTS s_student (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(20) DEFAULT NULL COMMENT '学生姓名',
    age TINYINT DEFAULT NULL COMMENT '年龄',
    c_id INT(11) DEFAULT NULL COMMENT '班级ID',
    PRIMARY KEY (id),
    KEY idx_class_id (c_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 插入测试数据
INSERT INTO c_class (id, name) VALUES (1, '计算机一班'), (2, '计算机二班'), (3, '软件工程一班');
INSERT INTO s_student (id, name, age, c_id) VALUES 
    (1, '张三', 20, 1),
    (2, '李四', 21, 1),
    (3, '王五', 19, 2),
    (4, '赵六', 20, 3);
