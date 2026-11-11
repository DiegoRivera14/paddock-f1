package com.diegorivera.paddock.controller;

import com.diegorivera.paddock.repository.NoticiaRepository;
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
    private final NoticiaRepository noticiaRepository;

    public PortadaController(FinDeSemanaService finDeSemanaService, NoticiaRepository noticiaRepository) {
        this.finDeSemanaService = finDeSemanaService;
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping("/")
    public String portada(Model model) {
        // Si no hay datos todavía, "fin" es null y la plantilla muestra un aviso
        model.addAttribute("fin", finDeSemanaService.actual().orElse(null));
        model.addAttribute("noticias", noticiaRepository.findTop5ByOrderByFechaDesc());
        return "index";
    }
}
