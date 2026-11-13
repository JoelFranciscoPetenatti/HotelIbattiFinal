package com.programacion2.servicio;

import com.programacion2.repositorio.DisponibilidadRepositorio;
import com.programacion2.repositorio.DisponibilidadRepositorio.Ocupacion;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class DisponibilidadService {

    private final DisponibilidadRepositorio repositorio;

    public DisponibilidadService(DisponibilidadRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Resumen consultarEstaNoche() {
        Ocupacion ocupacion = repositorio.consultar(LocalDate.now());
        int disponibles = ocupacion.activas() - ocupacion.ocupadas();

        return new Resumen(disponibles, ocupacion.activas());
    }

    public record Resumen(int disponibles, int activas) {}
}