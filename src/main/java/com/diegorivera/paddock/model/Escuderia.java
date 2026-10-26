package com.diegorivera.paddock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Escudería (constructor) que compite en el campeonato.
 * El campo "color" guarda un valor hexadecimal propio, usado para las tarjetas
 * en lugar de logotipos oficiales.
 */
@Entity
public class Escuderia {

    @Id
    private String id;

    private String nombre;
    private String nacionalidad;
    private String color;

    protected Escuderia() {
        // Constructor sin argumentos que exige JPA
    }

    public Escuderia(String id, String nombre, String nacionalidad, String color) {
        this.id = id;
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
        this.color = color;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNacionalidad() { return nacionalidad; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
