package co.edu.uniquindio.sga.infrastructure.web.mapper;

import co.edu.uniquindio.sga.application.dto.ImagenDTO;
import co.edu.uniquindio.sga.application.dto.PeriodoDTO;
import co.edu.uniquindio.sga.application.dto.TramoPoliticaDTO;
import co.edu.uniquindio.sga.application.dto.response.CargoNocheDTO;
import co.edu.uniquindio.sga.domain.model.apartamento.Capacidad;
import co.edu.uniquindio.sga.domain.model.apartamento.Caracteristica;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.ImagenApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.RangoFechas;
import co.edu.uniquindio.sga.domain.model.politica.TramoPolitica;
import co.edu.uniquindio.sga.domain.model.politica.VersionPolitica;
import co.edu.uniquindio.sga.domain.model.reserva.CargoNoche;
import co.edu.uniquindio.sga.domain.model.reserva.CodigoReserva;
import co.edu.uniquindio.sga.domain.model.temporada.IdTemporada;

import java.util.UUID;

/**
 * Conversiones de los objetos de valor del dominio a las formas simples que usan los DTOs REST
 * (dinero como long, identificadores como String/UUID). Sin reglas de negocio: solo cambia la
 * representación. La usan los mappers MapStruct de {@code infrastructure.web.mapper} vía
 * {@code uses = DominioWebMapperSoporte.class}.
 */
public final class DominioWebMapperSoporte {

    private DominioWebMapperSoporte() {
    }

    public static long dineroALong(Dinero dinero) {
        return dinero.valor().longValueExact();
    }

    public static Dinero dineroDesdeLong(long valor) {
        return Dinero.pesos(valor);
    }

    public static String identificacionATexto(IdentificacionApartamento identificacion) {
        return identificacion.valor();
    }

    public static IdentificacionApartamento identificacionDesdeTexto(String texto) {
        return new IdentificacionApartamento(texto);
    }

    public static String codigoATexto(CodigoReserva codigo) {
        return codigo.valor();
    }

    public static UUID idTemporadaAUuid(IdTemporada id) {
        return id.valor();
    }

    public static IdTemporada idTemporadaDesdeUuid(UUID uuid) {
        return new IdTemporada(uuid);
    }

    public static int versionPoliticaAInt(VersionPolitica version) {
        return version.numero();
    }

    public static Capacidad capacidadDesdeEntero(int personas) {
        return new Capacidad(personas);
    }

    public static int capacidadAEntero(Capacidad capacidad) {
        return capacidad.personas();
    }

    public static String caracteristicaATexto(Caracteristica caracteristica) {
        return caracteristica.nombre();
    }

    public static Caracteristica caracteristicaDesdeTexto(String texto) {
        return new Caracteristica(texto);
    }

    public static ImagenDTO imagenADto(ImagenApartamento imagen) {
        return new ImagenDTO(imagen.url(), imagen.principal());
    }

    public static ImagenApartamento imagenDesdeDto(ImagenDTO dto) {
        return new ImagenApartamento(dto.url(), dto.principal());
    }

    public static PeriodoDTO periodoADto(RangoFechas rango) {
        return new PeriodoDTO(rango.inicio(), rango.fin());
    }

    public static RangoFechas periodoDesdeDto(PeriodoDTO dto) {
        return new RangoFechas(dto.fechaInicio(), dto.fechaFin());
    }

    public static TramoPoliticaDTO tramoADto(TramoPolitica tramo) {
        return new TramoPoliticaDTO(tramo.antelacionMinimaDias(), tramo.porcentajeRetencion());
    }

    public static TramoPolitica tramoDesdeDto(TramoPoliticaDTO dto) {
        return new TramoPolitica(dto.antelacionMinimaDias(), dto.porcentajeRetencion());
    }

    public static CargoNocheDTO cargoNocheADto(CargoNoche cargo) {
        return new CargoNocheDTO(cargo.fecha(), cargo.temporada(), dineroALong(cargo.tarifa()),
                cargo.ocupantesFacturables(), dineroALong(cargo.subtotal()));
    }
}
