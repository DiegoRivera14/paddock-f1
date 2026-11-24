package com.diegorivera.paddock.service;

import com.diegorivera.paddock.model.Carrera;
import com.diegorivera.paddock.model.Escuderia;
import com.diegorivera.paddock.model.Piloto;
import com.diegorivera.paddock.model.Resultado;
import com.diegorivera.paddock.repository.CarreraRepository;
import com.diegorivera.paddock.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Calcula a partir de los resultados guardados la clasificación del campeonato
 * de pilotos y el podio de la última carrera. No se guarda nada nuevo en la base
 * de datos: los puntos y las victorias se suman cada vez que se piden.
 */
@Service
public class ClasificacionService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    /** Una fila de la clasificación de pilotos. */
    public record FilaPiloto(int posicion, String pilotoId, String nombre, String escuderia,
                             String color, double puntos, int victorias, int podios) {

        /** Los puntos sin decimales cuando son enteros: "25" en vez de "25.0". */
        public String puntosTexto() {
            return formatearPuntos(puntos);
        }
    }

    /** Una fila de la clasificación de constructores (escuderías). */
    public record FilaEscuderia(int posicion, String escuderiaId, String nombre, String color,
                                double puntos, int victorias, int podios) {

        public String puntosTexto() {
            return formatearPuntos(puntos);
        }
    }

    /** Uno de los tres primeros puestos de una carrera. */
    public record PuestoPodio(int posicion, String piloto, String escuderia, String color,
                              String salida, String puntos) {
    }

    /** El podio de una carrera, con los datos para titularlo. */
    public record Podio(int ronda, String carrera, String circuito, String fecha,
                        List<PuestoPodio> puestos) {
    }

    /** Datos que se van sumando de cada piloto mientras se recorren los resultados. */
    private static class Acumulado {
        private final Piloto piloto;
        private Escuderia escuderia;
        private double puntos;
        private int victorias;
        private int podios;

        private Acumulado(Piloto piloto) {
            this.piloto = piloto;
        }
    }

    /** Datos que se van sumando de cada escudería. */
    private static class AcumuladoEscuderia {
        private final Escuderia escuderia;
        private double puntos;
        private int victorias;
        private int podios;

        private AcumuladoEscuderia(Escuderia escuderia) {
            this.escuderia = escuderia;
        }
    }

    private final ResultadoRepository resultadoRepository;
    private final CarreraRepository carreraRepository;
    private final int temporada;

    public ClasificacionService(ResultadoRepository resultadoRepository,
                                CarreraRepository carreraRepository,
                                @Value("${paddock.temporada}") int temporada) {
        this.resultadoRepository = resultadoRepository;
        this.carreraRepository = carreraRepository;
        this.temporada = temporada;
    }

    public int temporada() {
        return temporada;
    }

    /** Clasificación completa de pilotos: más puntos primero; si empatan, más victorias. */
    @Transactional(readOnly = true)
    public List<FilaPiloto> clasificacionPilotos() {
        List<Resultado> resultados = new ArrayList<>(resultadoRepository.findByCarreraTemporada(temporada));
        // En orden de ronda, así la última escudería que se ve de cada piloto es la actual
        resultados.sort(Comparator.comparingInt(r -> r.getCarrera().getRonda()));

        Map<String, Acumulado> porPiloto = new LinkedHashMap<>();
        for (Resultado r : resultados) {
            Acumulado a = porPiloto.computeIfAbsent(r.getPiloto().getId(), id -> new Acumulado(r.getPiloto()));
            a.puntos += r.getPuntos();
            Integer llegada = r.getPosicionFinal();
            if (llegada != null && llegada == 1) {
                a.victorias++;
            }
            if (llegada != null && llegada <= 3) {
                a.podios++;
            }
            a.escuderia = r.getEscuderia();
        }

        List<Acumulado> ordenados = new ArrayList<>(porPiloto.values());
        ordenados.sort((x, y) -> {
            int c = Double.compare(y.puntos, x.puntos);
            if (c != 0) {
                return c;
            }
            c = Integer.compare(y.victorias, x.victorias);
            if (c != 0) {
                return c;
            }
            return x.piloto.getApellido().compareTo(y.piloto.getApellido());
        });

        List<FilaPiloto> filas = new ArrayList<>();
        int posicion = 1;
        for (Acumulado a : ordenados) {
            filas.add(new FilaPiloto(posicion++, a.piloto.getId(), a.piloto.getNombreCompleto(),
                    nombreEscuderia(a.escuderia), colorEscuderia(a.escuderia),
                    a.puntos, a.victorias, a.podios));
        }
        return filas;
    }

    /** Clasificación de constructores: suma de los puntos de todos los pilotos de cada escudería. */
    @Transactional(readOnly = true)
    public List<FilaEscuderia> clasificacionEscuderias() {
        Map<String, AcumuladoEscuderia> porEscuderia = new LinkedHashMap<>();
        for (Resultado r : resultadoRepository.findByCarreraTemporada(temporada)) {
            Escuderia e = r.getEscuderia();
            if (e == null) {
                continue;
            }
            AcumuladoEscuderia a = porEscuderia.computeIfAbsent(e.getId(), id -> new AcumuladoEscuderia(e));
            a.puntos += r.getPuntos();
            Integer llegada = r.getPosicionFinal();
            if (llegada != null && llegada == 1) {
                a.victorias++;
            }
            if (llegada != null && llegada <= 3) {
                a.podios++;
            }
        }

        List<AcumuladoEscuderia> ordenadas = new ArrayList<>(porEscuderia.values());
        ordenadas.sort((x, y) -> {
            int c = Double.compare(y.puntos, x.puntos);
            if (c != 0) {
                return c;
            }
            c = Integer.compare(y.victorias, x.victorias);
            if (c != 0) {
                return c;
            }
            return x.escuderia.getNombre().compareTo(y.escuderia.getNombre());
        });

        List<FilaEscuderia> filas = new ArrayList<>();
        int posicion = 1;
        for (AcumuladoEscuderia a : ordenadas) {
            filas.add(new FilaEscuderia(posicion++, a.escuderia.getId(), a.escuderia.getNombre(),
                    colorEscuderia(a.escuderia), a.puntos, a.victorias, a.podios));
        }
        return filas;
    }

    /** Los primeros "n" pilotos del campeonato. */
    @Transactional(readOnly = true)
    public List<FilaPiloto> primeros(int n) {
        return clasificacionPilotos().stream().limit(n).toList();
    }

    /** Podio de la última carrera de la temporada que tenga resultados guardados. */
    @Transactional(readOnly = true)
    public Optional<Podio> ultimoPodio() {
        List<Carrera> carreras = carreraRepository.findByTemporadaOrderByRonda(temporada);
        for (int i = carreras.size() - 1; i >= 0; i--) {
            Carrera carrera = carreras.get(i);
            List<PuestoPodio> puestos = resultadoRepository.findByCarreraIdOrderByPosicionFinal(carrera.getId())
                    .stream()
                    .filter(r -> r.getPosicionFinal() != null && r.getPosicionFinal() <= 3)
                    .sorted(Comparator.comparingInt(Resultado::getPosicionFinal))
                    .map(r -> new PuestoPodio(r.getPosicionFinal(), r.getPiloto().getNombreCompleto(),
                            nombreEscuderia(r.getEscuderia()), colorEscuderia(r.getEscuderia()),
                            r.getPosicionSalida() == 0 ? "pit lane" : r.getPosicionSalida() + "º",
                            formatearPuntos(r.getPuntos())))
                    .toList();
            if (!puestos.isEmpty()) {
                return Optional.of(new Podio(carrera.getRonda(), carrera.getNombre(),
                        carrera.getCircuito().getNombre(), carrera.getFecha().format(FECHA), puestos));
            }
        }
        return Optional.empty();
    }

    static String formatearPuntos(double puntos) {
        return puntos == Math.rint(puntos) ? String.valueOf((long) puntos) : String.valueOf(puntos);
    }

    static String nombreEscuderia(Escuderia e) {
        return e == null ? "Sin escudería" : e.getNombre();
    }

    static String colorEscuderia(Escuderia e) {
        return e == null || e.getColor() == null ? "#808080" : e.getColor();
    }
}
