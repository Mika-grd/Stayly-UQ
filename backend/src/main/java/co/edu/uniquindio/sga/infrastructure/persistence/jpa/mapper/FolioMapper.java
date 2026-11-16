package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.folio.AutorizacionCierre;
import co.edu.uniquindio.sga.domain.model.folio.Cargo;
import co.edu.uniquindio.sga.domain.model.folio.Folio;
import co.edu.uniquindio.sga.domain.model.folio.IdFolio;
import co.edu.uniquindio.sga.domain.model.folio.Pago;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.AutorizacionCierreEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.PagoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.FolioEntity;

/** Traduce entre el agregado Folio y su tabla. Sin lógica de negocio: el saldo se recalcula en el dominio. */
public final class FolioMapper {

    private FolioMapper() {
    }

    public static FolioEntity aEntidad(Folio folio) {
        return new FolioEntity(folio.id().valor(), folio.reserva().valor(),
                folio.cargos().stream()
                        .map(cargo -> new CargoEmbeddable(cargo.tipo(), cargo.concepto(), cargo.valor().valor(),
                                cargo.fecha()))
                        .toList(),
                folio.pagos().stream()
                        .map(pago -> new PagoEmbeddable(pago.valor().valor(), pago.medio(), pago.fecha(),
                                pago.referencia()))
                        .toList(),
                folio.estado(),
                folio.autorizacion()
                        .map(autorizacion -> new AutorizacionCierreEmbeddable(autorizacion.autor(),
                                autorizacion.motivo(), autorizacion.fecha()))
                        .orElse(null));
    }

    public static Folio aDominio(FolioEntity entidad) {
        AutorizacionCierreEmbeddable autorizacion = entidad.getAutorizacion();
        return Folio.reconstituir(new IdFolio(entidad.getId()), new CodigoReserva(entidad.getReserva()),
                entidad.getCargos().stream()
                        .map(cargo -> new Cargo(cargo.getTipo(), cargo.getConcepto(), new Dinero(cargo.getValor()),
                                cargo.getFecha()))
                        .toList(),
                entidad.getPagos().stream()
                        .map(pago -> new Pago(new Dinero(pago.getValor()), pago.getMedio(), pago.getFecha(),
                                pago.getReferencia()))
                        .toList(),
                entidad.getEstado(),
                autorizacion == null || autorizacion.getAutor() == null
                        ? null
                        : new AutorizacionCierre(autorizacion.getAutor(), autorizacion.getMotivo(),
                                autorizacion.getFecha()));
    }
}
