package com.diegorivera.paddock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Noticia o artículo breve de la portada. Se escriben a mano y se guardan
 * en la base de datos, porque no existe una fuente gratuita de noticias de F1
 * y copiar las de otras webs tendría problemas de derechos de autor.
 */
@Entity
public class Noticia {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.of("es", "ES"));

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @Column(length = 600)   // por defecto una columna de texto admite solo 255 caracteres
    private String resumen;

    private String categoria;
    private LocalDate fecha;

    protected Noticia() {
        // Constructor sin argumentos que exige JPA
    }

    public Noticia(String titulo, String categoria, String resumen, LocalDate fecha) {
        this.titulo = titulo;
        this.categoria = categoria;
        this.resumen = resumen;
        this.fecha = fecha;
    }

    /** Fecha lista para mostrar, por ejemplo "1 nov 2026". No es una columna de la tabla. */
    public String getFechaTexto() {
        return fecha.format(FORMATO_FECHA);
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getResumen() { return resumen; }
    public String getCategoria() { return categoria; }
    public LocalDate getFecha() { return fecha; }
}
