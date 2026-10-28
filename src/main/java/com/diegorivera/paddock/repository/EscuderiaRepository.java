package com.diegorivera.paddock.repository;

import com.diegorivera.paddock.model.Escuderia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscuderiaRepository extends JpaRepository<Escuderia, String> {
}
