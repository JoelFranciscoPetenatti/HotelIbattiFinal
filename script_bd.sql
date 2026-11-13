-- Ejecutar con una cuenta administradora de MySQL.
-- En una copia local, reemplazar ELEGIR_CLAVE_LOCAL antes de ejecutar.

CREATE DATABASE IF NOT EXISTS hotel_ibatti
    CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'hotel_app'@'localhost'
    IDENTIFIED BY 'ELEGIR_CLAVE_LOCAL';

GRANT ALL PRIVILEGES ON hotel_ibatti.* TO 'hotel_app'@'localhost';