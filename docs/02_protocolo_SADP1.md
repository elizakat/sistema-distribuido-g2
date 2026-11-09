# SADP/1 — Contrato de comunicación WebSocket

**Versión:** 1.0 (propuesta para aprobar por el grupo)  
**Proyecto:** Aplicaciones distribuidas, grupo 2  
**Transporte:** WebSocket, mensajes de texto UTF-8 con un objeto JSON por mensaje  
**Roles:** Swing (cliente) → Escucha (decodifica/enruta) → Negocio → Persistencia Oracle. GlassFish es un recurso externo pendiente de concretar.

## 1. Decisiones

- La capa Escucha no ejecuta SQL ni validaciones de dominio: parsea JSON, valida sobre, autentica, localiza la acción y llama al servicio correspondiente.
- Negocio valida las entidades y coordina transacciones, incluidas las operaciones entre Inventarios y Facturación.
- Persistencia ejecuta SQL con `PreparedStatement` y conexiones administradas por operación/transacción; nunca JDBC desde el cliente Swing.
- Los formularios `.java` y `.form` de NetBeans del ZIP se conservan. Sus llamadas `new UsuarioDAO()` o `new ArticuloDAO()` se reemplazan por la interfaz del cliente WebSocket.
- No exponer datos de acceso Oracle, contraseñas, detalles SQL ni excepciones internas en respuestas.
- Convenciones Java: clases `PascalCase`, métodos `camelCase`, constantes `UPPER_SNAKE_CASE`, campos JSON `camelCase`, SQL `UPPER_SNAKE_CASE`.

## 2. Solicitud

```json
{
  "version": "1.0",
  "requestId": "c1c05d18-54b7-4fdb-81d4-764ef67c21d4",
  "action": "facturacion.cliente.crear",
  "payload": {"ruc": "1792345678001", "nombre": "Cliente A", "direccion": "Quito"}
}
```

Campos obligatorios: `version`, `requestId` (UUID v4 recomendado), `action`, `payload` (objeto, `{}` si está vacío). Un mensaje de petición siempre produce una respuesta con el mismo `requestId`, salvo cierre abrupto de conexión. El servidor debe rechazar versiones incompatibles y acciones desconocidas.

## 3. Respuesta

**Éxito**
```json
{"version":"1.0","requestId":"c1c05d18-54b7-4fdb-81d4-764ef67c21d4","success":true,"data":{"idCliente":15},"error":null}
```

**Fallo**
```json
{"version":"1.0","requestId":"c1c05d18-54b7-4fdb-81d4-764ef67c21d4","success":false,"data":null,"error":{"code":"VALIDATION_ERROR","message":"El RUC es obligatorio"}}
```

Errores admitidos: `INVALID_MESSAGE`, `UNSUPPORTED_VERSION`, `UNKNOWN_ACTION`, `VALIDATION_ERROR`, `NOT_FOUND`, `CONFLICT`, `UNAUTHORIZED`, `FORBIDDEN`, `INSUFFICIENT_STOCK`, `INVOICE_ALREADY_CONFIRMED`, `INTERNAL_ERROR`, `SERVICE_UNAVAILABLE`. Se prohibe incluir SQL o stack traces en `message`.

**Representaciones:** fechas `YYYY-MM-DD`; fecha/hora ISO 8601 con zona cuando sea necesario; importes números JSON con 2 decimales y `BigDecimal` en Java; identificadores `long`; valores nulos solo cuando el contrato lo permita. Las claves primarias nunca son introducidas manualmente.

## 4. Catálogo de acciones y métodos de Negocio

El listado **no significa que todas estén implementadas** en el ZIP: distingue llamadas legadas (`UsuarioDAO`, `ArticuloDAO`) de objetivos nuevos.

