package com.diegorivera.paddock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Resultado de un piloto en una carrera.
 * Con estos registros se calculan victorias, podios, poles y puntos.
 */
@Entity
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    private Piloto piloto;

    @ManyToOne(fetch = FetchType.LAZY)
    private Escuderia escuderia;

    private int posicionSalida;
    private Integer posicionFinal;   // null si no terminó o no clasificó
    private double puntos;
    private String estado;           // "Finished", "Retired"...
    private String mejorVuelta;      // formato "1:23.456", puede ser null

    protected Resultado() {
        // Constructor sin argumentos que exige JPA
    }

    public Resultado(Carrera carrera, Piloto piloto, Escuderia escuderia, int posicionSalida,
                     Integer posicionFinal, double puntos, String estado, String mejorVuelta) {
        this.carrera = carrera;
        this.piloto = piloto;
        this.escuderia = escuderia;
        this.posicionSalida = posicionSalida;
        this.posicionFinal = posicionFinal;
        this.puntos = puntos;
        this.estado = estado;
        this.mejorVuelta = mejorVuelta;
    }

    public Long getId() { return id; }
    public Carrera getCarrera() { return carrera; }
    public Piloto getPiloto() { return piloto; }
    public Escuderia getEscuderia() { return escuderia; }
    public int getPosicionSalida() { return posicionSalida; }
    public Integer getPosicionFinal() { return posicionFinal; }
    public double getPuntos() { return puntos; }
    public String getEstado() { return estado; }
    public String getMejorVuelta() { return mejorVuelta; }
}
