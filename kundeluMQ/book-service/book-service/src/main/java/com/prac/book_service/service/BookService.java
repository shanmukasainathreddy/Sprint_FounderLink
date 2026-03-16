package com.prac.book_service.service;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prac.book_service.entity.Book;
import com.prac.book_service.repository.BookRepository;



@Service
public class BookService {

    @Autowired
    private BookRepository repo;

    public Book addBook(Book book) {
        return repo.save(book);
    }

    public List<Book> getBooks() {
        return repo.findAll();
    }

    public Book updateBook(Long id, Book book) {
        Book b = repo.findById(id).orElseThrow();
        b.setStock(book.getStock());
        return repo.save(b);
    }

    public void reduceStock(Long bookId, int quantity) {
        Book b = repo.findById(bookId).orElseThrow();
        b.setStock(b.getStock() - quantity);
        repo.save(b);
    }
}
