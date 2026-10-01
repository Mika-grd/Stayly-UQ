# Mapa de agregados — Vista de servicios

Una vista por servicio de dominio. Cada vista muestra **qué agregados coordina** (flecha sólida) y **de qué repositorios o puertos depende** (flecha punteada). Coincide exactamente con la hoja *Catálogo de Servicios*.

- Hexágono azul = servicio de dominio · Óvalo verde = agregado · Cilindro amarillo = repositorio · Paralelogramo morado = puerto del dominio.
- Los servicios sin repositorios reciben los agregados ya cargados por el caso de uso: solo coordinan, no consultan conjuntos.

## Resumen: servicio × agregado

| Servicio | Reserva | Apartamento | Folio | Bloqueo | Temporada | Tarifario | PoliticaCancelacion | ConflictoCanal |
|---|---|---|---|---|---|---|---|---|
| DisponibilidadApartamentoService | ● | ● |  | ● |  |  |  |  |
| CotizadorEstanciaService |  |  |  |  | ● | ● |  |  |
| ConfirmacionReservaService | ● |  | ● |  |  |  |  |  |
| EntregaApartamentoService | ● | ● |  |  |  |  |  |  |
| SalidaGrupoService | ● | ● | ● |  |  |  |  |  |
| CalculadorRetencionService | ● |  |  |  |  |  | ● |  |
| ConciliacionCanalExternoService | ● | ● |  |  |  |  |  | ● |
| RegistroBloqueoService | ● |  |  | ● |  |  |  |  |
| CalendarioTemporadasService |  |  |  |  | ● |  |  |  |
| VerificadorTarifasCompletasService |  | ● |  |  | ● | ● |  |  |
| RetiroApartamentoService | ● | ● |  |  |  |  |  |  |
| VerificadorEstanciaMinimaService |  |  |  |  | ● |  |  |  |
| AdmisionMascotasService |  | ● |  |  |  |  |  |  |

## A. Ciclo de vida de la reserva

### DisponibilidadApartamentoService

- **Reglas que resuelve:** RN-01, RN-02, RN-07, RN-12, RN-20, 3.3
- **Firma:** `void verificarDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes, CodigoReserva reservaExcluida)` · `boolean estaDisponible(Apartamento apartamento, Estancia estancia, int totalOcupantes)`
- **Por qué no vive en un solo agregado:** Exige comparar contra TODAS las reservas activas y los bloqueos del apartamento, y leer capacidad y estado de activo del Apartamento (otro agregado). Ninguna reserva conoce a las demás. El tiempo de preparación y el horario se piden a ConfiguracionAlojamiento. reservaExcluida permite revalidar una modificación sin chocar consigo misma (null al crear).
- **Caso de uso que lo invoca:** CU-02, CU-08 (vía conciliación), CU-13, CU-20

