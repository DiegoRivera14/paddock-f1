# Paddock F1

Aplicación web de consulta sobre la Fórmula 1: próxima carrera, pilotos, escuderías, clasificaciones y calendario de circuitos de la temporada 2025.

Proyecto de Fin de Ciclo (PFC) del Ciclo de Grado Superior en Desarrollo de Aplicaciones Web, CPR A Coruña Afundación, curso 2022/23.

> Proyecto no oficial. No está afiliado a la Fórmula 1 ni a ninguna escudería. No usa logotipos ni fotografías con derechos de autor.

## Motivación

Los aficionados a la F1 suelen consultar varias webs para saber cuándo es la próxima sesión, cómo va el campeonato o qué ha hecho un piloto en un circuito. Paddock reúne esa información en un único sitio, con un diseño propio y sin necesidad de JavaScript en el navegador.

## Estado del proyecto

En desarrollo. Entrega parcial: 15 de noviembre. Entrega final: 6 de diciembre.

| Módulo | Rama | Estado |
|---|---|---|
| Proyecto base Spring Boot | `main` | Hecho |
| Modelo de datos | `feature/modelo-datos` | En curso |
| Importador de datos de F1 | `feature/importador-f1` | Pendiente |
| Feed principal | `feature/feed-principal` | Pendiente |
| Pilotos | `feature/pilotos` | Pendiente |
| Escuderías | `feature/escuderias` | Pendiente |
| Clasificaciones | `feature/clasificaciones` | Pendiente |
| Calendario y circuitos | `feature/circuitos` | Pendiente |
| Circuito interactivo (simulación) | `feature/circuito-interactivo` | Extra |
| Login y favoritos | `feature/login-favoritos` | Extra |

## Tecnologías

- Java 21
- Spring Boot (Spring Web, Spring Data JPA, Thymeleaf, Validation, Spring Security)
- H2 (base de datos en archivo)
- HTML y CSS. Sin JavaScript: las páginas se generan en el servidor con Thymeleaf.
- Maven

## Fuente de datos

Los datos deportivos proceden de la API pública [Jolpica-F1](https://api.jolpi.ca/ergast/f1/), sucesora de Ergast. La aplicación los descarga una vez, los guarda en su propia base de datos y trabaja con esa copia.

## Requisitos

- JDK 17 o superior (desarrollado con JDK 21)
- Git
- No hace falta instalar Maven: el proyecto incluye el wrapper `mvnw`.

## Cómo ejecutarlo

```
git clone https://github.com/DiegoRivera14/paddock-f1.git
cd paddock-f1
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # Linux / macOS
```

La aplicación queda disponible en `http://localhost:8080`.

## Estructura

```
src/main/java/com/diegorivera/paddock/
  model/        Entidades JPA
  repository/   Acceso a datos (Spring Data)
  service/      Lógica de negocio
  controller/   Controladores web y API REST
src/main/resources/
  templates/    Plantillas Thymeleaf (HTML)
  static/css/   Hojas de estilo
```

## Flujo de trabajo

Cada funcionalidad se desarrolla en una rama propia que sale de `main`, con commits pequeños y mensajes siguiendo esta convención:

- `feat:` nueva funcionalidad
- `fix:` corrección de errores
- `docs:` documentación
- `style:` estilos CSS
- `chore:` configuración y tareas de mantenimiento

Las ramas se integran en `main` mediante Pull Request.

## Autor

Diego Rivera
