# Sistem Perpustakaan

Proyek ini adalah sistem manajemen perpustakaan yang dibangun menggunakan Spring Boot.

## Teknologi yang Digunakan

- Java 17
- Spring Boot 3.1.9
- Maven (sebagai build tool)

## Dependencies

### Spring Boot Dependencies
- **spring-boot-starter-web**: Untuk membuat aplikasi web dengan Spring MVC
- **spring-boot-starter-data-jpa**: Untuk akses database menggunakan JPA
- **spring-boot-starter-validation**: Untuk validasi data
- **spring-boot-starter-thymeleaf**: Template engine untuk tampilan web
- **spring-boot-devtools**: Tools development untuk hot reload

### Database
- **MySQL**: Database yang digunakan (mysql-connector-j)

### Tools
- **Lombok**: Untuk mengurangi boilerplate code (versi 1.18.30)

## Prasyarat

- Java Development Kit (JDK) 17
- MySQL Database
- Maven

## Cara Menjalankan

1. Pastikan MySQL server sudah berjalan
2. Clone repository ini
3. Masuk ke direktori proyek
4. Jalankan perintah:
   ```bash
   mvn spring-boot:run
   ```
5. Aplikasi akan berjalan di `http://localhost:8080`

## Build Project

Untuk membuild project menjadi JAR, jalankan:
```bash
mvn clean package
```

File JAR akan tersedia di folder `target/`.
