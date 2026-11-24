package com.diegorivera.paddock.controller;

import com.diegorivera.paddock.service.EscuderiaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Páginas de escuderías: "/escuderias" con el listado y "/escuderias/{id}" con el
 * detalle de una (el id es el de la API, por ejemplo "red_bull").
 */
@Controller
public class EscuderiaController {

    private final EscuderiaService escuderiaService;

    public EscuderiaController(EscuderiaService escuderiaService) {
        this.escuderiaService = escuderiaService;
    }

    @GetMapping("/escuderias")
    public String listado(Model model) {
        model.addAttribute("escuderias", escuderiaService.listar());
        return "escuderias";
    }

    @GetMapping("/escuderias/{id}")
    public String detalle(@PathVariable String id, Model model) {
        model.addAttribute("escuderia", escuderiaService.detalle(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escudería no encontrada")));
        return "escuderia";
    }
}
