package com.prac.book_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prac.book_service.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
