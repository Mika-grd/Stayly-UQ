package co.edu.uniquindio.sga.infrastructure.persistence.jpa.repository;

import co.edu.uniquindio.sga.infrastructure.persistence.jpa.entity.CodigoRecuperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Códigos de recuperación de contraseña (7.2). Como los usuarios, no tiene puerto en el dominio. */
public interface CodigoRecuperacionJpaRepository extends JpaRepository<CodigoRecuperacionEntity, UUID> {

    List<CodigoRecuperacionEntity> findByUsuarioIdAndUsadoFalse(UUID usuarioId);
}
