package org.example.pojo.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class UpdateDto {
    @NonNull
    private Long id;

    private String bookName;

    private String author;
}
