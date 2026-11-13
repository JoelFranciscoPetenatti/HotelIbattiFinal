package com.programacion2.repositorio;

import com.programacion2.dominio.EstadoReserva;
import com.programacion2.dominio.Habitacion;
import com.programacion2.dominio.Huesped;
import com.programacion2.dominio.Reserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.stereotype.Repository;

@Repository
public class ReservaRepositorio {

    private final PersistenceManagerFactory fabrica;

    public ReservaRepositorio(PersistenceManagerFactory fabrica) {
        this.fabrica = fabrica;
    }

    public void cancelar(int id) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        Transaction transaccion = gestor.currentTransaction();

        try {
            transaccion.begin();

            Reserva reserva = gestor.getObjectById(Reserva.class, id);
            if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
                throw new IllegalStateException(
                    "Solo se pueden cancelar reservas confirmadas."
                );
            }

            reserva.setEstado(EstadoReserva.CANCELADA);
            transaccion.commit();
        } finally {
            try {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
            } finally {
                gestor.close();
            }
        }
    }

    public void eliminar(int id) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        Transaction transaccion = gestor.currentTransaction();

        try {
            transaccion.begin();

            Reserva reserva = gestor.getObjectById(Reserva.class, id);
            gestor.deletePersistent(reserva);

            transaccion.commit();
        } finally {
            try {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
            } finally {
                gestor.close();
            }
        }
    }

    public void guardarOActualizar(int idReserva, int huespedId, int habitacionId,
                                   Function<DatosAlta, Reserva> prepararReserva) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        Transaction transaccion = gestor.currentTransaction();

        try {
            transaccion.begin();

            Reserva existente = gestor.getObjectById(Reserva.class, idReserva);
            if (existente.getEstado() == EstadoReserva.CANCELADA) {
                throw new IllegalStateException("Una reserva cancelada no se puede editar.");
            }

            Huesped huesped = gestor.getObjectById(Huesped.class, huespedId);
            Habitacion habitacion = gestor.getObjectById(Habitacion.class, habitacionId);

            Query<Reserva> consulta = gestor.newQuery(Reserva.class);
            try {
                List<Reserva> otrasReservas = consulta.executeList().stream()
                    .filter(reserva -> reserva.getId() != idReserva
                        && reserva.getHabitacion().getId() == habitacionId)
                    .toList();

                Reserva datos = prepararReserva.apply(
                    new DatosAlta(huesped, habitacion, otrasReservas)
                );
                existente.actualizarDatos(datos);
            } finally {
                consulta.closeAll();
            }

            transaccion.commit();
        } finally {
            try {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
            } finally {
                gestor.close();
            }
        }
    }

    public List<FilaReserva> listar() {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        try {
            Query<Reserva> consulta = gestor.newQuery(Reserva.class);
            try {
                return consulta.executeList().stream()
                    .map(this::crearFila)
                    .toList();
            } finally {
                consulta.closeAll();
            }
        } finally {
            gestor.close();
        }
    }

    public FilaReserva buscar(int id) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        try {
            return crearFila(gestor.getObjectById(Reserva.class, id));
        } finally {
            gestor.close();
        }
    }

    public ReferenciaReserva buscarReferencia(int id) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        try {
            Reserva reserva = gestor.getObjectById(Reserva.class, id);
            return new ReferenciaReserva(
                reserva.getHuesped().getId(),
                reserva.getHabitacion().getId(),
                reserva.getFechaIngreso(),
                reserva.getFechaSalida(),
                reserva.getCantidadPersonas(),
                reserva.getEstado()
            );
        } finally {
            gestor.close();
        }
    }

    public OpcionesReserva cargarOpciones() {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        try {
            Query<Huesped> huespedes = gestor.newQuery(Huesped.class);
            try {
                List<Opcion> opcionesHuespedes = huespedes.executeList().stream()
                    .map(huesped -> new Opcion(huesped.getId(), huesped.getNombre()))
                    .toList();

                Query<Habitacion> habitaciones = gestor.newQuery(Habitacion.class);
                try {
                    List<Opcion> opcionesHabitaciones = habitaciones.executeList().stream()
                        .filter(Habitacion::isActiva)
                        .map(habitacion -> new Opcion(
                            habitacion.getId(),
                            habitacion.getNumero()
                                + " | Hasta " + habitacion.getCapacidad()
                                + " personas | $" + habitacion.getPrecioPorNoche().toPlainString()
                                + " por noche"
                        ))
                        .toList();

                    return new OpcionesReserva(opcionesHuespedes, opcionesHabitaciones);
                } finally {
                    habitaciones.closeAll();
                }
            } finally {
                huespedes.closeAll();
            }
        } finally {
            gestor.close();
        }
    }

    public int guardar(int huespedId, int habitacionId,
                       Function<DatosAlta, Reserva> prepararReserva) {
        PersistenceManager gestor = fabrica.getPersistenceManager();
        Transaction transaccion = gestor.currentTransaction();

        try {
            transaccion.begin();

            Huesped huesped = gestor.getObjectById(Huesped.class, huespedId);
            Habitacion habitacion = gestor.getObjectById(Habitacion.class, habitacionId);

            Query<Reserva> consulta = gestor.newQuery(Reserva.class);
            Reserva nueva;
            try {
                List<Reserva> existentes = consulta.executeList().stream()
                    .filter(reserva -> reserva.getHabitacion().getId() == habitacionId)
                    .toList();

                nueva = prepararReserva.apply(new DatosAlta(huesped, habitacion, existentes));
            } finally {
                consulta.closeAll();
            }

            gestor.makePersistent(nueva);
            gestor.flush();
            int id = nueva.getId();

            transaccion.commit();
            return id;
        } finally {
            try {
                if (transaccion.isActive()) {
                    transaccion.rollback();
                }
            } finally {
                gestor.close();
            }
        }
    }

    private FilaReserva crearFila(Reserva reserva) {
        return new FilaReserva(
            reserva.getId(),
            reserva.getHuesped().getNombre(),
            String.valueOf(reserva.getHabitacion().getNumero()),
            reserva.getHabitacion().getHotel().getNombre(),
            reserva.getFechaIngreso(),
            reserva.getFechaSalida(),
            reserva.getTotal(),
            reserva.getEstado()
        );
    }

    public record FilaReserva(
        int id, String huesped, String habitacion, String hotel,
        LocalDate ingreso, LocalDate salida, BigDecimal total, EstadoReserva estado
    ) {}

    public record ReferenciaReserva(
        int huespedId, int habitacionId, LocalDate ingreso, LocalDate salida,
        int personas, EstadoReserva estado
    ) {}

    public record Opcion(int id, String etiqueta) {}

    public record OpcionesReserva(List<Opcion> huespedes, List<Opcion> habitaciones) {}

    public record DatosAlta(Huesped huesped, Habitacion habitacion, List<Reserva> existentes) {}
}