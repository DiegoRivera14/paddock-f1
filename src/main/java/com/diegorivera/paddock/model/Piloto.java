package com.diegorivera.paddock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

/**
 * Piloto de la temporada. Pertenece a una escudería.
 */
@Entity
public class Piloto {

    @Id
    private String id;

    private String nombre;
    private String apellido;
    private String codigo;
    private Integer numero;
    private String nacionalidad;
    private LocalDate fechaNacimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    private Escuderia escuderia;

    protected Piloto() {
        // Constructor sin argumentos que exige JPA
    }

    public Piloto(String id, String nombre, String apellido, String codigo, Integer numero,
                  String nacionalidad, LocalDate fechaNacimiento, Escuderia escuderia) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.codigo = codigo;
        this.numero = numero;
        this.nacionalidad = nacionalidad;
        this.fechaNacimiento = fechaNacimiento;
        this.escuderia = escuderia;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getCodigo() { return codigo; }
    public Integer getNumero() { return numero; }
    public String getNacionalidad() { return nacionalidad; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public Escuderia getEscuderia() { return escuderia; }
}
