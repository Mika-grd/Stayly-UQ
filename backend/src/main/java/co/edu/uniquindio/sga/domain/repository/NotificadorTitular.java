package co.edu.uniquindio.sga.domain.repository;

import co.edu.uniquindio.sga.domain.model.reserva.Reserva;

/**
 * Puerto de salida para avisar al titular (7.5, 7.8, 10.3.2 opción A). El dominio y los casos de uso
 * no conocen el proveedor (I-01): un adaptador decide si escribe al registro, envía un correo o nada.
 * Avisar nunca puede impedir reservar, confirmar ni cancelar (I-05): el adaptador absorbe sus fallos.
 */
public interface NotificadorTitular {

    /** Sin avisos: para pruebas y para apagar la integración por configuración. */
    NotificadorTitular NINGUNO = (reserva, motivo) -> { };

    void notificar(Reserva reserva, Motivo motivo);

    /** Momento del ciclo de vida que se le avisa al titular. */
    enum Motivo {
        CREADA,
        CONFIRMADA,
        CANCELADA
    }
}
