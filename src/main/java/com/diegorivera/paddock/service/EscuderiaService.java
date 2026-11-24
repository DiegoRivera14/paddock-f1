package com.diegorivera.paddock.service;

import com.diegorivera.paddock.model.Escuderia;
import com.diegorivera.paddock.model.Piloto;
import com.diegorivera.paddock.model.Resultado;
import com.diegorivera.paddock.repository.EscuderiaRepository;
import com.diegorivera.paddock.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Prepara los datos de las páginas de escuderías: el listado y el detalle de cada una.
 * Los pilotos de una escudería se sacan de los resultados, así aparecen todos los que
 * corrieron con ella durante la temporada (también los que la cambiaron a mitad de año).
 */
@Service
public class EscuderiaService {

    /** Una escudería en el listado. */
    public record EscuderiaResumen(String id, String nombre, String nacionalidad, String color,
                                   String posicion, String puntos, List<String> pilotos) {
    }

    /** Un piloto dentro de la página de su escudería, con los puntos que sumó para ella. */
    public record PilotoDeEscuderia(String id, String nombre, String numero, String puntos) {
    }

    /** Lo que hizo la escudería en una carrera: puntos sumados y su mejor posición. */
    public record CarreraEscuderia(int ronda, String carrera, String mejorPosicion, String puntos) {
    }

    /** Todos los datos del detalle de una escudería. */
    public record EscuderiaDetalle(String id, String nombre, String nacionalidad, String color,
                                   String posicion, String puntos, int victorias, int podios,
                                   List<PilotoDeEscuderia> pilotos, List<CarreraEscuderia> carreras) {
    }

    /** Puntos de un piloto con una escudería concreta. */
    private static class PuntosPiloto {
        private final Piloto piloto;
        private double puntos;

        private PuntosPiloto(Piloto piloto) {
            this.piloto = piloto;
        }
    }

    private final EscuderiaRepository escuderiaRepository;
    private final ResultadoRepository resultadoRepository;
    private final ClasificacionService clasificacionService;
    private final int temporada;

    public EscuderiaService(EscuderiaRepository escuderiaRepository,
                            ResultadoRepository resultadoRepository,
                            ClasificacionService clasificacionService,
                            @Value("${paddock.temporada}") int temporada) {
        this.escuderiaRepository = escuderiaRepository;
        this.resultadoRepository = resultadoRepository;
        this.clasificacionService = clasificacionService;
        this.temporada = temporada;
    }

