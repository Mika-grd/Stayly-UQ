package co.edu.uniquindio.sga.infrastructure.persistence.jpa.mapper;

import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.canal.IdCanal;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CargoNoche;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.DatosContactoTitular;
import co.edu.uniquindio.sga.domain.model.reserva.DocumentoIdentidad;
import co.edu.uniquindio.sga.domain.model.reserva.EventoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.HoraEstimadaLlegada;
import co.edu.uniquindio.sga.domain.model.reserva.IdOcupante;
import co.edu.uniquindio.sga.domain.model.reserva.IdentificadorExterno;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.Ocupante;
import co.edu.uniquindio.sga.domain.model.reserva.Reserva;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.CargoNocheEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.DatosContactoTitularEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EstanciaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.EventoReservaEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.embeddable.IdentificadorExternoEmbeddable;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.OcupanteEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.ReservaEntity;

/** Traduce entre el agregado Reserva (con sus ocupantes) y su tabla. Sin lógica de negocio. */
public final class ReservaMapper {

    private ReservaMapper() {
    }

    public static ReservaEntity aEntidad(Reserva reserva) {
        return new ReservaEntity(reserva.codigo().valor(), reserva.apartamento().valor(),
                new EstanciaEmbeddable(reserva.estancia().entrada(), reserva.estancia().salida()),
                reserva.ocupantes().stream().map(ReservaMapper::aEntidad).toList(),
                reserva.estado(), reserva.canal(),
                reserva.identificadorExterno()
                        .map(externo -> new IdentificadorExternoEmbeddable(externo.canal().valor(), externo.codigo()))
                        .orElse(null),
                reserva.horaEstimadaLlegada().map(HoraEstimadaLlegada::hora).orElse(null),
                reserva.mascotas().cantidad(),
                reserva.valor().noches().stream()
                        .map(noche -> new CargoNocheEmbeddable(noche.fecha(), noche.temporada(), noche.tarifa().valor(),
                                noche.ocupantesFacturables()))
                        .toList(),
                reserva.versionPolitica().numero(), reserva.fechaCreacion(),
                reserva.eventos().stream()
                        .map(evento -> new EventoReservaEmbeddable(evento.descripcion(), evento.autor(), evento.fecha()))
                        .toList());
    }

    public static Reserva aDominio(ReservaEntity entidad) {
        IdentificadorExternoEmbeddable externo = entidad.getIdentificadorExterno();
        return Reserva.reconstituir(new CodigoReserva(entidad.getCodigo()),
                new IdentificacionApartamento(entidad.getApartamento()),
                new Estancia(entidad.getEstancia().getEntrada(), entidad.getEstancia().getSalida()),
                entidad.getOcupantes().stream().map(ReservaMapper::aDominio).toList(),
                entidad.getEstado(), entidad.getCanal(),
                externo == null || externo.getCanal() == null
                        ? null : new IdentificadorExterno(new IdCanal(externo.getCanal()), externo.getCodigo()),
                entidad.getHoraEstimadaLlegada() == null
                        ? null : new HoraEstimadaLlegada(entidad.getHoraEstimadaLlegada()),
                new MascotasAutorizadas(entidad.getMascotas()),
                new ValorCongelado(entidad.getValor().stream()
                        .map(noche -> new CargoNoche(noche.getFecha(), noche.getTemporada(),
                                new Dinero(noche.getTarifa()), noche.getOcupantesFacturables()))
                        .toList()),
                new VersionPolitica(entidad.getVersionPolitica()), entidad.getFechaCreacion(),
                entidad.getEventos().stream()
                        .map(evento -> new EventoReserva(evento.getDescripcion(), evento.getAutor(), evento.getFecha()))
                        .toList());
    }

    private static OcupanteEntity aEntidad(Ocupante ocupante) {
        return new OcupanteEntity(ocupante.id().valor(), ocupante.nombre(), ocupante.fechaNacimiento(),
                ocupante.documento().map(DocumentoIdentidad::numero).orElse(null), ocupante.esTitular(),
                ocupante.contacto()
                        .map(contacto -> new DatosContactoTitularEmbeddable(contacto.correo(), contacto.telefono()))
                        .orElse(null));
    }

    private static Ocupante aDominio(OcupanteEntity entidad) {
        IdOcupante id = new IdOcupante(entidad.getId());
        DocumentoIdentidad documento = entidad.getDocumento() == null
                ? null : new DocumentoIdentidad(entidad.getDocumento());
        if (entidad.isTitular()) {
            DatosContactoTitularEmbeddable contacto = entidad.getContacto();
            return Ocupante.titular(id, entidad.getNombre(), entidad.getFechaNacimiento(), documento,
                    contacto == null ? null : new DatosContactoTitular(contacto.getCorreo(), contacto.getTelefono()));
        }
        return Ocupante.acompanante(id, entidad.getNombre(), entidad.getFechaNacimiento(), documento);
    }
}
