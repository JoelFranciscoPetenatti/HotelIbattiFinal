package com.programacion2.repositorio;

import com.programacion2.dominio.EstadoReserva;
import com.programacion2.dominio.Habitacion;
import com.programacion2.dominio.Reserva;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.springframework.stereotype.Repository;

@Repository
public class DisponibilidadRepositorio {

    private final PersistenceManagerFactory fabrica;

    public DisponibilidadRepositorio(PersistenceManagerFactory fabrica) {
        this.fabrica = fabrica;
    }

    public Ocupacion consultar(LocalDate noche) {
        PersistenceManager gestor = fabrica.getPersistenceManager();

        try {
            Query<Habitacion> habitaciones = gestor.newQuery(Habitacion.class);
            Set<Integer> activas;

            try {
                activas = habitaciones.executeList().stream()
                    .filter(Habitacion::isActiva)
                    .map(Habitacion::getId)
                    .collect(Collectors.toSet());
            } finally {
                habitaciones.closeAll();
            }

            Query<Reserva> reservas = gestor.newQuery(Reserva.class);
            try {
                Set<Integer> ocupadas = reservas.executeList().stream()
                    .filter(reserva -> reserva.getEstado() == EstadoReserva.CONFIRMADA
                        || reserva.getEstado() == EstadoReserva.EN_CURSO)
                    .filter(reserva -> !reserva.getFechaIngreso().isAfter(noche)
                        && reserva.getFechaSalida().isAfter(noche))
                    .map(reserva -> reserva.getHabitacion().getId())
                    .filter(activas::contains)
                    .collect(Collectors.toSet());

                return new Ocupacion(activas.size(), ocupadas.size());
            } finally {
                reservas.closeAll();
            }
        } finally {
            gestor.close();
        }
    }

    public record Ocupacion(int activas, int ocupadas) {}
}