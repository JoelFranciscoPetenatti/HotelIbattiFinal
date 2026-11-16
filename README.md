# Hotel IBATTI

Proyecto integrador de Programación 2. Aplicación web para gestionar huéspedes, habitaciones y reservas de un hotel.

---

## Integrantes
- Luka Ibarra
- Fran Petenatti
- Ángel Venditti

---

## Funcionalidades
- **Huéspedes:** Registrar, listar, editar y eliminar huéspedes, con validación de DNI único.
- **Habitaciones:** Registrar, listar, editar y eliminar habitaciones, con número único por hotel.
- **Reservas:** Crear, editar y filtrar reservas por número de habitación.
- **Validaciones:** Controlar fechas, capacidad y disponibilidad, calculando el total según la tarifa por noche y la duración de la estadía.
- **Gestión de estados:** Cancelar reservas conservando el historial y liberando las fechas.
- **Integridad de datos:** Eliminar reservas definitivamente y proteger los huéspedes y habitaciones que tienen reservas asociadas.

---

## Tecnologías
- **Backend:** Java 17+, Maven, Spring Boot, Spring MVC
- **Persistencia:** DataNucleus JDO y MySQL
- **Frontend:** Thymeleaf y Bootstrap
- **Arquitectura:** Organizada en capas de dominio, repositorios, servicios y controladores.

---

## Requisitos
- JDK 17 o superior.
- Maven disponible en la terminal.
- MySQL Server en ejecución en `127.0.0.1:3306`.

---

## Preparar MySQL
1. Abrir `script_bd.sql` en MySQL Workbench con una cuenta administradora.
2. Si el usuario `hotel_app` todavía no existe, reemplazar `ELEGIR_CLAVE_LOCAL` por una contraseña propia en una copia local del script.
3. Ejecutar el script. Si `hotel_app` ya existía, usar su contraseña actual.
4. DataNucleus crea las tablas faltantes al iniciar la aplicación. La aplicación también carga datos iniciales de ejemplo cuando corresponde.

---

## Ejecutar en Windows
Abrir PowerShell en la carpeta `Hotel IBATTI` que contiene `pom.xml`. Ejecutar la primera línea, ingresar la contraseña de `hotel_app` y ejecutar la segunda línea en la misma terminal:

```powershell
$env:HOTEL_DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Contraseña de MySQL para hotel_app' -AsSecureString)).Password
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.address=127.0.0.1 --server.port=8081"
```

Abrir [Hotel IBATTI](http://127.0.0.1:8081/) en el navegador. *(Nota: Si se abre una terminal nueva, volver a definir `HOTEL_DB_PASSWORD`)*.

---

## Demostración rápida
1. Crear una reserva para una habitación y fechas futuras disponibles.
2. Intentar crear otra para la misma habitación y las mismas fechas. La aplicación debe rechazarla.
3. Cancelar la primera reserva. Debe seguir visible como **Cancelada**.
4. Repetir el alta para las mismas fechas. La nueva reserva debe quedar **Confirmada** junto a la anterior.
