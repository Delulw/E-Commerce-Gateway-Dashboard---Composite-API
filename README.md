# E-Commerce Composite API

Proyecto académico para presentar información de producto mediante una API compuesta con Spring Boot y una interfaz web con Thymeleaf.

## Estructura

- `catalog-service`: microservicio propio para el catálogo de productos.
- `inventory-service`: microservicio propio para inventario y almacén.
- `composite-api`: API agregadora, integración con el servicio externo de divisas y vistas web.

Los módulos son independientes y se compilan desde el `pom.xml` raíz.

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

La implementación de endpoints, reglas de negocio y vistas se realizará en las siguientes etapas.
