package co.edu.uniquindio.sga.infrastructure.web;

import co.edu.uniquindio.sga.application.dto.response.ApartamentoInventarioResponse;
import co.edu.uniquindio.sga.application.dto.response.PaginaApartamentoInventarioResponse;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentosUseCase;
import co.edu.uniquindio.sga.application.usecase.ConsultarApartamentosUseCase.ApartamentoInventario;
import co.edu.uniquindio.sga.domain.model.compartido.Pagina;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller delgado del inventario de apartamentos (7.3): solo el administrador. */
@RestController
public class InventarioApartamentoController {

    private final ConsultarApartamentosUseCase consultar;

    public InventarioApartamentoController(ConsultarApartamentosUseCase consultar) {
        this.consultar = consultar;
    }

    @GetMapping("/api/apartamentos")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public PaginaApartamentoInventarioResponse listar(@RequestParam(defaultValue = "0") int pagina) {
        Pagina<ApartamentoInventario> resultado = consultar.ejecutar(new PeticionPagina(pagina));
        return new PaginaApartamentoInventarioResponse(resultado.contenido().stream().map(fila -> {
            var apartamento = fila.apartamento();
            return new ApartamentoInventarioResponse(apartamento.identificacion().valor(), apartamento.nombre(),
                    apartamento.dormitorios(), apartamento.capacidad().personas(), apartamento.estaActivo(),
                    apartamento.estadoOperativo(), fila.tarifasCompletas());
        }).toList(), resultado.numero(), resultado.tamano(), resultado.totalElementos(), resultado.totalPaginas());
    }
}
