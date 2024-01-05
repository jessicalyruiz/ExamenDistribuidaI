package com.distribuida.service;

import com.distribuida.db.Book;

import java.util.List;

public interface LibroService {
    void create(Book book);//insert
    Book read(Integer id); //findById
    void update(Book book);
    void delete(Integer id);

    List<Book> findAll();
    Book findByISBN(String isbn);
}