| Acción `action` | Payload mínimo | `Servicio.metodo` | `data` éxito | Procedencia / alcance |
|---|---|---|---|---|
| `seguridad.usuario.login` | `{username,clave}` | `UsuarioService.login` | `{idUsuario,username,tipoUsuario}` + sesión | Reutilizar `UsuarioDAO.validarLogin/obtenerUsuario`, sin devolver clave |
| `seguridad.usuario.crear` | `{username,clave,tipoUsuario}` | `UsuarioService.crear` | `{idUsuario}` | Reutilizar `UsuarioDAO.guardarUsuario` |
| `seguridad.usuario.listar` | `{}` | `UsuarioService.listar` | `{items:[...]}` | Reutilizar `UsuarioDAO.listarUsuarios`, excluir CLAVE |
| `seguridad.usuario.obtener` | `{idUsuario}` | `UsuarioService.obtener` | `{usuario}` | Completar si falta |
| `seguridad.usuario.actualizar` | `{idUsuario,username,estado,tipoUsuario}` | `UsuarioService.actualizar` | `{idUsuario}` | Reutilizar `UsuarioDAO.modificarUsuario`; cambio de clave separado |
| `seguridad.usuario.eliminar` | `{idUsuario}` | `UsuarioService.eliminar` | `{idUsuario}` | Según regla de usuarios; preferir desactivar |
| `inventario.articulo.crear` | `{nombre,precio,stockActual}` | `ArticuloService.crear` | `{idArticulo}` | Reutilizar `ArticuloDAO.guardarArticulo` |
| `inventario.articulo.listar` | `{}` | `ArticuloService.listar` | `{items:[...]}` | Reutilizar `ArticuloDAO.listarArticulos` |
| `inventario.articulo.obtener` | `{idArticulo}` | `ArticuloService.obtener` | `{articulo}` | Completar |
| `inventario.articulo.actualizar` | `{idArticulo,nombre,precio}` | `ArticuloService.actualizar` | `{idArticulo}` | Completar |
| `inventario.articulo.eliminar` | `{idArticulo}` | `ArticuloService.eliminar` | `{idArticulo}` | Respetar FKs |
| `inventario.tipoMovimiento.{crear,listar,obtener,actualizar,eliminar}` | Campos de tipo | `TipoMovimientoService.<operación>` | Según verbo | Pendiente compañero Inventarios |
| `inventario.movimiento.{crear,listar,obtener,actualizar,eliminar}` | Cabecera + líneas | `MovimientoService.<operación>` | Según verbo | Pendiente compañero Inventarios |
| `inventario.reporte.saldos` | `{desde,hasta}` | `InventarioReporteService.saldos` | `{items:[...]}` | Pendiente Inventarios |
| `inventario.reporte.cruzado` | `{desde,hasta}` | `InventarioReporteService.cruzado` | `{columnas,filas}` | Pendiente Inventarios |
| `facturacion.cliente.crear` | `{ruc,nombre,direccion}` | `ClienteService.crear` | `{idCliente}` | **Nuevo** |
| `facturacion.cliente.listar` | `{}` | `ClienteService.listar` | `{items:[...]}` | **Nuevo** |
| `facturacion.cliente.obtener` | `{idCliente}` | `ClienteService.obtener` | `{cliente}` | **Nuevo** |
| `facturacion.cliente.actualizar` | `{idCliente,ruc,nombre,direccion}` | `ClienteService.actualizar` | `{idCliente}` | **Nuevo** |
| `facturacion.cliente.eliminar` | `{idCliente}` | `ClienteService.eliminar` | `{idCliente}` | **Nuevo**, rechazar si hay factura asociada |
| `facturacion.ciudad.{crear,listar,obtener,actualizar,eliminar}` | `{idCiudad?,nombreCiudad}` | `CiudadEntregaService.<operación>` | Entidad/ID/items | **Nuevo** |
| `facturacion.factura.crear` | Cabecera + `detalles:[{idArticulo,cantidad,precioVenta?}]` | `FacturaService.crearBorrador` | `{idFactura,numeroFactura,total}` | **Nuevo**, una transacción |
| `facturacion.factura.listar` | `{desde?,hasta?,idCliente?}` | `FacturaService.listar` | `{items:[...]}` | **Nuevo**, filtros opcionales |
| `facturacion.factura.obtener` | `{idFactura}` | `FacturaService.obtener` | `{factura,detalles:[...]}` | **Nuevo**, maestro/detalle |
| `facturacion.factura.actualizar` | `{idFactura,idCliente,idCiudad,detalles:[...]}` | `FacturaService.actualizarBorrador` | `{idFactura,total}` | **Nuevo**, reemplazo transaccional del detalle en BORRADOR |
| `facturacion.factura.eliminar` | `{idFactura}` | `FacturaService.eliminarBorrador` | `{idFactura}` | **Nuevo**, solo BORRADOR sin cobros |
| `facturacion.factura.confirmar` | `{idFactura}` | `FacturaService.confirmar` | `{idFactura,estado,idMovimiento}` | **Nuevo**, valida stock, genera egreso y vincula movimiento |
| `facturacion.factura.anular` | `{idFactura}` | `FacturaService.anular` | `{idFactura,estado}` | **Nuevo**, requiere reversión inventario, sin pagos aplicados |
| `facturacion.reporte.ventasPorCiudad` | `{desde,hasta}` | `FacturacionReporteService.ventasPorCiudad` | `{items:[{ciudad,total}]}` | **Nuevo** |
| `facturacion.reporte.ventasClienteArticulo` | `{desde,hasta}` | `FacturacionReporteService.ventasClienteArticulo` | `{columnas:[...],filas:[...]}` | **Nuevo**, pivote en Swing o servicio |
| `facturacion.factura.saldo` | `{idFactura}` | `FacturaService.obtenerSaldo` | `{total,pagado,saldoPendiente}` | **Nuevo**, vista `V_SALDO_FACTURA` sin pantalla CxC |
| `bancos.cuenta.{crear,listar,obtener,actualizar,eliminar}` | Campos de cuenta | `CuentaBancariaService.<operación>` | Según verbo | Pendiente Bancos |
| `bancos.tipoTransaccion.{crear,listar,obtener,actualizar,eliminar}` | Campos de tipo | `TipoTransaccionService.<operación>` | Según verbo | Pendiente Bancos |
| `bancos.transaccion.{crear,listar,obtener,actualizar,eliminar}` | Cabecera + detalles | `TransaccionService.<operación>` | Según verbo | Pendiente Bancos |
| `bancos.reporte.saldos` | `{desde,hasta}` | `BancoReporteService.saldos` | `{items:[...]}` | Pendiente Bancos |
| `bancos.reporte.cruzado` | `{desde,hasta}` | `BancoReporteService.cruzado` | `{columnas,filas}` | Pendiente Bancos |

