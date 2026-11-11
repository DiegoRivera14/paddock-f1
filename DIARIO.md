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

N/A

## Lunes 26 de octubre de 2026

**Hecho**
- Revisé el archivo application.properties y entendí qué hace cada línea: la base de datos H2 se guarda en un archivo para que los datos no se pierdan al apagar la aplicación, y Hibernate crea las tablas a partir de las entidades.
- Creé las entidades Circuito y Escuderia. Cada una representa una tabla de la base de datos, y cada objeto, una fila.
- Hice un commit por cada entidad y los subí a la rama feature/modelo-datos.

**Lo que aprendí**
- Qué hacen las anotaciones @Entity y @Id, y por qué el id de Circuito y Escuderia es un texto que viene de la API.
- Por qué los campos son privados y se leen con getters.

**Problemas y soluciones**
- Por el momento aparente nada, fase muy inicial del proyecto.

## Martes 27 de octubre de 2026

**Hecho**
- Creé las entidades Piloto, Carrera y Resultado, cada una con su commit.

**Aprendido**
- @ManyToOne: muchos pilotos pertenecen a una escudería, y en la tabla se guarda como clave foránea.
- Diferencia entre un identificador de texto (Circuito, Escuderia, Piloto) y uno generado por la base de datos (Carrera, Resultado).
- Uso de Integer en vez de int cuando el valor puede ser null, como la posición final de un piloto que no termina.
- Resultado une Carrera, Piloto y Escuderia, y de ahí saldrán las estadísticas.

**Problemas y soluciones**
- En la consola H2 no aparecen las tablas.
   - Causa: la consola trae por defecto la URL jdbc:h2:mem:testdb, que es una base en memoria distinta de la nuestra.
   - Solución: escribir la URL de application.properties (jdbc:h2:file:./data/paddock).

- Cambié una entidad y la tabla no se actualiza bien.
   - Causa: ddl-auto=update añade columnas, pero no borra ni modifica las existentes.
   - Solución: en desarrollo, borrar la carpeta data/ para regenerar la base de datos.

## Miércoles 28 de octubre de 2026

**Hecho**
- Revisé los cinco repositorios Spring Data (Circuito, Escuderia, Piloto, Carrera y Resultado) y los subí en dos commits.
- Creé la clase SecurityConfig, en un paquete nuevo llamado config, para que las páginas sean públicas y para poder usar la consola H2.
- Comprobé en la consola H2 que existen las cinco tablas y que PILOTO tiene la columna ESCUDERIA_ID.
- Abrí el Pull Request "Modelo de datos" y lo fusioné en main.

**Aprendido**
- Qué es un repositorio: una interfaz que, al extender JpaRepository, ya trae guardar, buscar y borrar sin escribir SQL.
- Que Spring construye consultas a partir del nombre del método, por ejemplo findByTemporadaOrderByRonda.
- Por qué findByTemporadaAndRonda devuelve un Optional: la carrera puede no existir.
- Que Spring Security bloquea todo por defecto, y que la consola H2 necesita una configuración propia para poder mostrarse.

**Problemas y soluciones**
- No entendía qué debía poner en el campo JDBC URL de la consola H2.
   - Causa: no sabía que había que copiar el valor de spring.datasource.url del archivo application.properties.
   - Solución: copiar solo lo que va después del signo igual y pegarlo en lugar de la URL que trae por defecto.
   - Aprendido: la consola H2 se conecta a la base de datos que indique esa URL, y si no coincide con la de la aplicación no se ven las tablas.

**Dudas pendientes**
- Cómo se va a convertir el JSON de la API en objetos Piloto, Carrera y Resultado.
- Qué pasará con un piloto que cambie de equipo durante la temporada, ya que Piloto guarda una sola escudería.
- Cuándo hace falta realmente la carga LAZY y qué error da si se usa mal.
- Por qué la clave de Carrera se genera sola y la de Piloto no.

## Jueves 29 – Domingo 1 de noviembre de 2026

**Hecho**
- Creé la rama feature/importador-f1 y añadí la configuración de la API Jolpica-F1 y de la temporada (2025) en application.properties.
- Incorporé las clases del importador, cada una en su commit: JolpicaDto, JolpicaClient, ImportadorService (con ColoresEscuderia) e ImportadorRunner.
- Arranqué la aplicación y comprobé en la consola H2 que se habían importado los datos.

**Aprendido**
- La aplicación guarda una copia de los datos de la API en su propia base de datos, en lugar de llamar a la API en cada visita. Así las páginas cargan rápido, no dependemos de que la API esté disponible y no superamos sus límites.
- La API devuelve JSON, y los record de JolpicaDto son las estructuras a las que Jackson convierte ese JSON. Sus nombres están en inglés porque tienen que coincidir con los de la API.
- JolpicaClient usa RestClient para hacer las peticiones y respeta los límites de la API (4 por segundo y 500 por hora) con una pausa entre peticiones. También envía un User-Agent propio, que la API exige.
- Los números llegan como texto en el JSON ("25", "1"), y el importador los convierte. Si un dato no existe (como el número de un piloto) se guarda como null.
- La API no dice en qué equipo está cada piloto, solo con qué equipo corrió cada carrera, así que los pilotos se crean a partir de los resultados y se les asigna el último equipo.
- ImportadorService va en una sola transacción (@Transactional): si algo falla a mitad, no se guarda nada y la base de datos queda como estaba.
- ImportadorRunner se ejecuta al arrancar y solo importa si la temporada aún no está en la base de datos, para no duplicar datos.

**Problemas y soluciones**
1. Error 429 "Too Many Requests".
   Causa: superar el límite de la API (4 peticiones por segundo o 500 por hora).
   Solución: el cliente espera 0,3 segundos entre peticiones y reintenta hasta tres veces si le dicen que espere.

2. La API rechaza las peticiones.
   Causa: la API pide un User-Agent propio que identifique la aplicación.
   Solución: JolpicaClient lo envía en todas las peticiones.

3. La aplicación arranca pero sin datos.
   Causa: la API no responde o no hay conexión a internet.
   Solución: el runner captura el error y lo escribe en el log. Se reinicia cuando la API vuelva.

4. "Database may be already in use".
   Causa: dos copias de la aplicación usando el mismo archivo de base de datos.
   Solución: cerrar la que sobra antes de arrancar de nuevo.

## Lunes 2 - Miércoles 11 de noviembre de 2026

**Hecho**

- Actualización de main y creación de la rama feature/feed-principal para la portada.
- Incorporación al proyecto de los archivos de la portada: entidad Noticia y su repositorio, carga de noticias de ejemplo, servicio del horario del fin de semana, controlador de la portada, plantillas Thymeleaf (index y fragmentos) y hoja de estilos.
- Nuevas propiedades en application.properties para el modo demostración (paddock.demo.ronda y paddock.demo.hoy).
- Comprobación con git status de que los 9 archivos (8 nuevos y 1 modificado) aparecen en la rama correcta.

**Decisiones**

- La portada muestra una carrera fija (ronda 1 de 2025, Gran Premio de Australia) con una fecha "de hoy" simulada, porque la temporada importada está cerrada y no hay un "próximo fin de semana" real.
- El horario de las sesiones es de ejemplo y las noticias son guías escritas para el proyecto, para no copiar contenido con derechos de autor ni inventar hechos.
- Sin JavaScript: la cuenta atrás se calcula en el servidor.

**Problemas y soluciones**

- La página sale sin estilos si el navegador no carga estilos.css o las fuentes de Google (sin internet).
- Error de Thymeleaf al leer los datos del servicio si hay un fallo de sintaxis en la plantilla.