# E-Commerce Composite API

Proyecto académico para presentar información de producto mediante una API compuesta con Spring Boot y una interfaz web con Thymeleaf.

## Estructura

- `catalog-service`: microservicio propio para el catálogo de productos.
- `inventory-service`: microservicio propio para inventario y almacén.
- `composite-api`: API agregadora, integración con el servicio externo de divisas y vistas web.

Los módulos son independientes y se compilan desde el `pom.xml` raíz.

## Microservicio de inventario

El inventario expone `GET /microservices/inventory/{productId}` en el puerto `8082`. Utiliza datos de demostración en memoria, por lo que no requiere base de datos.

Para iniciar solo este módulo desde la raíz:

```shell
mvn -pl inventory-service spring-boot:run
```

Consulta del inventario de demostración:

```shell
curl http://localhost:8082/microservices/inventory/P-101
```

La respuesta contiene `productId`, `availableUnits`, `warehouseLocation` y `estimatedDeliveryDays`. Si el producto no tiene un registro de inventario, el servicio responde `404 Not Found`.

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

## Flujo de ramas

- `main`: rama estable; no se trabaja directamente sobre ella.
- `develop`: rama de integración y base común.
- `feature/*`: ramas de trabajo por funcionalidad, creadas desde `develop`.

La estructura inicial se mantiene en `develop`. Antes de comenzar una funcionalidad, actualizar su rama `feature/*` con `develop` para heredar los cambios integrados.

## Compilación

Desde la raíz del repositorio:

```shell
mvn clean verify
```

## Microservicio de catálogo

El catálogo expone `GET /microservices/products/{id}` en el puerto `8081`. En esta etapa utiliza datos de demostración en memoria, por lo que no requiere base de datos.

Para iniciar solo este módulo desde la raíz:

```shell
mvn -pl catalog-service spring-boot:run
```

Producto de demostración:

```shell
curl http://localhost:8081/microservices/products/P-101
```

La respuesta contiene `id`, `name`, `description`, `category` y `priceUsd`. Un identificador inexistente produce `404 Not Found`.

Los demás módulos y funcionalidades se implementarán en etapas posteriores.
