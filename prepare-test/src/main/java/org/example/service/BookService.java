package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.mappers.BookMapper;
import org.example.pojo.Book;
import org.example.pojo.dto.QueryDto;
import org.example.pojo.dto.UpdateDto;
import org.example.pojo.vo.SelectBookByIdVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper mapper;

    public int addBook(Book book) {
        return mapper.addBook(book);
    }

    public SelectBookByIdVo getBookById(Long id) {
        return mapper.getBookById(id);
    }

    public Book listBooks(QueryDto dto) {
        return mapper.listBook(dto);
    }

    public int updateBook(UpdateDto dto) {
        return mapper.updateBook(dto);
    }

    public int deleteBooks(List<Long> ids) {
        return mapper.deleteBooks(ids);
    }

    public int batchAddBooks(List<Book> books) {
        return mapper.batchAddBooks(books);
    }
}
