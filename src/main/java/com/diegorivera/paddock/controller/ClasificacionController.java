package com.diegorivera.paddock.controller;

import com.diegorivera.paddock.service.ClasificacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Página de clasificaciones. Con "?tipo=constructores" muestra la de escuderías;
 * sin parámetro (o con cualquier otro valor), la de pilotos.
 */
@Controller
public class ClasificacionController {

    private final ClasificacionService clasificacionService;

    public ClasificacionController(ClasificacionService clasificacionService) {
        this.clasificacionService = clasificacionService;
    }

    @GetMapping("/clasificacion")
    public String clasificacion(@RequestParam(defaultValue = "pilotos") String tipo, Model model) {
        boolean constructores = tipo.equals("constructores");
        model.addAttribute("tipo", constructores ? "constructores" : "pilotos");
        model.addAttribute("temporada", clasificacionService.temporada());
        if (constructores) {
            model.addAttribute("escuderias", clasificacionService.clasificacionEscuderias());
        } else {
            model.addAttribute("pilotos", clasificacionService.clasificacionPilotos());
        }
        return "clasificacion";
    }
}
