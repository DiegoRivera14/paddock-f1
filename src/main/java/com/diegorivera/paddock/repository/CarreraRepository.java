package com.diegorivera.paddock.repository;

import com.diegorivera.paddock.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    // Calendario completo de una temporada, en orden de ronda
    List<Carrera> findByTemporadaOrderByRonda(int temporada);

    Optional<Carrera> findByTemporadaAndRonda(int temporada, int ronda);
}
