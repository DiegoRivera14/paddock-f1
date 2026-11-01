package com.diegorivera.paddock.importador;

import com.diegorivera.paddock.importador.JolpicaDto.ApiCircuit;
import com.diegorivera.paddock.importador.JolpicaDto.ApiConstructor;
import com.diegorivera.paddock.importador.JolpicaDto.ApiRace;
import com.diegorivera.paddock.importador.JolpicaDto.MRData;
import com.diegorivera.paddock.importador.JolpicaDto.Respuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Cliente de la API pública Jolpica-F1: hace las peticiones HTTP y devuelve
 * los datos ya convertidos en objetos Java.
 *
 * La API limita a 4 peticiones por segundo y 500 por hora, y exige que cada
 * petición lleve un User-Agent que identifique la aplicación.
 */
@Component
public class JolpicaClient {

    private static final Logger log = LoggerFactory.getLogger(JolpicaClient.class);

    private static final long PAUSA_ENTRE_PETICIONES_MS = 300;
    private static final long PAUSA_SI_NOS_LIMITAN_MS = 5000;
    private static final int MAX_INTENTOS = 3;

    private final RestClient restClient;

    public JolpicaClient(@Value("${paddock.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "PaddockF1/0.1 (proyecto de fin de ciclo DAW)")
                .build();
    }

    /** Circuitos en los que se corre en una temporada. */
    public List<ApiCircuit> circuitos(int temporada) {
        MRData datos = pedir("/" + temporada + "/circuits/?limit=100");
        return datos.tablaCircuitos().circuits();
    }

    /** Escuderías que participan en una temporada. */
    public List<ApiConstructor> escuderias(int temporada) {
        MRData datos = pedir("/" + temporada + "/constructors/?limit=100");
        return datos.tablaEscuderias().constructors();
    }

    /** Calendario completo de una temporada, con sus carreras ya disputadas o no. */
    public List<ApiRace> calendario(int temporada) {
        MRData datos = pedir("/" + temporada + "/races/?limit=100");
        return datos.tablaCarreras().races();
    }

    /** Resultados de una carrera. Vacío si la carrera todavía no se ha disputado. */
    public Optional<ApiRace> resultados(int temporada, int ronda) {
        MRData datos = pedir("/" + temporada + "/" + ronda + "/results/?limit=100");
        return datos.tablaCarreras().races().stream().findFirst();
    }

    /** Hace una petición GET respetando los límites de la API. */
    private MRData pedir(String ruta) {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            pausar(PAUSA_ENTRE_PETICIONES_MS);
            try {
                Respuesta respuesta = restClient.get().uri(ruta).retrieve().body(Respuesta.class);
                if (respuesta == null || respuesta.datos() == null) {
                    throw new IllegalStateException("Respuesta vacía de la API: " + ruta);
                }
                return respuesta.datos();
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("La API pide esperar (error 429). Intento {} de {}", intento, MAX_INTENTOS);
                pausar(PAUSA_SI_NOS_LIMITAN_MS);
            }
        }
        throw new IllegalStateException("La API sigue rechazando la petición: " + ruta);
    }

    private void pausar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
