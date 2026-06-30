# ms-pedidos — EcoTrade

Gestión de pedidos: creación, consulta y cambio de estado.

Microservicio del marketplace **EcoTrade** — Desarrollo Fullstack I.

---

## Integrantes

- Martín Meliman
- Ignacio Lapierre

**Equipo N°:** 9

---

## Información técnica

| Dato | Valor |
|------|-------|
| Puerto local | 8085 |
| Patrón | CSR (Controller–Service–Repository) |
| Base de datos | MySQL (Railway en producción) |
| Comunicación | REST / FeignClient |
| Consume | ms-productos, ms-usuarios |

---

## Endpoints principales

```
GET /api/pedidos, POST /api/pedidos, GET /api/pedidos/{id}, GET /api/pedidos/usuario/{uid}, PUT /api/pedidos/{id}/estado, DELETE /api/pedidos/{id}
```

---

## Documentación Swagger

| Entorno | URL |
|---------|-----|
| Local | http://localhost:8085/swagger-ui.html |
| Remoto (Render) | https://ms-pedidos-8e8g.onrender.com/swagger-ui.html |

---

## Acceso vía API Gateway

```
https://ms-gateway-ndke.onrender.com/api/pedidos
```

---

## Ejecución local

1. Tener MySQL corriendo (Laragon)
2. Asegurarse que **ms-eureka** esté levantado primero (puerto 8761)
3. Ejecutar este microservicio:
   ```bash
   mvn spring-boot:run
   ```
4. Verificar registro en http://localhost:8761

Perfil de desarrollo:
```
SPRING_PROFILES_ACTIVE=dev
```

---

## Pruebas unitarias

```bash
mvn test
```

Tests con JUnit 5 + Mockito (patrón Given-When-Then) sobre las reglas
de negocio del Service.

---

## Despliegue

Desplegado en **Render**: https://ms-pedidos-8e8g.onrender.com

Base de datos MySQL en **Railway**.

Variables de entorno (perfil prod):
```
SPRING_PROFILES_ACTIVE=prod
EUREKA_URL=https://ms-eureka-2lpc.onrender.com/eureka/
SPRING_DATASOURCE_URL=jdbc:mysql://reseau.proxy.rlwy.net:17411/railway?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=***
```

---

## Proyecto completo

Este es uno de los 12 servicios de **EcoTrade**.
Ver el README completo en el repositorio **ms-gateway** para la
arquitectura completa, los 12 microservicios y todas las rutas del Gateway.
