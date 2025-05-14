package com.perpustakaan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "book")
@Getter
@Setter
public class Book {
    
    @Id
    private Long isbn;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false)
    private int stock;
    
    public void reduceStock(int jumlah) {
        if (this.stock >= jumlah) {
            this.stock -= jumlah;
        } else {
            throw new RuntimeException("Stok buku tidak mencukupi!");
        }
    }
    public void addStock(int jumlah) {
        if (jumlah > 0) {
            this.stock += jumlah;
        } else {
            throw new RuntimeException("Jumlah penambahan stok harus lebih dari 0!");
        }
    }
} 