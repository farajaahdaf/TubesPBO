package com.perpustakaan.repository;

import com.perpustakaan.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // ISBN digunakan sebagai ID, sehingga tipe ID adalah Long
} 