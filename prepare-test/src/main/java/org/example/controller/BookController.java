package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.pojo.Book;
import org.example.pojo.dto.QueryDto;
import org.example.pojo.dto.UpdateDto;
import org.example.pojo.vo.SelectBookByIdVo;
import org.example.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService service;

    @Operation(summary = "1.新增图书")
    @PostMapping("")
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        int res;
        if (book.getBookName() != null &&
                book.getAuthor() != null &&
                book.getPrice() != null &&
                book.getCategoryId() != null &&
                book.getStock() != null &&
                book.getPublishDate() != null &&
                book.getStatus() != null) {
            res = service.addBook(book);
        } else {
            return ResponseEntity.badRequest().body("参数不能为空");
        }

        return res > 0 ?
                ResponseEntity.ok("新增图书成功") :
                ResponseEntity.badRequest().body("新增图书失败");

    }

    @Operation(summary = "2.根据ID查询图书")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBookById(@PathVariable Long id){
        if (id == null){
            return ResponseEntity.badRequest().body("参数不能为空");
        }
        SelectBookByIdVo res = service.getBookById(id);
        return res != null ?
                ResponseEntity.ok(res) :
                ResponseEntity.badRequest().body("查询图书失败");
    }

    @Operation(summary = "3.分页查询")
    @GetMapping("list")
    public ResponseEntity<Object> listBooks(@RequestParam QueryDto dto){
        Book book = service.listBooks(dto);
        if (book == null){
            return ResponseEntity.badRequest().body("分页查询失败");
        }
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "4.修改图书信息")
    @PutMapping("")
    public ResponseEntity<String> updateBook(@RequestBody @Validated UpdateDto dto){
        if (dto == null){
            return ResponseEntity.badRequest().body("参数不能为空");
        }
        int res = service.updateBook(dto);

        return res >0 ?
                ResponseEntity.ok("修改图书信息成功") :
                ResponseEntity.badRequest().body("修改图书信息失败");
    }

    @Operation(summary = "5.批量删除图书")
    @DeleteMapping("batch")
    public ResponseEntity<String> deleteBooks(@RequestBody List<Long> ids){
        if (ids == null || ids.isEmpty()){
            return ResponseEntity.badRequest().body("参数不能为空");
        }
        int res = service.deleteBooks(ids);

        return res >0 ?
                ResponseEntity.ok("批量删除图书成功") :
                ResponseEntity.badRequest().body("批量删除图书失败");
    }

    @Operation(summary = "6.批量入库图书")
    @PostMapping("batchAdd")
    public ResponseEntity<String> batchAddBooks(@RequestBody List<Book> books){
        if (books == null || books.isEmpty()){
            return ResponseEntity.badRequest().body("参数不能为空");
        }
        int res = service.batchAddBooks(books);

        return res>0 ?
                ResponseEntity.ok("批量入库图书成功") :
                ResponseEntity.badRequest().body("批量入库图书失败");
    }
}
