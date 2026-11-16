package co.edu.uniquindio.sga.application.usecase;

import co.edu.uniquindio.sga.domain.model.apartamento.Apartamento;
import co.edu.uniquindio.sga.domain.model.apartamento.IdentificacionApartamento;
import co.edu.uniquindio.sga.domain.model.compartido.ComposicionGrupo;
import co.edu.uniquindio.sga.domain.model.compartido.Dinero;
import co.edu.uniquindio.sga.domain.model.compartido.Estancia;
import co.edu.uniquindio.sga.domain.model.reserva.Cotizacion;
import co.edu.uniquindio.sga.domain.model.reserva.MascotasAutorizadas;
import co.edu.uniquindio.sga.domain.model.reserva.ValorCongelado;
import co.edu.uniquindio.sga.domain.repository.ApartamentoRepository;
import co.edu.uniquindio.sga.domain.repository.ConfiguracionAlojamiento;
import co.edu.uniquindio.sga.domain.service.AdmisionMascotasService;
import co.edu.uniquindio.sga.domain.service.CotizadorEstanciaService;
import co.edu.uniquindio.sga.domain.service.VerificadorTarifasCompletasService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * CU-01: el valor de una estancia antes de reservar, con el desglose noche por noche (RN-05,
 * RN-06, 3.4). CotizadorEstanciaService liquida cada noche con la tarifa de su temporada y
 * AdmisionMascotasService el cargo por mascotas (RP-02). No crea nada.
 */
@Service
@Transactional(readOnly = true)
public class CotizarEstanciaUseCase {

    private final ApartamentoRepository apartamentos;
    private final VerificadorTarifasCompletasService verificadorTarifas;
    private final CotizadorEstanciaService cotizador;
    private final AdmisionMascotasService admisionMascotas;
    private final ConfiguracionAlojamiento configuracion;

    public CotizarEstanciaUseCase(ApartamentoRepository apartamentos,
                                  VerificadorTarifasCompletasService verificadorTarifas,
                                  CotizadorEstanciaService cotizador, AdmisionMascotasService admisionMascotas,
                                  ConfiguracionAlojamiento configuracion) {
        this.apartamentos = apartamentos;
        this.verificadorTarifas = verificadorTarifas;
        this.cotizador = cotizador;
        this.admisionMascotas = admisionMascotas;
        this.configuracion = configuracion;
    }

    public Cotizacion ejecutar(IdentificacionApartamento identificacion, LocalDate entrada, LocalDate salida,
                               List<LocalDate> fechasNacimiento, MascotasAutorizadas mascotas) {
        Estancia estancia = new Estancia(entrada, salida);
        ComposicionGrupo grupo = new ComposicionGrupo(fechasNacimiento);
        Apartamento apartamento = Consultas.apartamento(apartamentos, identificacion);
        verificadorTarifas.verificarTarifasCompletas(identificacion);
        admisionMascotas.verificarMascotas(apartamento, mascotas);
        ValorCongelado valor = cotizador.cotizar(identificacion, estancia, grupo);
        Dinero cargoMascotas = mascotas.hayMascotas()
                ? admisionMascotas.cargoPorMascotas(mascotas, estancia) : Dinero.cero();
        return new Cotizacion(identificacion, estancia, valor, grupo.total(),
                grupo.facturablesEn(entrada, configuracion.umbralEdad()), cargoMascotas);
    }
}
