package co.edu.uniquindio.sga.infrastructure.security;

import co.edu.uniquindio.sga.application.dto.request.UsuarioAdminRequest;
import co.edu.uniquindio.sga.application.dto.response.PaginaUsuarioResponse;
import co.edu.uniquindio.sga.application.dto.response.UsuarioResponse;
import co.edu.uniquindio.sga.application.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.sga.application.exception.ReglaAccesoException;
import co.edu.uniquindio.sga.application.security.GestionUsuariosService;
import co.edu.uniquindio.sga.domain.model.compartido.PeticionPagina;
import co.edu.uniquindio.sga.domain.model.reserva.EstadoReserva;
import co.edu.uniquindio.sga.domain.model.reserva.FiltroReservas;
import co.edu.uniquindio.sga.domain.repository.ReservaRepository;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.UsuarioEntity;
import co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository.UsuarioJpaDataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CU-38: gestión de usuarios y roles por el administrador (7.2). Contraseñas siempre en BCrypt,
 * correo y documento únicos, el correo no se edita y la eliminación es lógica.
 */
@Service
@Transactional
public class GestionUsuariosServiceImpl implements GestionUsuariosService {

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);

    private final UsuarioJpaDataRepository usuarios;
    private final ReservaRepository reservas;
    private final PasswordEncoder codificador;

    public GestionUsuariosServiceImpl(UsuarioJpaDataRepository usuarios, ReservaRepository reservas,
                                      PasswordEncoder codificador) {
        this.usuarios = usuarios;
        this.reservas = reservas;
        this.codificador = codificador;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaUsuarioResponse listar(int pagina) {
        Page<UsuarioEntity> resultado = usuarios.findAll(
                PageRequest.of(Math.max(pagina, 0), PeticionPagina.TAMANO, Sort.by("nombre", "correo")));
        return new PaginaUsuarioResponse(resultado.getContent().stream().map(UsuarioRespuestas::de).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(), resultado.getTotalPages());
    }

    @Override
    public UsuarioResponse crear(UsuarioAdminRequest request) {
        if (request.contrasena() == null) {
            throw new ReglaAccesoException("FORMATO_INVALIDO", "El campo contrasena es obligatorio al crear.");
        }
        String correo = request.correo().trim().toLowerCase();
        String documento = request.documentoIdentidad().trim();
        if (usuarios.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaAccesoException("CORREO_YA_REGISTRADO", "Ya existe un usuario con ese correo.");
        }
        if (usuarios.existsByDocumento(documento)) {
            throw new ReglaAccesoException("DOCUMENTO_YA_REGISTRADO", "Ya existe un usuario con ese documento.");
        }
        UsuarioEntity usuario = usuarios.save(new UsuarioEntity(request.nombre().trim(), correo,
                codificador.encode(request.contrasena()), request.telefono().trim(), documento,
                request.fechaNacimiento(), RolUsuario.valueOf(request.rol().name())));
        return UsuarioRespuestas.de(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse consultar(UUID id) {
        return UsuarioRespuestas.de(buscar(id));
    }

    @Override
    public UsuarioResponse actualizar(UUID id, UsuarioAdminRequest request) {
        UsuarioEntity usuario = buscar(id);
        if (!usuario.getCorreo().equalsIgnoreCase(request.correo().trim())) {
            throw new ReglaAccesoException("CORREO_NO_EDITABLE", "El correo de un usuario no se puede cambiar.");
        }
        String documento = request.documentoIdentidad().trim();
        if (!documento.equals(usuario.getDocumento()) && usuarios.existsByDocumento(documento)) {
            throw new ReglaAccesoException("DOCUMENTO_YA_REGISTRADO", "Ya existe un usuario con ese documento.");
        }
        usuario.actualizarDatos(request.nombre().trim(), request.telefono().trim(), documento,
                request.fechaNacimiento(), RolUsuario.valueOf(request.rol().name()));
        return UsuarioRespuestas.de(usuario);
    }

    @Override
    public void eliminar(UUID id, String correoAdministrador) {
        UsuarioEntity usuario = buscar(id);
        if (usuario.getCorreo().equalsIgnoreCase(correoAdministrador)) {
            throw new ReglaAccesoException("AUTOELIMINACION_NO_PERMITIDA",
                    "Un administrador no puede eliminar su propio usuario.");
        }
        boolean titularDeReservasActivas = ESTADOS_ACTIVOS.stream().anyMatch(estado -> reservas.buscar(
                new FiltroReservas(estado, null, null, usuario.getDocumento(), null, null),
                PeticionPagina.primera()).totalElementos() > 0);
        if (titularDeReservasActivas) {
            throw new ReglaAccesoException("USUARIO_CON_RESERVAS_ACTIVAS",
                    "No se puede eliminar un usuario titular de reservas pendientes, confirmadas o en curso.");
        }
        usuario.setActivo(false);
    }

    private UsuarioEntity buscar(UUID id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("USUARIO_NO_ENCONTRADO",
                "No existe el usuario " + id + "."));
    }
}
