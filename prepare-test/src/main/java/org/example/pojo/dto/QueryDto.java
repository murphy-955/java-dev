package org.example.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QueryDto {
    /**
     * 图书名称（模糊查询，非必填）
     */
    private String bookName;

    /**
     * 作者（精确查询，非必填）
     */
    private String author;
    /**
     * 价格区间（非必填）
     */
    private BigDecimal minPrice;

    /**
     * 价格区间（非必填）
     */
    private BigDecimal maxPrice;

    /**
     * 分类 ID（非必填）
     */
    private Long categoryId;
}
