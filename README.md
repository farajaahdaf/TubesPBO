<<<<<<< HEAD
# Sistem Perpustakaan

Sistem manajemen perpustakaan sederhana menggunakan Spring Boot.

## Fitur

- Sistem autentikasi (login/register)
- Dashboard admin dan user
- Manajemen buku
- Manajemen peminjaman
- Manajemen user

## Teknologi yang Digunakan

- Java 17
- Spring Boot 3.2.1
- MySQL
- Thymeleaf
- Bootstrap 5
- Maven

## Cara Menjalankan

1. Pastikan Java 17 dan MySQL sudah terinstall
2. Clone repository ini
3. Buat database MySQL dengan nama `perpustakaan`
4. Sesuaikan konfigurasi database di `src/main/resources/application.properties`
5. Jalankan aplikasi dengan perintah:
   ```bash
   ./mvnw spring-boot:run
   ```
6. Akses aplikasi di `http://localhost:8080`

## Struktur Database

- User (id, username, password, role)
- Book (id, title, author, year, stock)
- Borrowing (id, user_id, book_id, borrow_date, return_date, status)

## Kontributor

- [Nama Anda]

## Lisensi

MIT License 
=======
# TubesPBO
>>>>>>> a3efe4f34daeafdab59ddd209e9be85e3122681d
