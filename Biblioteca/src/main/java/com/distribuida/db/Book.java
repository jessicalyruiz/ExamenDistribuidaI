package com.distribuida.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "book")
public class Book {
    @Id
    @Setter @Getter
    private Integer id;
    @Setter @Getter
    private  String isbn;
    @Setter @Getter
    private String title;
    @Setter @Getter
    private String author;
    @Setter @Getter
    private BigDecimal price;
}
