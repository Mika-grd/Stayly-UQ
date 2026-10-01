# SGA · Stayly-UQ — Entrega 1

Sistema de Gestión de Alojamiento para Stayly-UQ (apartahotel de 6 apartamentos en Armenia, Quindío).
Programación Avanzada · Universidad del Quindío · 2026-2.

Esta entrega contiene el **dominio** (arquitectura hexagonal), sus **pruebas unitarias**, los **repositorios en memoria**
y el contrato de la API en **OpenAPI 3.1**. Todavía no hay controladores ni base de datos.

## Cómo ejecutar las pruebas

Requiere un JDK 17 o superior para ejecutar Gradle. El proyecto compila con Java 21: si no está instalado, Gradle lo descarga solo (toolchain).

```bash
./gradlew clean test
```

## Estructura

```
src/main/java/co/edu/uniquindio/sga/
  domain/            modelo (agregados, objetos de valor), 13 servicios de dominio,
                     interfaces de repositorio, puerto ConfiguracionAlojamiento y ReglaDominioException.
                     Solo depende de java.*
  application/usecase/  CU-02 Crear, CU-03 Confirmar, CU-04 Cancelar, CU-05 Registrar llegada
  infrastructure/
    persistence/memoria/  repositorios con HashMap
    config/               valores de la Ficha leídos de application.yml + ensamblaje de beans
    web/                  (Corte 2)
docs/
  modelo/     libro del modelo (Excel): identificación, invariantes, servicios, casos de uso, matriz, plan de pruebas
  diagramas/  diagrama de clases (Mermaid) y mapa de agregados (límites y servicios)
  anexos/     Anexo B — preguntas guía
  api/        openapi.yaml
```

Los valores de la Ficha del Alojamiento (umbral de edad, horarios, anticipo, mascotas, recargo nocturno…) están en
`src/main/resources/application.yml`; el dominio los pide al puerto `ConfiguracionAlojamiento`.

## Validar la API

```bash
npx @redocly/cli lint docs/api/openapi.yaml
```