### Ejemplo de factura

```json
{
  "version": "1.0", "requestId": "ca2d0464-7db4-4006-b9bc-2cd6e55c5a21",
  "action": "facturacion.factura.crear",
  "payload": {
    "idCliente": 1, "idCiudad": 1, "fechaEmision": "2026-11-08",
    "detalles": [
      {"idArticulo": 1, "cantidad": 2},
      {"idArticulo": 2, "cantidad": 1}
    ]
  }
}
```

El precio se debe **obtener del servidor** y guardar como `PRECIO_VENTA` histórico; si se permite editarlo, el servicio valida permisos. La tasa de impuesto no está definida en los PDF: el diseño NO presupone una tasa fiscal. Se propone inicialmente `IMPUESTO = 0` hasta acordar la regla comercial con el grupo y docente.

## 5. Despacho concreto en Escucha

```java
// Pseudocódigo: el parseo JSON y las respuestas se centralizan en Escucha.
switch (request.action()) {
    case "facturacion.cliente.crear" -> clienteService.crear(request.payload());
    case "facturacion.factura.obtener" -> facturaService.obtener(request.payload().idFactura());
    case "facturacion.factura.confirmar" -> facturaService.confirmar(request.payload().idFactura());
    default -> throw new AccionDesconocidaException(request.action());
}
```

