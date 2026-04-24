package org.example.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.pojo.Book;
import org.example.pojo.dto.QueryDto;
import org.example.pojo.dto.UpdateDto;
import org.example.pojo.vo.SelectBookByIdVo;

import java.util.List;

@Mapper
public interface BookMapper {
    int addBook(@Param("book") Book book);

    SelectBookByIdVo getBookById(Long id);

    Book listBook(QueryDto dto);

    int updateBook(UpdateDto dto);

    int deleteBooks(List<Long> ids);

    int batchAddBooks(List<Book> books);
}
