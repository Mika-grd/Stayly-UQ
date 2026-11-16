# Diseño de la interfaz

## De dónde sale

Stayly son seis apartamentos en Armenia, Quindío. El rasgo más reconocible del Eje Cafetero son las
fachadas de bahareque de Salento y Filandia: muros encalados con balcones, puertas y postigos pintados en
colores planos y fuertes. La interfaz toma de ahí su paleta y su recurso principal.

## Tokens

| Nombre | Color | Uso |
|---|---|---|
| Tinta | `#1D2A44` | Texto, bordes de 2 px y la capa de atrás |
| Cal | `#FFFFFF` | Placas (el contenido) |
| Muro | `#EEF1F4` | Fondo de la página |
| Balcón | `#D62839` | Acción principal (Buscar, Reservar), cancelación, SUQ-101 |
| Cobalto | `#2347C8` | Confirmada, folio, SUQ-102 |
| Cafetal | `#13694A` | En curso, pagos, llegada y salida, SUQ-201 |
| Maíz | `#F2B300` | Pendiente, saldo por pagar, SUQ-202 |
| Lila y aguamarina | `#6B3FC4`, `#0E8A8A` | SUQ-301 y SUQ-302 |

Cada apartamento tiene siempre el mismo color: en su fachada, en la losa de su tarjeta, en su casilla del
calendario y en su número en recepción. Así se reconoce de un vistazo en cualquier pantalla.

**Tipografía.** Archivo en ancho expandido (112–125 %) y peso 800–900 para titulares y cifras: recuerda
los letreros pintados. Hanken Grotesk para leer y operar formularios. Escala 1.25 sobre 16 px.

## Capas

El contenido va en **placas** blancas con borde de tinta, apoyadas sobre una **losa** de color sólido
desplazada 8 px, sin desenfoque. El panel de reserva lleva tres capas (placa, losa de color, losa de tinta).
Los botones tienen su propia losa de tinta y al presionarlos bajan hasta ella.

## Lo memorable

La **cuadra de seis fachadas** de la portada, dibujada en SVG: cada casa es uno de los apartamentos, con su
color, su piso (SUQ-**2**01 tiene el balcón en el segundo) y tantas ventanas como dormitorios. La misma
ilustración reemplaza la foto cuando la imagen no carga. Todo lo demás es sobrio: una sola animación (el
botón que baja a su capa) y ninguna decoración que no diga algo.

## Reglas de escritura

Verbos claros en los botones (Reservar, Registrar pago, Cerrar folio), el mismo nombre para la acción y su
confirmación, y errores que dicen qué pasó y qué hacer: el mensaje viene del dominio.

## Versión HARSH

Un interruptor en la cabecera cambia a la versión HARSH: la misma interfaz en clave gótica. Solo redefine tokens y decoración en `src/styles/harsh.css` (activado con `data-tema="harsh"` en `<html>` y guardado en el navegador); textos, estructura y comportamiento no cambian.

- Negros y grises con tinte morado (no un inverso): fondo casi negro, placas carbón, bordes de hierro y texto lavanda pálido. Los seis colores de fachada pasan a sangre, índigo de noche, musgo, bronce oxidado, morado obispo y pizarra; la sangre es el único acento encendido.
- Fachadas con contorno negro y ventanas con luz de vela.
- Metal Mania para titulares, Pirata One para cifras y Barlow Semi Condensed para leer.
- Esquinas rectas, placas con filete interior y cortes en diente de sierra bajo la cabecera y sobre el pie.
- En la portada, luna de sangre, cordilleras en silueta, murciélagos que cruzan el cielo y, en lugar de la cuadra, un camino oscuro: árboles secos, postes de cerca y una trocha de tierra por la que pasa el Willys con los faros encendidos.