```mermaid
flowchart TB
  S{{"DisponibilidadApartamentoService<br/><i>RN-01, RN-02, RN-07, RN-12, RN-20, 3.3</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Apartamento(["Apartamento"])
    A_Reserva(["Reserva"])
    A_Bloqueo(["Bloqueo"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ReservaRepository[("ReservaRepository")]
    D_BloqueoRepository[("BloqueoRepository")]
    D_ConfiguracionAlojamiento[/"ConfiguracionAlojamiento «puerto»"/]
  end
  S -- "coordina" --> A_Apartamento
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Bloqueo
  S -. "depende" .-> D_ReservaRepository
  S -. "depende" .-> D_BloqueoRepository
  S -. "depende" .-> D_ConfiguracionAlojamiento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Apartamento,A_Reserva,A_Bloqueo agr
  class D_ReservaRepository,D_BloqueoRepository repo
  class D_ConfiguracionAlojamiento puerto
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### CotizadorEstanciaService

- **Reglas que resuelve:** RN-05, RN-06, 3.4, RP-02 (cargo por mascota)
- **Firma:** `ValorCongelado cotizar(IdentificacionApartamento apartamento, Estancia estancia, ComposicionGrupo grupo)`
- **Por qué no vive en un solo agregado:** El cálculo noche por noche necesita el calendario de temporadas (agregado Temporada) y las tarifas del apartamento (agregado Tarifario); la Reserva solo recibe el valor ya congelado. El umbral de edad se pide a ConfiguracionAlojamiento. El redondeo se aplica al final de cada cargo.
- **Caso de uso que lo invoca:** CU-01, CU-02, CU-08, CU-13, CU-20

```mermaid
flowchart TB
  S{{"CotizadorEstanciaService<br/><i>RN-05, RN-06, 3.4, RP-02 (cargo por mascota)</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Temporada(["Temporada"])
    A_Tarifario(["Tarifario"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_TemporadaRepository[("TemporadaRepository")]
    D_TarifarioRepository[("TarifarioRepository")]
    D_ConfiguracionAlojamiento[/"ConfiguracionAlojamiento «puerto»"/]
  end
  S -- "coordina" --> A_Temporada
  S -- "coordina" --> A_Tarifario
  S -. "depende" .-> D_TemporadaRepository
  S -. "depende" .-> D_TarifarioRepository
  S -. "depende" .-> D_ConfiguracionAlojamiento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Temporada,A_Tarifario agr
  class D_TemporadaRepository,D_TarifarioRepository repo
  class D_ConfiguracionAlojamiento puerto
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### ConfirmacionReservaService

- **Reglas que resuelve:** L-11 (coordina RN-08 y RN-09 de Reserva)
- **Firma:** `void confirmar(Reserva reserva, Folio folio, String autor, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** El anticipo pagado está en el Folio (otro agregado) y la Reserva no puede ver sus pagos; el porcentaje exigido es configuración. El servicio verifica folio.totalPagos() ≥ anticipo.exigidoSobre(reserva.valor().total()) y luego llama reserva.confirmar(), que protege RN-08 y RN-09.
- **Caso de uso que lo invoca:** CU-03

```mermaid
flowchart TB
  S{{"ConfirmacionReservaService<br/><i>L-11 (coordina RN-08 y RN-09 de Reserva)</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_Folio(["Folio"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ConfiguracionAlojamiento[/"ConfiguracionAlojamiento «puerto»"/]
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Folio
  S -. "depende" .-> D_ConfiguracionAlojamiento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Reserva,A_Folio agr
  class D_ConfiguracionAlojamiento puerto
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### EntregaApartamentoService

- **Reglas que resuelve:** RN-11 (coordina RN-10 de Reserva)
- **Firma:** `void registrarLlegada(Reserva reserva, Apartamento apartamento, String autor, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** Registrar la llegada cambia dos agregados a la vez: la Reserva no puede leer el estado operativo del Apartamento ni el Apartamento conoce la reserva. El servicio verifica que la reserva sea de ese apartamento, que reserva.puedeRegistrarLlegada(hoy) (RN-10) y apartamento.puedeRecibirGrupo() (RN-11) ANTES de cambiar cualquiera; solo entonces llama reserva.registrarLlegada() y apartamento.marcarOcupado(). Así nunca queda uno cambiado y el otro no.
- **Caso de uso que lo invoca:** CU-05

```mermaid
flowchart TB
  S{{"EntregaApartamentoService<br/><i>RN-11 (coordina RN-10 de Reserva)</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_Apartamento(["Apartamento"])
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Apartamento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Reserva,A_Apartamento agr
  style CO fill:#fafffb,stroke:#1f6f43
```

### SalidaGrupoService

- **Reglas que resuelve:** 7.6 (coordina RN-17 de Folio)
- **Firma:** `void registrarSalida(Reserva reserva, Folio folio, Apartamento apartamento, String autor, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** El folio cerrado es requisito de la salida, pero Folio es otro agregado (y cerrarlo con saldo exige autorización, RN-17, que vive en Folio). El servicio verifica folio.estaCerrado() y que los tres correspondan entre sí; luego coordina reserva.registrarSalida() y apartamento.liberar() sin fusionar fronteras.
- **Caso de uso que lo invoca:** CU-06

```mermaid
flowchart TB
  S{{"SalidaGrupoService<br/><i>7.6 (coordina RN-17 de Folio)</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_Folio(["Folio"])
    A_Apartamento(["Apartamento"])
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Folio
  S -- "coordina" --> A_Apartamento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Reserva,A_Folio,A_Apartamento agr
  style CO fill:#fafffb,stroke:#1f6f43
```

### CalculadorRetencionService

- **Reglas que resuelve:** RN-13, 7.5
- **Firma:** `Dinero calcularRetencion(Reserva reserva, LocalDateTime momentoCancelacion)` · `Dinero calcularPenalidadNoShow(Reserva reserva)`
- **Por qué no vive en un solo agregado:** La reserva solo guarda su VersionPolitica; los tramos viven en el agregado PoliticaCancelacion (versión histórica). El servicio cruza la antelación de la cancelación con la versión congelada. El caso de uso entrega la retención a Folio.liquidarCancelacion.
- **Caso de uso que lo invoca:** CU-04, CU-12

```mermaid
flowchart TB
  S{{"CalculadorRetencionService<br/><i>RN-13, 7.5</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_PoliticaCancelacion(["PoliticaCancelacion"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_PoliticaCancelacionRepository[("PoliticaCancelacionRepository")]
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_PoliticaCancelacion
  S -. "depende" .-> D_PoliticaCancelacionRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Reserva,A_PoliticaCancelacion agr
  class D_PoliticaCancelacionRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

## B. Canal externo y administración

### ConciliacionCanalExternoService

- **Reglas que resuelve:** RN-18, RN-19
- **Firma:** `ResultadoConciliacion conciliar(IdentificadorExterno identificador, Apartamento apartamento, Estancia estancia, int totalOcupantes, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** Detectar un mensaje repetido exige buscar en el conjunto de reservas por canal + identificador externo, y detectar un conflicto exige la disponibilidad de todo el apartamento; ninguna reserva individual puede garantizarlo. Devuelve ACEPTAR, YA_EXISTE (con la reserva existente) o CONFLICTO (con el ConflictoCanal nuevo); nunca toca la reserva vigente.
- **Caso de uso que lo invoca:** CU-08

```mermaid
flowchart TB
  S{{"ConciliacionCanalExternoService<br/><i>RN-18, RN-19</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_Apartamento(["Apartamento"])
    A_ConflictoCanal(["ConflictoCanal"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ReservaRepository[("ReservaRepository")]
    D_DisponibilidadApartamentoService{{"DisponibilidadApartamentoService"}}
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Apartamento
  S -- "coordina" --> A_ConflictoCanal
  S -. "depende" .-> D_ReservaRepository
  S -. "depende" .-> D_DisponibilidadApartamentoService
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Reserva,A_Apartamento,A_ConflictoCanal agr
  class D_ReservaRepository repo
  class D_DisponibilidadApartamentoService srv
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### RegistroBloqueoService

- **Reglas que resuelve:** 7.3, RN-07
- **Firma:** `Bloqueo registrar(IdentificacionApartamento apartamento, RangoFechas rango, String motivo, String autor, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** No puede registrarse un bloqueo sobre noches con reservas activas: requiere consultar las reservas del apartamento, que son otro agregado.
- **Caso de uso que lo invoca:** CU-16

```mermaid
flowchart TB
  S{{"RegistroBloqueoService<br/><i>7.3, RN-07</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Bloqueo(["Bloqueo"])
    A_Reserva(["Reserva"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ReservaRepository[("ReservaRepository")]
  end
  S -- "coordina" --> A_Bloqueo
  S -- "coordina" --> A_Reserva
  S -. "depende" .-> D_ReservaRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Bloqueo,A_Reserva agr
  class D_ReservaRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### CalendarioTemporadasService

- **Reglas que resuelve:** 7.4, F-05
- **Firma:** `void verificarCalendario(Temporada nueva)`
- **Por qué no vive en un solo agregado:** Que las temporadas no se solapen y que exista exactamente una base depende de todas las temporadas, no de una sola.
- **Caso de uso que lo invoca:** Gestionar temporadas (POST/PUT /api/temporadas)

```mermaid
flowchart TB
  S{{"CalendarioTemporadasService<br/><i>7.4, F-05</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Temporada(["Temporada"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_TemporadaRepository[("TemporadaRepository")]
  end
  S -- "coordina" --> A_Temporada
  S -. "depende" .-> D_TemporadaRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Temporada agr
  class D_TemporadaRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### VerificadorTarifasCompletasService

- **Reglas que resuelve:** 7.4
- **Firma:** `void verificarTarifasCompletas(IdentificacionApartamento apartamento)`
- **Por qué no vive en un solo agregado:** Activar un apartamento o reservarlo exige tarifa en TODAS las temporadas: cruza el agregado Temporada con el Tarifario del apartamento.
- **Caso de uso que lo invoca:** CU-02, CU-08, CU-13, Activar apartamento

```mermaid
flowchart TB
  S{{"VerificadorTarifasCompletasService<br/><i>7.4</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Apartamento(["Apartamento"])
    A_Temporada(["Temporada"])
    A_Tarifario(["Tarifario"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_TemporadaRepository[("TemporadaRepository")]
    D_TarifarioRepository[("TarifarioRepository")]
  end
  S -- "coordina" --> A_Apartamento
  S -- "coordina" --> A_Temporada
  S -- "coordina" --> A_Tarifario
  S -. "depende" .-> D_TemporadaRepository
  S -. "depende" .-> D_TarifarioRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Apartamento,A_Temporada,A_Tarifario agr
  class D_TemporadaRepository,D_TarifarioRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### RetiroApartamentoService

- **Reglas que resuelve:** 7.3
- **Firma:** `void retirarDeLaVenta(Apartamento apartamento, LocalDate hoy)`
- **Por qué no vive en un solo agregado:** Solo se retira si no tiene reservas activas ni futuras: requiere consultar reservas, que son otro agregado.
- **Caso de uso que lo invoca:** Retirar apartamento (DELETE /api/apartamentos/{identificacion})

```mermaid
flowchart TB
  S{{"RetiroApartamentoService<br/><i>7.3</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Apartamento(["Apartamento"])
    A_Reserva(["Reserva"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ReservaRepository[("ReservaRepository")]
  end
  S -- "coordina" --> A_Apartamento
  S -- "coordina" --> A_Reserva
  S -. "depende" .-> D_ReservaRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Apartamento,A_Reserva agr
  class D_ReservaRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

## C. Reglas propias del grupo

### VerificadorEstanciaMinimaService

- **Reglas que resuelve:** RP-01
- **Firma:** `void verificarEstanciaMinima(Estancia estancia)`
- **Por qué no vive en un solo agregado:** La estancia mínima depende de la temporada de la noche de entrada (otro agregado): no cabe en Estancia ni en Reserva.
- **Caso de uso que lo invoca:** CU-02, CU-08, CU-13

```mermaid
flowchart TB
  S{{"VerificadorEstanciaMinimaService<br/><i>RP-01</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Temporada(["Temporada"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_TemporadaRepository[("TemporadaRepository")]
  end
  S -- "coordina" --> A_Temporada
  S -. "depende" .-> D_TemporadaRepository
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Temporada agr
  class D_TemporadaRepository repo
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### AdmisionMascotasService

- **Reglas que resuelve:** RP-02
- **Firma:** `void verificarMascotas(Apartamento apartamento, MascotasAutorizadas mascotas)` · `Dinero cargoPorMascotas(MascotasAutorizadas mascotas, Estancia estancia)`
- **Por qué no vive en un solo agregado:** Depende de una característica del Apartamento (otro agregado) y del cupo configurable del alojamiento; la Reserva no conoce ninguno de los dos.
- **Caso de uso que lo invoca:** CU-02, CU-13

```mermaid
flowchart TB
  S{{"AdmisionMascotasService<br/><i>RP-02</i>"}}
  subgraph CO["Coordina"]
    direction LR
    A_Apartamento(["Apartamento"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ConfiguracionAlojamiento[/"ConfiguracionAlojamiento «puerto»"/]
  end
  S -- "coordina" --> A_Apartamento
  S -. "depende" .-> D_ConfiguracionAlojamiento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S srv
  class A_Apartamento agr
  class D_ConfiguracionAlojamiento puerto
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```

### RecargoLlegadaNocturna — objeto de valor, no servicio

- **Reglas que resuelve:** RP-03
- **Firma:** `boolean aplicaA(HoraEstimadaLlegada hora)` · `Optional<Cargo> cargoPorCambioDeHora(HoraEstimadaLlegada anterior, HoraEstimadaLlegada nueva, LocalDateTime ahora)`
- **Por qué no vive en un solo agregado:** La regla es sobre valores (hora ≥ hora de inicio): cabe en un objeto de valor y no necesita servicio. Devuelve el recargo, su movimiento inverso o nada, y el caso de uso solo lo entrega a Folio.registrarCargo.
- **Caso de uso que lo invoca:** CU-02, CU-14

```mermaid
flowchart TB
  S["RecargoLlegadaNocturna<br/><i>RP-03</i>"]
  subgraph CO["Coordina"]
    direction LR
    A_Reserva(["Reserva"])
    A_Folio(["Folio"])
  end
  subgraph DE["Depende de"]
    direction LR
    D_ConfiguracionAlojamiento[/"ConfiguracionAlojamiento «puerto»"/]
  end
  S -- "coordina" --> A_Reserva
  S -- "coordina" --> A_Folio
  S -. "depende" .-> D_ConfiguracionAlojamiento
  classDef srv fill:#182951,color:#fff,stroke:#0b1430,stroke-width:2px
  classDef agr fill:#1f6f43,color:#fff,stroke:#0d3b22
  classDef repo fill:#fff4d6,stroke:#b8860b,color:#333
  classDef puerto fill:#f3e8ff,stroke:#7b3fb8,color:#333
  classDef vo fill:#e8f5ec,stroke:#1f6f43,color:#111,stroke-dasharray: 4 3
  class S vo
  class A_Reserva,A_Folio agr
  class D_ConfiguracionAlojamiento puerto
  style CO fill:#fafffb,stroke:#1f6f43
  style DE fill:#fffdf5,stroke:#b8860b
```
