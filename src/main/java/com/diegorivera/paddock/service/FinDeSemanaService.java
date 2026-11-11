package com.diegorivera.paddock.service;

import com.diegorivera.paddock.model.Carrera;
import com.diegorivera.paddock.model.Circuito;
import com.diegorivera.paddock.repository.CarreraRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Prepara los datos del "próximo fin de semana de carrera" para la portada.
 *
 * Modo demostración: la carrera que se muestra es siempre la misma (la ronda indicada
 * en application.properties) y la fecha de "hoy" está fijada en la configuración.
 * Así la portada se ve igual cualquier día, sin depender del calendario real.
 * Los horarios de las sesiones también son de ejemplo.
 */
@Service
public class FinDeSemanaService {

    private static final Locale ES = Locale.of("es", "ES");
    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES);
    private static final DateTimeFormatter FECHA_CORTA =
            DateTimeFormatter.ofPattern("d 'de' MMMM", ES);
    private static final DateTimeFormatter DIA_SEMANA =
            DateTimeFormatter.ofPattern("EEEE", ES);

    /** Una sesión del fin de semana, por ejemplo "Libres 1" a las "02:30". */
    public record Sesion(String nombre, String hora, boolean carrera) {
    }

    /** Las sesiones de un día: viernes, sábado o domingo. */
    public record Jornada(String dia, String fecha, List<Sesion> sesiones) {
    }

    /** Todo lo que necesita la portada sobre la próxima carrera. */
    public record FinDeSemana(int ronda, int totalRondas, String nombreCarrera, String circuito,
                              String localidad, String pais, String fecha, long diasRestantes,
                              List<Jornada> jornadas) {
    }

    private final CarreraRepository carreraRepository;
    private final int temporada;
    private final int ronda;
    private final LocalDate hoy;

    public FinDeSemanaService(CarreraRepository carreraRepository,
                              @Value("${paddock.temporada}") int temporada,
                              @Value("${paddock.demo.ronda}") int ronda,
                              @Value("${paddock.demo.hoy}") String hoy) {
        this.carreraRepository = carreraRepository;
        this.temporada = temporada;
        this.ronda = ronda;
        this.hoy = LocalDate.parse(hoy);
    }

    /** Vacío si todavía no se han importado los datos de la temporada. */
    @Transactional(readOnly = true)
    public Optional<FinDeSemana> actual() {
        Optional<Carrera> encontrada = carreraRepository.findByTemporadaAndRonda(temporada, ronda);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Carrera carrera = encontrada.get();
        Circuito circuito = carrera.getCircuito();   // se carga aquí, dentro de la transacción
        LocalDate domingo = carrera.getFecha();
        LocalDate viernes = domingo.minusDays(2);
        LocalDate sabado = domingo.minusDays(1);

        List<Jornada> jornadas = List.of(
                new Jornada(capitalizar(viernes.format(DIA_SEMANA)), viernes.format(FECHA_CORTA), List.of(
                        new Sesion("Libres 1", "02:30", false),
                        new Sesion("Libres 2", "06:00", false))),
                new Jornada(capitalizar(sabado.format(DIA_SEMANA)), sabado.format(FECHA_CORTA), List.of(
                        new Sesion("Libres 3", "02:30", false),
                        new Sesion("Clasificación", "06:00", false))),
                new Jornada(capitalizar(domingo.format(DIA_SEMANA)), domingo.format(FECHA_CORTA), List.of(
                        new Sesion("Carrera", "05:00", true))));

        long dias = Math.max(0, ChronoUnit.DAYS.between(hoy, domingo));
        int totalRondas = carreraRepository.findByTemporadaOrderByRonda(temporada).size();

        return Optional.of(new FinDeSemana(
                carrera.getRonda(),
                totalRondas,
                carrera.getNombre(),
                circuito.getNombre(),
                circuito.getLocalidad(),
                circuito.getPais(),
                capitalizar(domingo.format(FECHA_LARGA)),
                dias,
                jornadas));
    }

    private String capitalizar(String texto) {
        return texto.substring(0, 1).toUpperCase(ES) + texto.substring(1);
    }
}
