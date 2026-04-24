package org.example.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Book {
    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String bookName;

    /**
     * 作者
     */
    private String author;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类
     */
    private Long categoryId;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 发布时间
     */
    private LocalDateTime publishDate;

    /**
     * 状态
     */
    private Integer status;
}