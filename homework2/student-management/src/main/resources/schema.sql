-- H2数据库建表脚本

-- 班级表 c_class
CREATE TABLE IF NOT EXISTS c_class (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20)
);

-- 学生表 s_student
CREATE TABLE IF NOT EXISTS s_student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20),
    age TINYINT,
    c_id INT,
    FOREIGN KEY (c_id) REFERENCES c_class(id)
);

-- 插入测试数据
INSERT INTO c_class (id, name) VALUES (1, '计算机一班'), (2, '计算机二班'), (3, '软件工程一班');
INSERT INTO s_student (id, name, age, c_id) VALUES
    (1, '张三', 20, 1),
    (2, '李四', 21, 1),
    (3, '王五', 19, 2),
    (4, '赵六', 20, 3);
