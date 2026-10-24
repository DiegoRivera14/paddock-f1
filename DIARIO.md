# Diario de trabajo

Registro de lo que se hace cada día, los problemas que aparecen y cómo se resuelven. Servirá de base para el apartado 2 de la memoria (temporalización y retos).

## Sábado 24 de octubre de 2026

**Hecho**
- Elección del proyecto: web de consulta de Fórmula 1 ("Paddock").
- Decisión de tecnologías: Java + Spring Boot + Thymeleaf, HTML y CSS, sin JavaScript.
- Instalación de JDK 21 (Eclipse Temurin), Git y Visual Studio Code.
- Creación del proyecto base con Spring Initializr y primer commit en GitHub.
- Comprobación de que la aplicación arranca (Tomcat en el puerto 8080).
- Elección de la temporada 2025 como fuente de datos (temporada cerrada, datos estables).

**Problemas y soluciones**
- `java -version` no funcionaba tras instalar el JDK. Causa: el PATH no se había actualizado. Solución: reiniciar VS Code y comprobar las opciones "Add to PATH" y "Set JAVA_HOME" del instalador.
- El zip de Spring Initializr se descomprimió dentro de una carpeta `paddock` anidada, de modo que Git se inició en la carpeta equivocada. Solución: borrar el `.git` exterior y repetir `git init` dentro de la carpeta que contiene `pom.xml`.
- `git push` falló con "Repository not found" porque el repositorio no existía todavía en GitHub. Solución: crear `paddock-f1` en GitHub y repetir el push.

**Aprendido**
- Diferencia entre Git (herramienta local) y GitHub (servicio web).
- Los comandos de `git config` no muestran salida cuando funcionan.

## Domingo 25 de octubre de 2026

(pendiente)
