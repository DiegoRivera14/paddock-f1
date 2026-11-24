package com.diegorivera.paddock.controller;

import com.diegorivera.paddock.service.PilotoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Páginas de pilotos: "/pilotos" muestra el listado y "/pilotos/{id}" el perfil
 * de uno concreto (el id es el de la API, por ejemplo "max_verstappen").
 */
@Controller
public class PilotoController {

    private final PilotoService pilotoService;

    public PilotoController(PilotoService pilotoService) {
        this.pilotoService = pilotoService;
    }

    @GetMapping("/pilotos")
    public String listado(Model model) {
        model.addAttribute("pilotos", pilotoService.listar());
        return "pilotos";
    }

    @GetMapping("/pilotos/{id}")
    public String perfil(@PathVariable String id, Model model) {
        // Si el piloto no existe se responde con un 404 en lugar de una página rota
        model.addAttribute("piloto", pilotoService.perfil(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Piloto no encontrado")));
        return "piloto";
    }
}
