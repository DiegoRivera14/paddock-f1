package com.diegorivera.paddock.importador;

import com.diegorivera.paddock.importador.JolpicaDto.ApiCircuit;
import com.diegorivera.paddock.importador.JolpicaDto.ApiConstructor;
import com.diegorivera.paddock.importador.JolpicaDto.ApiDriver;
import com.diegorivera.paddock.importador.JolpicaDto.ApiRace;
import com.diegorivera.paddock.importador.JolpicaDto.ApiResult;
import com.diegorivera.paddock.model.Carrera;
import com.diegorivera.paddock.model.Circuito;
import com.diegorivera.paddock.model.Escuderia;
import com.diegorivera.paddock.model.Piloto;
import com.diegorivera.paddock.model.Resultado;
import com.diegorivera.paddock.repository.CarreraRepository;
import com.diegorivera.paddock.repository.CircuitoRepository;
import com.diegorivera.paddock.repository.EscuderiaRepository;
import com.diegorivera.paddock.repository.PilotoRepository;
import com.diegorivera.paddock.repository.ResultadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Descarga una temporada completa de la API y la guarda en nuestra base de datos.
 *
 * Todo ocurre en una sola transacción: si algo falla a mitad (por ejemplo, la API
 * deja de responder), no se guarda nada y la base de datos queda como estaba.
 */
@Service
public class ImportadorService {

    private static final Logger log = LoggerFactory.getLogger(ImportadorService.class);

    private final JolpicaClient api;
    private final CircuitoRepository circuitoRepository;
    private final EscuderiaRepository escuderiaRepository;
    private final PilotoRepository pilotoRepository;
    private final CarreraRepository carreraRepository;
    private final ResultadoRepository resultadoRepository;

    public ImportadorService(JolpicaClient api,
                             CircuitoRepository circuitoRepository,
                             EscuderiaRepository escuderiaRepository,
                             PilotoRepository pilotoRepository,
                             CarreraRepository carreraRepository,
                             ResultadoRepository resultadoRepository) {
        this.api = api;
        this.circuitoRepository = circuitoRepository;
        this.escuderiaRepository = escuderiaRepository;
        this.pilotoRepository = pilotoRepository;
        this.carreraRepository = carreraRepository;
        this.resultadoRepository = resultadoRepository;
    }

    /** Resultados de una carrera ya descargados, pendientes de guardar. */
    private record ResultadosDeCarrera(Carrera carrera, List<ApiResult> resultados) {
    }

    @Transactional
    public void importarTemporada(int temporada) {
        log.info("Importando la temporada {}...", temporada);

        Map<String, Circuito> circuitos = importarCircuitos(temporada);
        Map<String, Escuderia> escuderias = importarEscuderias(temporada);
        List<Carrera> carreras = importarCalendario(temporada, circuitos);
        importarPilotosYResultados(temporada, carreras, escuderias);

        log.info("Importación de la temporada {} terminada", temporada);
    }

    private Map<String, Circuito> importarCircuitos(int temporada) {
        Map<String, Circuito> circuitos = new HashMap<>();
        for (ApiCircuit c : api.circuitos(temporada)) {
            Circuito circuito = new Circuito(
                    c.circuitId(),
                    c.circuitName(),
                    c.location().locality(),
                    c.location().country(),
                    Double.parseDouble(c.location().lat()),
                    Double.parseDouble(c.location().lng()));
            circuitos.put(c.circuitId(), circuitoRepository.save(circuito));
        }
        log.info("Circuitos importados: {}", circuitos.size());
        return circuitos;
    }

    private Map<String, Escuderia> importarEscuderias(int temporada) {
        Map<String, Escuderia> escuderias = new HashMap<>();
        for (ApiConstructor c : api.escuderias(temporada)) {
            escuderias.put(c.constructorId(), guardarEscuderia(c));
        }
        log.info("Escuderías importadas: {}", escuderias.size());
        return escuderias;
    }

    private Escuderia guardarEscuderia(ApiConstructor c) {
        Escuderia escuderia = new Escuderia(
                c.constructorId(),
                c.name(),
                c.nationality(),
                ColoresEscuderia.paraEquipo(c.constructorId()));
        return escuderiaRepository.save(escuderia);
    }

