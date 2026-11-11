package com.diegorivera.paddock.repository;

import com.diegorivera.paddock.model.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    // Las cinco noticias más recientes, la más nueva primero
    List<Noticia> findTop5ByOrderByFechaDesc();
}
