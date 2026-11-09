# Diseño del módulo Facturación — reutilización del ZIP original

**Alcance:** Vargas, grupo 2. Documento de diseño, no certificación de implementación. La fuente de verdad de clases heredadas es `APP_DISTRIBUIDAS.zip` recibido. Las clases nuevas se incorporarán en `app-client`, `app-server` y `app-common` cuando se realice la refactorización WebSocket.

## 1. Inventario real de código reutilizable

| Archivo original | Qué hace hoy | Uso futuro |
|---|---|---|
| `vista/FrmLogin.java` + `.form` | Formulario login | Mantener diseño; llamada a `seguridad.usuario.login` |
| `vista/FrmUsuario.java` + `.form` | CRUD usuarios Swing | Mantener diseño; cambiar llamadas directas al DAO |
| `vista/FrmArticulo.java` + `.form` | Formulario de artículos | Referencia visual para `FrmCliente` y `FrmCiudadEntrega`; no copiar lógica de stock sin revisar |
| `vista/FrmMenuPrincipal.java` + `.form` | Menú con Clientes, Ciudades y Facturas | Agregar acciones y apertura de nuevos formularios |
| `modelo/Articulo.java` | ID, nombre, `double` precio y stock | Mantener compatibilidad; facturación recibirá DTO con `BigDecimal` |
| `dao/ArticuloDAO.java` | `guardarArticulo`, `listarArticulos` con `SEQ_ARTICULO` | Conservar SQL y mover a Persistencia del servidor; revisar `try-with-resources` |
| `modelo/Usuario.java` / `dao/UsuarioDAO.java` | Acceso y gestión de usuarios; usa `TIPO_USUARIO` | Reutilizar en servidor; el DDL nuevo incluye esa columna |
| `conexion/Conexion.java` | JDBC en `localhost:1521:XE` con usuario/clave fijos | No copiar al cliente ni dejar contraseña en código; sustituir por configuración del servidor |

**Nota:** `ArticuloDAO` utiliza variables de instancia `Connection`, `PreparedStatement` y `ResultSet`. Esa forma no es segura cuando el servidor procesa conexiones concurrentes. En la migración, usaremos variables locales y `try-with-resources`.

## 2. Pantallas requeridas

### `FrmCliente` (simple 1)

- Campos: RUC, nombre, dirección. ID de solo lectura, generado por Oracle.
- Botones: Nuevo, Guardar, Buscar, Modificar, Eliminar, Limpiar, Menú.
- Tabla `JTable`: ID, RUC, nombre, dirección.
- Al presionar Guardar: `facturacion.cliente.crear`.
- Validaciones: RUC obligatorio, nombre obligatorio, longitud compatible con Oracle y RUC único.

### `FrmCiudadEntrega` (simple 2)

- Campos: ID no editable, nombre de ciudad.
- Mismo patrón visual de `FrmArticulo`.
- Acciones `facturacion.ciudad.*`.
- No permitir borrar una ciudad ya usada en una factura (FK).

### `FrmFactura` (compleja)

- Cabecera: número automático, fecha, cliente (`JComboBox`), ciudad (`JComboBox`), estado, subtotal, impuesto, total.
- Detalle en `JTable`: artículo (`JComboBox`), cantidad, precio de venta, importe calculado.
- Permite añadir N líneas, editar cantidad y eliminar líneas del **borrador**; al volver a abrir una factura debe recuperar cabecera y todos los detalles asociados.
- Nuevo, Guardar borrador, Buscar, Modificar borrador, Eliminar borrador, Confirmar, Anular y Menú.
- Buscar artículo: reutilizar la consulta de artículos, pero pasando por Escucha/Negocio; nunca conectar el formulario a `ArticuloDAO`.
- El usuario selecciona nombres y opciones, no introduce IDs ni claves foráneas manualmente.
- No disminuir stock mientras la factura esté en `BORRADOR`.

## 3. Modelo Oracle del módulo

