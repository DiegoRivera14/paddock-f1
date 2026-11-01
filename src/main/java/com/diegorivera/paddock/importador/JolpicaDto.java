package com.diegorivera.paddock.importador;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Estructuras que representan el JSON que devuelve la API Jolpica-F1.
 *
 * Los nombres de los campos son los de la API (en inglés) porque Jackson los usa
 * para convertir el JSON en objetos Java. Los campos que no se declaran aquí
 * se ignoran.
 *
 * Todos los valores numéricos llegan como texto ("1", "25"), así que aquí son String
 * y se convierten después en el importador.
 */
public final class JolpicaDto {

    private JolpicaDto() {
        // Clase solo para agrupar los records
    }

    /** Raíz de toda respuesta: {"MRData": {...}} */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respuesta(@JsonProperty("MRData") MRData datos) {
    }

    /** Contiene una sola tabla según lo que se haya pedido; las demás llegan a null. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MRData(
            @JsonProperty("CircuitTable") TablaCircuitos tablaCircuitos,
            @JsonProperty("ConstructorTable") TablaEscuderias tablaEscuderias,
            @JsonProperty("RaceTable") TablaCarreras tablaCarreras) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TablaCircuitos(@JsonProperty("Circuits") List<ApiCircuit> circuits) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TablaEscuderias(@JsonProperty("Constructors") List<ApiConstructor> constructors) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TablaCarreras(@JsonProperty("Races") List<ApiRace> races) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiCircuit(
            String circuitId,
            String circuitName,
            @JsonProperty("Location") ApiLocation location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiLocation(
            String locality,
            String country,
            String lat,
            @JsonProperty("long") String lng) {   // "long" es una palabra reservada de Java
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiConstructor(
            String constructorId,
            String name,
            String nationality) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiDriver(
            String driverId,
            String permanentNumber,
            String code,
            String givenName,
            String familyName,
            String dateOfBirth,
            String nationality) {
    }

    /** Una carrera. La lista "Results" solo viene cuando se piden los resultados. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiRace(
            String season,
            String round,
            String raceName,
            String date,
            @JsonProperty("Circuit") ApiCircuit circuit,
            @JsonProperty("Results") List<ApiResult> results) {
    }

    /** Resultado de un piloto en una carrera. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResult(
            String position,
            String positionText,   // número si terminó, "R" si se retiró, "D" si fue descalificado...
            String points,
            String grid,
            String status,
            @JsonProperty("Driver") ApiDriver driver,
            @JsonProperty("Constructor") ApiConstructor constructor,
            @JsonProperty("FastestLap") ApiFastestLap fastestLap) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiFastestLap(
            String rank,
            String lap,
            @JsonProperty("Time") ApiTime time) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiTime(String time) {
    }
}
