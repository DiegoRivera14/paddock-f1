package com.diegorivera.paddock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Circuito donde se disputa una carrera.
 * El identificador es el que usa la API Jolpica-F1 (por ejemplo "monza").
 */
@Entity
public class Circuito {

    @Id
    private String id;

    private String nombre;
    private String localidad;
    private String pais;
    private double latitud;
    private double longitud;

    protected Circuito() {
        // Constructor sin argumentos que exige JPA
    }

    public Circuito(String id, String nombre, String localidad, String pais, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.localidad = localidad;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public String getPais() { return pais; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}
