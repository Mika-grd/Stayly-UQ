package co.edu.uniquindio.sga.application.dto.request;

import jakarta.validation.constraints.Size;

/** Datos para cerrar el folio (schema CerrarFolioRequest). El motivo solo cuenta si hay saldo y cierra el administrador (RN-17). */
public record CerrarFolioRequest(@Size(max = 500) String motivoAutorizacion) {
}
