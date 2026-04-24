CREATE DATABASE IF NOT EXISTS java_dev_exam;

USE java_dev_exam;

-- 创建图书分类表
CREATE TABLE category
(
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称'
);

-- 创建图书信息表
CREATE TABLE book
(
    id           INT PRIMARY KEY AUTO_INCREMENT,
    book_name    VARCHAR(100) NOT NULL COMMENT '图书名称',
    author       VARCHAR(50) COMMENT '作者',
    price        DECIMAL(10, 2) COMMENT '价格',
    category_id  INT COMMENT '分类ID',
    stock        INT     DEFAULT 0 COMMENT '库存',
    publish_date DATE COMMENT '出版日期',
    status       TINYINT DEFAULT 1 COMMENT '0-下架 1-上架',
    FOREIGN KEY (category_id) REFERENCES category (id)
);

-- 插入测试数据
INSERT INTO category (name)
VALUES ('计算机'),
       ('文学'),
       ('历史');
INSERT INTO book (book_name, author, price, category_id, stock, publish_date, status)
VALUES ('Java核心技术', 'Cay Horstmann', 119.00, 1, 100, '2020-06-01', 1),
       ('Spring实战', 'Craig Walls', 89.00, 1, 50, '2021-03-15', 1),
       ('活着', '余华', 45.00, 2, 200, '2012-01-01', 1),
       ('三体', '刘慈欣', 58.00, 2, 150, '2008-01-01', 1),
       ('明朝那些事儿', '当年明月', 199.00, 3, 80, '2009-04-01', 1);