| Tabla | Rol | Observación |
|---|---|---|
| `CLIENTE` | Catálogo | `DIRECCION` corrige `ADIRECCION` del SQL original |
| `CIUDAD_ENTREGA` | Catálogo | `NOMBRE_CIUDAD` corrige `NOMBRE_CIUDAD_` |
| `FACTURA` | Cabecera | Campos originales + `ESTADO` (`BORRADOR`, `CONFIRMADA`, `ANULADA`) |
| `DETALLE_FACTURA` | N líneas | `CANTIDAD NUMBER(12,2)` corrige tipo inválido `N10.2`; `PRECIO_VENTA` histórico |
| `ARTICULO` | Catálogo y stock de Inventarios | Se reutiliza, no se duplica |
| `MOVIMIENTO_INVENTARIO` / `DETALLE_MOVIMIENTO` | Egreso al confirmar | Se reutilizan, tipos de movimiento con `SIGNO=-1` |
| **`FACTURA_MOVIMIENTO`** | **Tabla nueva** | Asociación única factura-movimiento y movimiento de reversa opcional |
| `V_SALDO_FACTURA` | **Vista nueva** | Deriva saldo de total confirmado menos pagos aplicados, sin módulo CxC completo |

No se crea una tabla adicional `CUENTA_POR_COBRAR` por acuerdo: el saldo pendiente se obtiene de la factura confirmada y futuros registros en `DETALLE_COBRO`.

## 4. API de negocio (métodos concretos)

```java
// En servidor: métodos esperados, no presentes hoy en el ZIP.
Cliente crearCliente(ClienteCrearDto solicitud);
List<Cliente> listarClientes();
Cliente obtenerCliente(long idCliente);
Cliente actualizarCliente(long idCliente, ClienteActualizarDto solicitud);
void eliminarCliente(long idCliente);

CiudadEntrega crearCiudad(CiudadCrearDto solicitud);
List<CiudadEntrega> listarCiudades();
CiudadEntrega obtenerCiudad(long idCiudad);
CiudadEntrega actualizarCiudad(long idCiudad, CiudadActualizarDto solicitud);
void eliminarCiudad(long idCiudad);

Factura crearBorrador(FacturaCrearDto solicitud);
List<FacturaResumen> listarFacturas(FacturaFiltro filtro);
FacturaConDetalles obtenerFactura(long idFactura);
Factura actualizarBorrador(long idFactura, FacturaActualizarDto solicitud);
void eliminarBorrador(long idFactura);
ConfirmacionFactura confirmarFactura(long idFactura);
AnulacionFactura anularFactura(long idFactura);
SaldoFactura obtenerSaldoFactura(long idFactura);

List<VentasCiudadFila> ventasPorCiudad(LocalDate desde, LocalDate hasta);
ReporteMatriz ventasClienteArticulo(LocalDate desde, LocalDate hasta);
```

La entidad `FacturaCrearDto` contiene una colección obligatoria `detalles` (al menos una línea). El precio se toma del catálogo `ARTICULO` en el servidor; la fecha no debe confiarse sin validación.

## 5. Flujo de confirmación — transacción obligatoria

1. `Escucha`: validar versión/sesión, decodificar `facturacion.factura.confirmar`, llamar a `FacturaService.confirmarFactura(id)`.
2. `Negocio`: abrir transacción, obtener y bloquear cabecera, verificar `ESTADO = BORRADOR`, traer los detalles.
3. Bloquear artículos con `SELECT ... FOR UPDATE` en orden fijo de `ID_ARTICULO` (minimiza interbloqueos).
4. Verificar que cada cantidad solicitada sea menor o igual a `STOCK_ACTUAL` y que stock nunca sea negativo.
5. Crear `MOVIMIENTO_INVENTARIO` tipo `EGRESO POR VENTA` y un `DETALLE_MOVIMIENTO` por línea; actualizar stock una sola vez, coordinado con responsable de Inventarios.
6. Insertar `FACTURA_MOVIMIENTO(ID_FACTURA, ID_MOVIMIENTO)` para evitar doble egreso.
7. Actualizar `FACTURA.ESTADO='CONFIRMADA'` y confirmar la transacción.
8. Ante cualquier error, hacer `ROLLBACK` y responder un error estándar por WebSocket.

