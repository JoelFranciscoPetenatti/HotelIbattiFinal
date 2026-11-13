package com.programacion2.controlador;

import com.programacion2.servicio.DisponibilidadService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    private final DisponibilidadService disponibilidadService;

    public InicioController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("nombreHotel", "Hotel IBATTI");
        model.addAttribute("disponibilidad", disponibilidadService.consultarEstaNoche());
        return "inicio";
    }
}