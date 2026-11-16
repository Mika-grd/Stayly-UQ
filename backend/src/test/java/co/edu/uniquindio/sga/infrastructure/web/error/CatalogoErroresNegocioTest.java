package co.edu.uniquindio.sga.infrastructure.web.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/** Hoja "Errores de Negocio": cada código con su HTTP y su regla. */
class CatalogoErroresNegocioTest {

    @Test
    @DisplayName("Un código de la hoja devuelve su HTTP y su regla")
    void clasificarCodigoRegistrado() {
        // Act
        CatalogoErroresNegocio.Clasificacion clasificacion =
                CatalogoErroresNegocio.clasificar("ESTANCIA_MINIMA_NO_CUMPLIDA");

        // Assert
        assertThat(clasificacion.estado()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(clasificacion.regla()).isEqualTo("RP-01");
    }

    @Test
    @DisplayName("Ninguna regla del negocio responde 422: solo 400, 401, 403, 404 y 409")
    void ningunCodigoUsa422() {
        assertThat(java.util.List.of("CAPACIDAD_EXCEDIDA", "FECHA_ENTRADA_PASADA", "ESTANCIA_SIN_NOCHES",
                "TITULAR_NO_FACTURABLE", "MASCOTAS_NO_ADMITIDAS", "CUPO_MASCOTAS_EXCEDIDO", "HORA_LLEGADA_REQUERIDA",
                "IMAGENES_INVALIDAS", "TRAMOS_INSUFICIENTES", "PARAMETRO_INVALIDO"))
                .allSatisfy(codigo -> assertThat(CatalogoErroresNegocio.clasificar(codigo).estado())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("Todo *_NO_ENCONTRADO / *_NO_ENCONTRADA es 404 sin regla")
    void clasificarNoEncontradoPorSufijo() {
        assertThat(CatalogoErroresNegocio.clasificar("TEMPORADA_NO_ENCONTRADA").estado())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(CatalogoErroresNegocio.clasificar("APARTAMENTO_NO_ENCONTRADO").regla()).isNull();
    }

    @Test
    @DisplayName("Un código que no está en la hoja se trata como conflicto (409), nunca como 500")
    void clasificarCodigoDesconocido() {
        // Act
        CatalogoErroresNegocio.Clasificacion clasificacion = CatalogoErroresNegocio.clasificar("CODIGO_NUEVO");

        // Assert
        assertThat(clasificacion.estado()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(clasificacion.regla()).isNull();
        assertThat(CatalogoErroresNegocio.contiene("CODIGO_NUEVO")).isFalse();
    }
}
