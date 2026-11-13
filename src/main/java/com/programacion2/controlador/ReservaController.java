package com.programacion2.controlador;

import com.programacion2.dominio.EstadoReserva;
import com.programacion2.repositorio.ReservaRepositorio.OpcionesReserva;
import com.programacion2.repositorio.ReservaRepositorio.ReferenciaReserva;
import com.programacion2.servicio.ReservaService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/reservas")
    public String listar(
            @RequestParam(name = "habitacion", defaultValue = "") String habitacion,
            Model model) {
        model.addAttribute("reservas", servicio.listarPorHabitacion(habitacion));
        model.addAttribute("habitacion", habitacion.strip());
        return "reservas";
    }

    @GetMapping("/reservas/nueva")
    public String nueva(Model model) {
        return mostrarFormulario(model, "", "", "", "", "1");
    }

    @PostMapping("/reservas")
    public String guardar(
            @RequestParam(name = "huespedId", defaultValue = "") String huespedId,
            @RequestParam(name = "habitacionId", defaultValue = "") String habitacionId,
            @RequestParam(name = "ingreso", defaultValue = "") String ingreso,
            @RequestParam(name = "salida", defaultValue = "") String salida,
            @RequestParam(name = "personas", defaultValue = "") String personas,
            Model model, RedirectAttributes redirect) {
        try {
            int id = servicio.crearReserva(
                Integer.parseInt(huespedId),
                Integer.parseInt(habitacionId),
                LocalDate.parse(ingreso),
                LocalDate.parse(salida),
                Integer.parseInt(personas)
            );

            redirect.addFlashAttribute("mensaje", "Reserva guardada. ID: " + id);
            return "redirect:/reservas";
        } catch (NumberFormatException | DateTimeParseException error) {
            model.addAttribute("error",
                "Completá el huésped, la habitación, las fechas y la cantidad de personas.");
        } catch (IllegalArgumentException | IllegalStateException error) {
            model.addAttribute("error", error.getMessage());
        }

        return mostrarFormulario(model, huespedId, habitacionId, ingreso, salida, personas);
    }

    @GetMapping("/reservas/{id}/editar")
    public String editar(@PathVariable("id") int id, Model model, RedirectAttributes redirect) {
        try {
            ReferenciaReserva reserva = servicio.buscarReferencia(id);

            if (reserva.estado() == EstadoReserva.CANCELADA) {
                redirect.addFlashAttribute("error", "Una reserva cancelada no se puede editar.");
                return "redirect:/reservas";
            }

            model.addAttribute("reservaId", id);
            return mostrarFormulario(
                model,
                String.valueOf(reserva.huespedId()),
                String.valueOf(reserva.habitacionId()),
                reserva.ingreso().toString(),
                reserva.salida().toString(),
                String.valueOf(reserva.personas())
            );
        } catch (JDOObjectNotFoundException error) {
            redirect.addFlashAttribute("error", "La reserva ya no existe.");
            return "redirect:/reservas";
        }
    }

    @PostMapping("/reservas/{id}/editar")
    public String actualizar(
            @PathVariable("id") int id,
            @RequestParam(name = "huespedId", defaultValue = "") String huespedId,
            @RequestParam(name = "habitacionId", defaultValue = "") String habitacionId,
            @RequestParam(name = "ingreso", defaultValue = "") String ingreso,
            @RequestParam(name = "salida", defaultValue = "") String salida,
            @RequestParam(name = "personas", defaultValue = "") String personas,
            Model model, RedirectAttributes redirect) {
        try {
            servicio.actualizarReserva(
                id,
                Integer.parseInt(huespedId),
                Integer.parseInt(habitacionId),
                LocalDate.parse(ingreso),
                LocalDate.parse(salida),
                Integer.parseInt(personas)
            );

            redirect.addFlashAttribute("mensaje", "Reserva actualizada. ID: " + id);
            return "redirect:/reservas";
        } catch (NumberFormatException | DateTimeParseException error) {
            model.addAttribute("error",
                "Completá el huésped, la habitación, las fechas y la cantidad de personas.");
        } catch (IllegalArgumentException | IllegalStateException error) {
            model.addAttribute("error", error.getMessage());
        }

        model.addAttribute("reservaId", id);
        return mostrarFormulario(model, huespedId, habitacionId, ingreso, salida, personas);
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable("id") int id, RedirectAttributes redirect) {
        try {
            servicio.cancelar(id);
            redirect.addFlashAttribute("mensaje", "Reserva cancelada correctamente. ID: " + id);
        } catch (JDOObjectNotFoundException error) {
            redirect.addFlashAttribute("error", "La reserva ya no existe.");
        } catch (IllegalStateException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }

        return "redirect:/reservas";
    }

    @PostMapping("/reservas/{id}/eliminar")
    public String eliminar(@PathVariable("id") int id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Reserva eliminada con éxito.");
        } catch (JDOObjectNotFoundException error) {
            redirect.addFlashAttribute("error", "La reserva ya no existe.");
        }

        return "redirect:/reservas";
    }

    private String mostrarFormulario(Model model, String huespedId, String habitacionId,
                                     String ingreso, String salida, String personas) {
        OpcionesReserva opciones = servicio.cargarOpciones();
        model.addAttribute("huespedes", opciones.huespedes());
        model.addAttribute("habitaciones", opciones.habitaciones());
        model.addAttribute("huespedId", huespedId);
        model.addAttribute("habitacionId", habitacionId);
        model.addAttribute("ingreso", ingreso);
        model.addAttribute("salida", salida);
        model.addAttribute("personas", personas);
        return "reserva-nueva";
    }
}