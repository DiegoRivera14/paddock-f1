package com.diegorivera.paddock.service;

import com.diegorivera.paddock.model.Escuderia;
import com.diegorivera.paddock.model.Piloto;
import com.diegorivera.paddock.model.Resultado;
import com.diegorivera.paddock.repository.PilotoRepository;
import com.diegorivera.paddock.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Prepara los datos de las páginas de pilotos: el listado y el perfil de cada uno.
 * Devuelve "records" ya listos para mostrar, de modo que las plantillas no tengan
 * que tocar la base de datos.
 */
@Service
public class PilotoService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "ES"));

    /** Un piloto en el listado. */
    public record PilotoResumen(String id, String nombre, String apellido, String numero, String codigo,
                                String escuderia, String color, String puntos) {
    }

    /** Resultado de un piloto en una carrera, para la tabla de su perfil. */
    public record ResultadoCarrera(int ronda, String carrera, String salida, String llegada,
                                   String puntos, String estado, String mejorVuelta) {
    }

    /** Todos los datos del perfil de un piloto. */
    public record PilotoPerfil(String id, String nombre, String apellido, String numero, String codigo,
                               String nacionalidad, String nacimiento, String escuderia, String color,
                               String posicionCampeonato, String puntos, int victorias, int podios,
                               int poles, int carreras, List<ResultadoCarrera> resultados) {
    }

    private final PilotoRepository pilotoRepository;
    private final ResultadoRepository resultadoRepository;
    private final ClasificacionService clasificacionService;
    private final int temporada;

    public PilotoService(PilotoRepository pilotoRepository,
                         ResultadoRepository resultadoRepository,
                         ClasificacionService clasificacionService,
                         @Value("${paddock.temporada}") int temporada) {
        this.pilotoRepository = pilotoRepository;
        this.resultadoRepository = resultadoRepository;
        this.clasificacionService = clasificacionService;
        this.temporada = temporada;
    }

    /** Todos los pilotos, en el orden del campeonato (los que no han puntuado, al final). */
    @Transactional(readOnly = true)
    public List<PilotoResumen> listar() {
        Map<String, ClasificacionService.FilaPiloto> clasificacion = new HashMap<>();
        for (ClasificacionService.FilaPiloto fila : clasificacionService.clasificacionPilotos()) {
            clasificacion.put(fila.pilotoId(), fila);
        }

        return pilotoRepository.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Piloto p) -> posicion(clasificacion, p))
                        .thenComparing(Piloto::getApellido))
                .map(p -> {
                    ClasificacionService.FilaPiloto fila = clasificacion.get(p.getId());
                    return new PilotoResumen(p.getId(), p.getNombre(), p.getApellido(), numero(p),
                            codigo(p), ClasificacionService.nombreEscuderia(p.getEscuderia()),
                            ClasificacionService.colorEscuderia(p.getEscuderia()),
                            fila == null ? "0" : fila.puntosTexto());
                })
                .toList();
    }

    /** Perfil de un piloto, o vacío si el identificador no existe. */
    @Transactional(readOnly = true)
    public Optional<PilotoPerfil> perfil(String id) {
        Optional<Piloto> encontrado = pilotoRepository.findById(id);
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        Piloto piloto = encontrado.get();

        List<Resultado> resultados = resultadoRepository.findByPilotoId(id).stream()
                .filter(r -> r.getCarrera().getTemporada() == temporada)
                .sorted(Comparator.comparingInt(r -> r.getCarrera().getRonda()))
                .toList();

        double puntos = 0;
        int victorias = 0;
        int podios = 0;
        int poles = 0;
        for (Resultado r : resultados) {
            puntos += r.getPuntos();
            Integer llegada = r.getPosicionFinal();
            if (llegada != null && llegada == 1) {
                victorias++;
            }
            if (llegada != null && llegada <= 3) {
                podios++;
            }
            if (r.getPosicionSalida() == 1) {
                poles++;
            }
        }

        String posicionCampeonato = "–";
        for (ClasificacionService.FilaPiloto fila : clasificacionService.clasificacionPilotos()) {
            if (fila.pilotoId().equals(id)) {
                posicionCampeonato = fila.posicion() + "º";
            }
        }

        List<ResultadoCarrera> filas = resultados.stream()
                .map(r -> new ResultadoCarrera(r.getCarrera().getRonda(), r.getCarrera().getNombre(),
                        r.getPosicionSalida() == 0 ? "Pit" : String.valueOf(r.getPosicionSalida()),
                        r.getPosicionFinal() == null ? "–" : String.valueOf(r.getPosicionFinal()),
                        ClasificacionService.formatearPuntos(r.getPuntos()),
                        traducirEstado(r.getEstado()),
                        r.getMejorVuelta() == null ? "–" : r.getMejorVuelta()))
                .toList();

        Escuderia escuderia = piloto.getEscuderia();
        return Optional.of(new PilotoPerfil(piloto.getId(), piloto.getNombre(), piloto.getApellido(),
                numero(piloto), codigo(piloto), valor(piloto.getNacionalidad()),
                piloto.getFechaNacimiento() == null ? "–" : piloto.getFechaNacimiento().format(FECHA),
                ClasificacionService.nombreEscuderia(escuderia), ClasificacionService.colorEscuderia(escuderia),
                posicionCampeonato, ClasificacionService.formatearPuntos(puntos),
                victorias, podios, poles, resultados.size(), filas));
    }

    private static int posicion(Map<String, ClasificacionService.FilaPiloto> clasificacion, Piloto p) {
        ClasificacionService.FilaPiloto fila = clasificacion.get(p.getId());
        return fila == null ? Integer.MAX_VALUE : fila.posicion();
    }

    private static String numero(Piloto p) {
        return p.getNumero() == null ? "–" : String.valueOf(p.getNumero());
    }

    private static String codigo(Piloto p) {
        return p.getCodigo() == null ? "" : p.getCodigo();
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "–" : texto;
    }

    /** El estado llega en inglés desde la API; se traducen los más comunes y el resto se deja como viene. */
    private static String traducirEstado(String estado) {
        if (estado == null) {
            return "–";
        }
        if (estado.equals("Finished")) {
            return "Finalizó";
        }
        if (estado.startsWith("+") || estado.equals("Lapped")) {
            return "Finalizó con vueltas de retraso";
        }
        if (estado.equals("Retired")) {
            return "Retirado";
        }
        if (estado.equals("Disqualified")) {
            return "Descalificado";
        }
        return estado;
    }
}
