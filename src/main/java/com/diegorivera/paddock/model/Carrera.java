package com.diegorivera.paddock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/**
 * Gran Premio de una temporada. Una carrera se disputa en un circuito.
 */
@Entity
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int temporada;
    private int ronda;
    private String nombre;
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    private Circuito circuito;

    protected Carrera() {
        // Constructor sin argumentos que exige JPA
    }

    public Carrera(int temporada, int ronda, String nombre, LocalDate fecha, Circuito circuito) {
        this.temporada = temporada;
        this.ronda = ronda;
        this.nombre = nombre;
        this.fecha = fecha;
        this.circuito = circuito;
    }

    public Long getId() { return id; }
    public int getTemporada() { return temporada; }
    public int getRonda() { return ronda; }
    public String getNombre() { return nombre; }
    public LocalDate getFecha() { return fecha; }
    public Circuito getCircuito() { return circuito; }
}