    /** Todas las escuderías, en el orden del campeonato de constructores. */
    @Transactional(readOnly = true)
    public List<EscuderiaResumen> listar() {
        Map<String, ClasificacionService.FilaEscuderia> clasificacion = new HashMap<>();
        for (ClasificacionService.FilaEscuderia fila : clasificacionService.clasificacionEscuderias()) {
            clasificacion.put(fila.escuderiaId(), fila);
        }

        // Pilotos de cada escudería según los resultados de la temporada
        Map<String, Map<String, PuntosPiloto>> pilotosPorEscuderia = new HashMap<>();
        for (Resultado r : resultadoRepository.findByCarreraTemporada(temporada)) {
            if (r.getEscuderia() == null) {
                continue;
            }
            PuntosPiloto pp = pilotosPorEscuderia
                    .computeIfAbsent(r.getEscuderia().getId(), id -> new LinkedHashMap<>())
                    .computeIfAbsent(r.getPiloto().getId(), id -> new PuntosPiloto(r.getPiloto()));
            pp.puntos += r.getPuntos();
        }

        return escuderiaRepository.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Escuderia e) -> clasificacion.containsKey(e.getId())
                                ? clasificacion.get(e.getId()).posicion() : Integer.MAX_VALUE)
                        .thenComparing(Escuderia::getNombre))
                .map(e -> {
                    ClasificacionService.FilaEscuderia fila = clasificacion.get(e.getId());
                    List<String> pilotos = ordenarPorPuntos(pilotosPorEscuderia.getOrDefault(e.getId(), Map.of()))
                            .stream().map(pp -> pp.piloto.getNombreCompleto()).toList();
                    return new EscuderiaResumen(e.getId(), e.getNombre(), valor(e.getNacionalidad()),
                            ClasificacionService.colorEscuderia(e),
                            fila == null ? "–" : String.valueOf(fila.posicion()),
                            fila == null ? "0" : fila.puntosTexto(), pilotos);
                })
                .toList();
    }

    /** Detalle de una escudería, o vacío si el identificador no existe. */
    @Transactional(readOnly = true)
    public Optional<EscuderiaDetalle> detalle(String id) {
        Optional<Escuderia> encontrada = escuderiaRepository.findById(id);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Escuderia escuderia = encontrada.get();

        List<Resultado> resultados = resultadoRepository.findByEscuderiaId(id).stream()
                .filter(r -> r.getCarrera().getTemporada() == temporada)
                .sorted(Comparator.comparingInt(r -> r.getCarrera().getRonda()))
                .toList();

        double puntos = 0;
        int victorias = 0;
        int podios = 0;
        Map<String, PuntosPiloto> porPiloto = new LinkedHashMap<>();
        // TreeMap: las carreras quedan ordenadas por número de ronda
        Map<Integer, List<Resultado>> porCarrera = new TreeMap<>();
        for (Resultado r : resultados) {
            puntos += r.getPuntos();
            Integer llegada = r.getPosicionFinal();
            if (llegada != null && llegada == 1) {
                victorias++;
            }
            if (llegada != null && llegada <= 3) {
                podios++;
            }
            porPiloto.computeIfAbsent(r.getPiloto().getId(), pid -> new PuntosPiloto(r.getPiloto())).puntos += r.getPuntos();
            porCarrera.computeIfAbsent(r.getCarrera().getRonda(), ronda -> new ArrayList<>()).add(r);
        }

        List<PilotoDeEscuderia> pilotos = ordenarPorPuntos(porPiloto).stream()
                .map(pp -> new PilotoDeEscuderia(pp.piloto.getId(), pp.piloto.getNombreCompleto(),
                        pp.piloto.getNumero() == null ? "–" : String.valueOf(pp.piloto.getNumero()),
                        ClasificacionService.formatearPuntos(pp.puntos)))
                .toList();

        List<CarreraEscuderia> carreras = new ArrayList<>();
        for (List<Resultado> deCarrera : porCarrera.values()) {
            double puntosCarrera = 0;
            Integer mejor = null;
            for (Resultado r : deCarrera) {
                puntosCarrera += r.getPuntos();
                Integer llegada = r.getPosicionFinal();
                if (llegada != null && (mejor == null || llegada < mejor)) {
                    mejor = llegada;
                }
            }
            Resultado primero = deCarrera.get(0);
            carreras.add(new CarreraEscuderia(primero.getCarrera().getRonda(), primero.getCarrera().getNombre(),
                    mejor == null ? "–" : String.valueOf(mejor),
                    ClasificacionService.formatearPuntos(puntosCarrera)));
        }

        String posicion = "–";
        for (ClasificacionService.FilaEscuderia fila : clasificacionService.clasificacionEscuderias()) {
            if (fila.escuderiaId().equals(id)) {
                posicion = fila.posicion() + "º";
            }
        }

        return Optional.of(new EscuderiaDetalle(escuderia.getId(), escuderia.getNombre(),
                valor(escuderia.getNacionalidad()), ClasificacionService.colorEscuderia(escuderia),
                posicion, ClasificacionService.formatearPuntos(puntos), victorias, podios, pilotos, carreras));
    }

    private static List<PuntosPiloto> ordenarPorPuntos(Map<String, PuntosPiloto> mapa) {
        List<PuntosPiloto> lista = new ArrayList<>(mapa.values());
        lista.sort((x, y) -> Double.compare(y.puntos, x.puntos));
        return lista;
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "–" : texto;
    }
}
