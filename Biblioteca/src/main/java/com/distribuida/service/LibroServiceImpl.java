package com.distribuida.service;

import com.distribuida.db.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class LibroServiceImpl implements LibroService{
    @Inject
    EntityManager entityManager;
    @Override
    public void create(Book book) {
        var tx=entityManager.getTransaction();

        try {
            tx.begin();
            entityManager.persist(book);
            tx.commit();
        }
        catch (Exception ex){
            tx.rollback();
        }
    }

    @Override
    public Book read(Integer id) {
        return entityManager.find(Book.class, id);
    }

    @Override
    public void update(Book book) {
        entityManager.merge(book);
    }

    @Override
    public void delete(Integer id) {
        var tx=entityManager.getTransaction();

        try {
            tx.begin();
        entityManager.remove(this.read(id));
            tx.commit();
        }
        catch (Exception ex){
            tx.rollback();
        }
    }


    @Override
    public List<Book> findAll() {
        return entityManager.createQuery("Select p from Book p").getResultList();
    }

    @Override
    public Book findByISBN(String isbn) {
        TypedQuery<Book> myQuery=entityManager.createQuery("Select b from Book b where b.isbn=:valor", Book.class);
        myQuery.setParameter("valor", isbn);
        return myQuery.getSingleResult();
    }
}