Para anular: verificar que no existan cobros aplicados; crear movimiento inverso de Inventarios, registrar `ID_MOVIMIENTO_REVERSA`, cambiar estado a `ANULADA`. Debe ser una sola transacción. Si anulación no es necesaria en la primera entrega, no habilitarla en UI hasta que esté implementada completamente.

## 6. Reportes (requisitos del enunciado)

**Reporte 1: ventas por ciudad**. Consulta sugerida:

```sql
SELECT c.NOMBRE_CIUDAD, SUM(f.TOTAL) AS DOLARES_VENDIDOS
FROM FACTURA f
JOIN CIUDAD_ENTREGA c ON c.ID_CIUDAD = f.ID_CIUDAD
WHERE f.ESTADO = 'CONFIRMADA'
  AND f.FECHA_EMISION >= :desde
  AND f.FECHA_EMISION < :hasta_exclusivo
GROUP BY c.NOMBRE_CIUDAD
ORDER BY c.NOMBRE_CIUDAD;
```

**Reporte 2: cruzado cliente-artículo**. Consultar datos agregados y pivotear dinámicamente en Java para que las columnas representen clientes encontrados en el rango:

```sql
SELECT a.ID_ARTICULO, a.NOMBRE AS ARTICULO,
       c.ID_CLIENTE, c.NOMBRE AS CLIENTE,
       SUM(d.CANTIDAD * d.PRECIO_VENTA) AS DOLARES_VENDIDOS
FROM DETALLE_FACTURA d
JOIN FACTURA f ON f.ID_FACTURA = d.ID_FACTURA
JOIN ARTICULO a ON a.ID_ARTICULO = d.ID_ARTICULO
JOIN CLIENTE c ON c.ID_CLIENTE = f.ID_CLIENTE
WHERE f.ESTADO = 'CONFIRMADA'
  AND f.FECHA_EMISION >= :desde
  AND f.FECHA_EMISION < :hasta_exclusivo
GROUP BY a.ID_ARTICULO, a.NOMBRE, c.ID_CLIENTE, c.NOMBRE
ORDER BY a.NOMBRE, c.NOMBRE;
```

Las fechas deben incluir todo el último día (parámetro `hasta_exclusivo` = día siguiente a `hasta`). Ambos reportes deben permitir imprimir desde Swing; la biblioteca de impresión se decide en implementación.

## 7. Guion de demostración individual (máximo 6 minutos)

1. Entrar a Clientes. Crear A, B y C; buscar A y convertirlo en X; borrar C; verificar Oracle.
2. Repetir Ciudades (A, B, C) y verificar Oracle.
3. Crear factura con cliente/ciudad X y dos líneas de artículos; salir, volver, buscar factura, eliminar una línea y actualizar cantidad de la otra.
4. Mostrar `SELECT` de `FACTURA` y `DETALLE_FACTURA` y confirmar factura con salida en Inventarios.
5. Mostrar ventas por ciudad y matriz ventas cliente-artículo, filtrar e imprimir.

**Ojo:** el PDF de entrega pide crear A y B pero luego borrar C; agregar C es un ajuste práctico para la demostración, no una instrucción textual inequívoca.

## 8. Dependencias y riesgos antes de programar

- Recurso GlassFish externo: interfaz sin especificar. No crear un cliente ficticio a una URL inventada.
- Los datos del ZIP corresponden solo a seguridad y artículos; Facturación y Bancos no tienen clases completas en la versión adjunta.
- No ejecutar `01_esquema_oracle_corregido.sql` en una base que ya tiene tablas: está pensado para **esquema vacío**.
- El script original usa contraseñas en claro y el cliente tiene credenciales Oracle; se deben corregir al migrar los DAO al servidor.
- La tasa impositiva no consta definida en los documentos; evitar una tasa supuesta y acordar criterio docente/comercial.
- La permanencia del módulo CxC en el proyecto exigido debe aclararse con el docente, ya que su ausencia altera la distribución original del grupo.
