package org.example.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SelectBookByIdVo {
    private String bookName;
    private String author;
    private BigDecimal price;
    private String categoryName;
    private Integer stock;
    private LocalDateTime publishDate;
    private Integer status;
}
