package com.diegorivera.paddock.config;

import com.diegorivera.paddock.model.Noticia;
import com.diegorivera.paddock.repository.NoticiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Guarda unas noticias de ejemplo la primera vez que arranca la aplicación.
 * Son textos propios sobre cómo funciona la F1, no noticias de actualidad.
 */
@Component
public class NoticiasIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(NoticiasIniciales.class);

    private final NoticiaRepository noticiaRepository;

    public NoticiasIniciales(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @Override
    public void run(String... args) {
        if (noticiaRepository.count() > 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        noticiaRepository.saveAll(List.of(
                new Noticia("Así es un fin de semana de Gran Premio", "Guía",
                        "El viernes y el sábado por la mañana se disputan los entrenamientos libres, la "
                                + "clasificación del sábado decide la parrilla y la carrera se corre el domingo. "
                                + "En los fines de semana con sprint hay una sola sesión de libres y se añaden "
                                + "una clasificación y una carrera cortas.",
                        hoy.minusDays(1)),
                new Noticia("Qué es la pole position", "Reglamento",
                        "Es el primer puesto de la parrilla y se lo lleva el piloto más rápido de la "
                                + "clasificación. Esta se divide en tres tramos: en los dos primeros van quedando "
                                + "eliminados los más lentos, y los diez mejores se disputan la pole en el último.",
                        hoy.minusDays(2)),
                new Noticia("Cómo se reparten los puntos", "Reglamento",
                        "Los diez primeros de cada carrera puntúan: 25, 18, 15, 12, 10, 8, 6, 4, 2 y 1 puntos. "
                                + "En las carreras sprint puntúan los ocho primeros, con 8, 7, 6, 5, 4, 3, 2 y 1.",
                        hoy.minusDays(3)),
                new Noticia("Los neumáticos, explicados", "Guía",
                        "Para el seco hay compuestos blandos, medios y duros, con las marcas roja, amarilla y "
                                + "blanca. Para el agua, intermedios (verdes) y de lluvia (azules). Los blandos dan "
                                + "más agarre pero se desgastan antes. En una carrera en seco hay que usar al "
                                + "menos dos compuestos distintos.",
                        hoy.minusDays(4)),
                new Noticia("Qué significan las banderas", "Guía",
                        "La verde indica pista libre, la amarilla avisa de un peligro y prohíbe adelantar en "
                                + "esa zona, la azul ordena a un piloto doblado dejar paso, la roja detiene la "
                                + "sesión y la de cuadros blancos y negros marca el final.",
                        hoy.minusDays(5)),
                new Noticia("Pilotos y constructores: dos campeonatos", "Campeonato",
                        "Cada fin de semana se suman puntos a dos clasificaciones. La de pilotos premia el "
                                + "resultado individual y la de constructores suma los puntos de los dos pilotos "
                                + "de cada escudería. Los dos títulos se reparten por separado.",
                        hoy.minusDays(6))));
        log.info("Noticias de ejemplo guardadas");
    }
}
