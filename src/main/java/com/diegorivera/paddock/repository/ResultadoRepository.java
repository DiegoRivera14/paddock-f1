package com.diegorivera.paddock.repository;

import com.diegorivera.paddock.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    // Resultados de una carrera ordenados por posición final
    List<Resultado> findByCarreraIdOrderByPosicionFinal(Long carreraId);

    // Todos los resultados de un piloto
    List<Resultado> findByPilotoId(String pilotoId);
}
