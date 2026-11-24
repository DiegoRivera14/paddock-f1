package com.diegorivera.paddock.controller;

import com.diegorivera.paddock.repository.NoticiaRepository;
import com.diegorivera.paddock.service.ClasificacionService;
import com.diegorivera.paddock.service.FinDeSemanaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de la portada. Recibe la petición a "/", pide los datos a los
 * servicios y repositorios, los pasa a la plantilla y devuelve su nombre
 * ("index" es el archivo templates/index.html).
 */
@Controller
public class PortadaController {

    private final FinDeSemanaService finDeSemanaService;
    private final ClasificacionService clasificacionService;
    private final NoticiaRepository noticiaRepository;

    public PortadaController(FinDeSemanaService finDeSemanaService,
                             ClasificacionService clasificacionService,
                             NoticiaRepository noticiaRepository) {
        this.finDeSemanaService = finDeSemanaService;
        this.clasificacionService = clasificacionService;
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping("/")
    public String portada(Model model) {
        // Si no hay datos todavía, "fin" y "podio" son null y la plantilla oculta esas secciones
        model.addAttribute("fin", finDeSemanaService.actual().orElse(null));
        model.addAttribute("podio", clasificacionService.ultimoPodio().orElse(null));
        model.addAttribute("top5", clasificacionService.primeros(5));
        model.addAttribute("temporada", clasificacionService.temporada());
        model.addAttribute("noticias", noticiaRepository.findTop5ByOrderByFechaDesc());
        return "index";
    }
}