**No** se llama dinámicamente a métodos Java por reflexión desde una cadena proporcionada por el cliente. Se utiliza una lista blanca de acciones registradas.

## 6. Concurrencia e integridad

- WebSocket mantiene varias sesiones. No compartir objetos JDBC `Connection`, `PreparedStatement`, `ResultSet` ni estado de formulario entre sesiones.
- `requestId` correlaciona mensajes; NO garantiza idempotencia. Para `confirmar`, el servidor bloquea/verifica estado y `FACTURA_MOVIMIENTO` impide duplicar egresos.
- Control de stock al confirmar en **una sola transacción**: `SELECT ... FOR UPDATE`, comparar cantidad, insertar movimiento y detalles, actualizar stock, insertar `FACTURA_MOVIMIENTO`, actualizar estado y `COMMIT`.
- Un fallo en cualquiera de estos pasos provoca `ROLLBACK`.
- Enviar respuesta después de confirmar la transacción. La confirmación de un cobro futuro no forma parte del módulo mínimo.
- Reglas de seguridad: sesión validada por conexión, cierre/caducidad, autorización por acción; no incluir la clave al listar usuarios.
- Los hilos de lectura de la red no modifican componentes Swing directamente: usar `SwingUtilities.invokeLater`.
- Recomendación inicial configurable (no exigencia textual): 1 MiB máximo por mensaje, 15 s de timeout, logs técnicos con `requestId`.

## 7. Contrato mínimo de GlassFish pendiente

El docente indicó que GlassFish es un **recurso externo**. Antes de escribir el adaptador real, confirmar si es servicio HTTP, EJB remoto u otro recurso y qué interfaz publica. No inventar URL ni asumir que JNDI local de GlassFish es accesible desde el servidor Java independiente.

## 8. Matriz de trazabilidad contra el ZIP

| Implementación del ZIP | Acción futura | Cambio mínimo |
|---|---|---|
| `FrmLogin` → `UsuarioDAO.validarLogin/obtenerUsuario` | `seguridad.usuario.login` | Sustituir llamada DAO por cliente WS; lógica DAO queda servidor |
| `FrmUsuario` → `UsuarioDAO.guardarUsuario/listarUsuarios/modificarUsuario` | `seguridad.usuario.*` | Mantener `.form`, adaptar eventos y excluir clave de respuestas |
| `FrmArticulo` → `ArticuloDAO.guardarArticulo/listarArticulos` | `inventario.articulo.*` | Mantener `.form`, adaptar eventos y completar métodos |
| `Conexion` con credenciales fijas | Persistencia del servidor | Mover configuración a entorno; no empaquetar credenciales en cliente |
| `FrmMenuPrincipal` | Navegación | Conservar y conectar nuevos formularios de facturación |
| `Usuario.tipoUsuario` | `USUARIO.TIPO_USUARIO` | Columna incorporada al DDL corregido |

## 9. Pruebas de aceptación

1. Abrir el servidor y dos clientes Swing independientes.
2. Enviar simultáneamente dos solicitudes con `requestId` diferentes y validar correlación.
3. Insertar, buscar, editar y eliminar clientes y ciudades por WebSocket; consultar Oracle directamente.
4. Guardar borrador con 2 detalles, reabrir, modificar una cantidad, eliminar un detalle y verificar persistencia.
5. Confirmar factura con stock suficiente; asegurar una sola salida de inventario.
6. Rechazar confirmación cuando no hay stock sin cambios parciales.
7. Recuperar saldo pendiente desde `V_SALDO_FACTURA`.
8. Mostrar reportes por rango de fechas e imprimirlos.
9. Rechazar acción desconocida y sesión no autorizada sin detener servidor.

> Las acciones de Inventarios y Bancos son **contratos propuestos** para migración, no funcionalidades verificadas como existentes en el ZIP. Se conservan para que todos los módulos usen el mismo protocolo.
