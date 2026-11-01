package com.diegorivera.paddock.importador;

import java.util.Map;

/**
 * La API no da colores de equipo, así que los definimos aquí. Se usan en el CSS
 * para pintar las tarjetas en lugar de logotipos oficiales.
 * Si aparece un equipo que no está en la lista, se usa un gris neutro.
 */
final class ColoresEscuderia {

    private static final String COLOR_POR_DEFECTO = "#808080";

    private static final Map<String, String> COLORES = Map.of(
            "red_bull", "#3671C6",
            "ferrari", "#E8002D",
            "mercedes", "#27F4D2",
            "mclaren", "#FF8000",
            "aston_martin", "#229971",
            "alpine", "#FF87BC",
            "williams", "#64C4FF",
            "rb", "#6692FF",
            "haas", "#B6BABD",
            "sauber", "#52E252");

    private ColoresEscuderia() {
        // Clase de utilidad: no se instancia
    }

    static String paraEquipo(String constructorId) {
        return COLORES.getOrDefault(constructorId, COLOR_POR_DEFECTO);
    }
}
