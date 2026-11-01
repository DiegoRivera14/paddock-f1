package com.diegorivera.paddock.importador;

import com.diegorivera.paddock.repository.CarreraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Se ejecuta una vez, justo después de arrancar la aplicación.
 * Si la base de datos aún no tiene la temporada, la importa desde la API.
 * Si ya la tiene, no hace nada: así no se repiten las peticiones ni los datos.
 */
@Component
public class ImportadorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportadorRunner.class);

    private final ImportadorService importador;
    private final CarreraRepository carreraRepository;
    private final int temporada;

    public ImportadorRunner(ImportadorService importador,
                            CarreraRepository carreraRepository,
                            @Value("${paddock.temporada}") int temporada) {
        this.importador = importador;
        this.carreraRepository = carreraRepository;
        this.temporada = temporada;
    }

    @Override
    public void run(String... args) {
        if (!carreraRepository.findByTemporadaOrderByRonda(temporada).isEmpty()) {
            log.info("La temporada {} ya está en la base de datos. No se importa nada.", temporada);
            return;
        }
        try {
            importador.importarTemporada(temporada);
        } catch (Exception e) {
            // Si la API falla, la aplicación arranca igualmente, aunque sin datos
            log.error("No se pudo importar la temporada {}: {}", temporada, e.getMessage(), e);
        }
    }
}
