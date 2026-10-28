package com.diegorivera.paddock.repository;

import com.diegorivera.paddock.model.Piloto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PilotoRepository extends JpaRepository<Piloto, String> {

    // Pilotos de una escudería, ordenados por apellido
    List<Piloto> findByEscuderiaIdOrderByApellido(String escuderiaId);
}