    private List<Carrera> importarCalendario(int temporada, Map<String, Circuito> circuitos) {
        List<Carrera> carreras = new ArrayList<>();
        for (ApiRace r : api.calendario(temporada)) {
            Circuito circuito = circuitos.get(r.circuit().circuitId());
            if (circuito == null) {
                throw new IllegalStateException("Circuito desconocido: " + r.circuit().circuitId());
            }
            Carrera carrera = new Carrera(
                    temporada,
                    Integer.parseInt(r.round()),
                    r.raceName(),
                    LocalDate.parse(r.date()),
                    circuito);
            carreras.add(carreraRepository.save(carrera));
        }
        log.info("Carreras importadas: {}", carreras.size());
        return carreras;
    }

    /**
     * La API no dice en qué equipo está un piloto, solo en qué equipo corrió cada
     * carrera. Por eso los pilotos se crean a partir de los resultados, y se les
     * asigna el último equipo con el que corrieron.
     */
    private void importarPilotosYResultados(int temporada, List<Carrera> carreras,
                                            Map<String, Escuderia> escuderias) {
        Map<String, ApiDriver> datosPilotos = new LinkedHashMap<>();
        Map<String, Escuderia> ultimoEquipo = new HashMap<>();
        List<ResultadosDeCarrera> pendientes = new ArrayList<>();

        // 1. Descargar los resultados de cada carrera ya disputada
        for (Carrera carrera : carreras) {
            Optional<ApiRace> descargada = api.resultados(temporada, carrera.getRonda());
            if (descargada.isEmpty() || descargada.get().results() == null) {
                log.info("La ronda {} todavía no tiene resultados", carrera.getRonda());
                continue;
            }
            List<ApiResult> resultados = descargada.get().results();
            for (ApiResult r : resultados) {
                String idPiloto = r.driver().driverId();
                datosPilotos.putIfAbsent(idPiloto, r.driver());
                ultimoEquipo.put(idPiloto, escuderiaDe(r.constructor(), escuderias));
            }
            pendientes.add(new ResultadosDeCarrera(carrera, resultados));
        }

        // 2. Guardar los pilotos
        Map<String, Piloto> pilotos = new HashMap<>();
        for (Map.Entry<String, ApiDriver> entrada : datosPilotos.entrySet()) {
            ApiDriver d = entrada.getValue();
            Piloto piloto = new Piloto(
                    d.driverId(),
                    d.givenName(),
                    d.familyName(),
                    d.code(),
                    d.permanentNumber() == null ? null : Integer.valueOf(d.permanentNumber()),
                    d.nationality(),
                    d.dateOfBirth() == null ? null : LocalDate.parse(d.dateOfBirth()),
                    ultimoEquipo.get(entrada.getKey()));
            pilotos.put(entrada.getKey(), pilotoRepository.save(piloto));
        }
        log.info("Pilotos importados: {}", pilotos.size());

        // 3. Guardar los resultados
        int total = 0;
        for (ResultadosDeCarrera pendiente : pendientes) {
            for (ApiResult r : pendiente.resultados()) {
                Resultado resultado = new Resultado(
                        pendiente.carrera(),
                        pilotos.get(r.driver().driverId()),
                        escuderiaDe(r.constructor(), escuderias),
                        Integer.parseInt(r.grid()),
                        posicionFinal(r),
                        Double.parseDouble(r.points()),
                        r.status(),
                        mejorVuelta(r));
                resultadoRepository.save(resultado);
                total++;
            }
        }
        log.info("Resultados importados: {}", total);
    }

    /** Devuelve la escudería ya guardada o, si no estaba en la lista de la temporada, la crea. */
    private Escuderia escuderiaDe(ApiConstructor c, Map<String, Escuderia> escuderias) {
        return escuderias.computeIfAbsent(c.constructorId(), id -> guardarEscuderia(c));
    }

    /** Posición final solo si terminó la carrera; null si se retiró o fue descalificado. */
    private Integer posicionFinal(ApiResult r) {
        if (r.positionText() != null && r.positionText().matches("\\d+")) {
            return Integer.valueOf(r.position());
        }
        return null;
    }

    private String mejorVuelta(ApiResult r) {
        if (r.fastestLap() == null || r.fastestLap().time() == null) {
            return null;
        }
        return r.fastestLap().time().time();
    }
}
