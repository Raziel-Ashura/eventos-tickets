# EventosTickets — Etapa 1

Proyecto académico de **tres microservicios independientes** con Spring Boot 3.4.4, Java 21, PostgreSQL y API Gateway. **No incluye todavía** PDF, EFS, S3, Docker Hub ni despliegue EC2.

## Requisitos
- JDK 21, Maven 3.9+, Docker Desktop con Compose, IntelliJ IDEA y Postman.
- Puertos 5432 y 8080–8083 libres.

## Arranque local
1. Desde la raíz: `docker compose -f infra/docker-compose.postgres.yml up -d`.
2. Importar los cuatro `pom.xml` como proyectos Maven en IntelliJ.
3. Ejecutar en orden: `EventosApplication`, `ReservasApplication`, `TicketsApplication`, `GatewayApplication`.
4. Comprobar `http://localhost:8080/actuator/health` y los endpoints de cada servicio (`8081`, `8082`, `8083`).
5. Usar la colección Postman incluida; ejecutar las solicitudes en orden.

## Nota sobre persistencia
`ddl-auto=update` es solo para desarrollo. Antes de producción se usarán migraciones Flyway. El ejemplo de reserva hace la asignación de cupos en `ms-eventos` antes de persistir la reserva; **todavía no incluye compensación transaccional ante errores entre servicios**. La etapa posterior debe añadir idempotencia, liberación de cupos, concurrencia segura y reintentos.

## Pruebas
- POST `/api/eventos`: `{"nombre":"Concierto","fecha":"2027-12-15T20:00:00","lugar":"Teatro Central","capacidad":100,"precio":25000}`
- POST `/api/reservas`: `{"eventoId":1,"usuarioId":"usuarioX","cantidad":1}`
- POST `/api/tickets/registrar`: `{"reservaId":1,"nombreAsistente":"Juan Perez"}`
- GET `/api/tickets/usuario/usuarioX`

## Alcance de la etapa
El registro de ticket crea metadatos; **no genera PDF**. Para eso se implementará `/api/tickets/generar` en la etapa 2.
