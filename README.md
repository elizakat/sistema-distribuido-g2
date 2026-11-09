# Sistema distribuido — Grupo 2 (Java 17 / Swing / WebSocket / Oracle)

Refactorización del proyecto `APP_DISTRIBUIDAS.zip` original. **La interfaz NetBeans `.form` de Login, Usuarios, Artículos y Menú se conserva**. Los adaptadores DAO en el cliente ahora envían las solicitudes del protocolo SADP/1 mediante WebSocket; el cliente no contiene JDBC.

## Módulos

- `app-common`: DTO/modelos existentes y protocolo JSON común.
- `app-client`: pantallas Swing originales, formularios editables en el diseñador NetBeans para Clientes, Ciudades, Factura y reportes (tablas imprimibles). Usa `java.net.http.WebSocket` de Java 17.
- `app-server`: servidor concurrente WebSocket (Java-WebSocket), Escucha/Router, lógica de negocio, persistencia JDBC en Oracle.
- `database/01_esquema_oracle_corregido.sql`: esquema consolidado Oracle 12c+ con FACTURA_MOVIMIENTO y vista V_SALDO_FACTURA. **Solo base vacía**.

## Ejecutar

1. Java 17+ y Maven 3.8+ instalados; acceso a repositorios Maven para descargar dependencias.
2. En Oracle crear un usuario de aplicación con permisos de tablas/secuencias y ejecutar `database/01_esquema_oracle_corregido.sql` **en ese esquema vacío**. Si ya tienes las tablas, NO ejecutes el script completo: prepara migraciones incrementales.
3. En la terminal del servidor configurar `ORACLE_URL` (predeterminado `jdbc:oracle:thin:@localhost:1521/XEPDB1`), `ORACLE_USER` (`APP_G2`) y `ORACLE_PASSWORD` (obligatoria). No usar SYSTEM desde la app.
4. En raíz: `mvn clean install -DskipTests`.
5. Ejecutar servidor en una terminal: `mvn -pl app-server exec:java -Dexec.mainClass=com.mycompany.app_distribuidas.escucha.ServidorMain` (el plugin exec necesita configurarse/descargarse). Alternativamente ejecutar `ServidorMain` desde NetBeans después de construir.
6. Ejecutar cliente en otra terminal: `mvn -pl app-client exec:java -Dexec.mainClass=com.mycompany.app_distribuidas.Client`. O ejecutar la clase `Client` desde NetBeans. Para conectar a servidor remoto, configurar `APP_WS_URL=ws://IP_SERVIDOR:8081`.
7. Ingresar con el administrador de prueba únicamente en ambiente académico: `admin / admin123`. Cambiarlo para cualquier otro ambiente.

## Flujo Facturación

1. Crear Clientes y Ciudades; consultar artículos en inventario.
2. Crear factura BORRADOR con 1 o más detalles; precio se lee desde Oracle (no se acepta precio impuesto por Swing).
3. Editar/eliminar líneas del borrador y guardar con `Actualizar borrador` (reemplazo transaccional).
4. Confirmar: bloquea filas de artículos, verifica existencias, registra MOVIMIENTO_INVENTARIO y DETALLE_MOVIMIENTO, descuenta stock y relaciona FACTURA_MOVIMIENTO; todo dentro de una transacción.
5. Anular: sin cobros aplicados, crea movimiento de reversa, devuelve stock y cambia estado.
6. Reportes: ventas por ciudad y matriz cliente/artículo; filtrar por fecha e imprimir.
7. CxC mínimo: `facturacion.factura.saldo` consulta V_SALDO_FACTURA, sin pantalla de cobros.

## Límites conocidos y decisiones pendientes

- **GlassFish externo**: no se ha implementado un adaptador hacia el recurso GlassFish porque no se especificó su interfaz (JNDI remoto, EJB o servicio REST). El servidor actual usa JDBC directo *desde Persistencia*, sin acceso JDBC en Swing. Para cumplir completamente con el diagrama del docente debe implementarse esta integración cuando se conozca su contrato.
- **Módulo Bancos**: no aparece implementado en el ZIP original; no se inventan formularios ni su lógica. El contrato está en `docs/02_protocolo_SADP1.md`.
- **Inventarios**: preserva FrmArticulo y operaciones crear/listar. Tipo movimiento, comprobantes y reportes de Inventarios no estaban implementados en el ZIP.
- **Seguridad**: para conservar compatibilidad con el script original, login usa clave en texto plano y se exige login por cada conexión, pero no hay control de permisos ni token persistente; esta configuración es **solo para laboratorio académico**, no apta para despliegue real. Debe mejorarse antes de exposición a red no confiable.
- **IVA**: el enunciado no especifica la tasa; la factura utiliza impuesto 0 inicialmente, hasta que se acuerde la regla de negocio.
- **Bloqueo de Swing**: formularios originales y los nuevos usan llamadas síncronas WebSocket en algunos listeners; para un cliente robusto se deben migrar a SwingWorker, evitando congelamientos si el servidor se demora.
- **Validación**: los artefactos no se han probado conectados a Oracle/GlassFish dentro de este entorno. No afirmar integración probada sin ejecutar allí.
- En la versión del proyecto original, `.form` de NetBeans puede regenerar partes de su código y requerir revalidar navegación agregada manualmente.

## Documentación

`docs/02_protocolo_SADP1.md` incluye la lista de acciones planificadas; **no todas están implementadas**. La implementación de esta entrega atiende Seguridad, Artículos (crear/listar) y Facturación con reportes. `docs/03_diseno_facturacion.md` documenta modelo e integraciones.

## ¿Dónde está Facturación en app-client?

Las clases de la interfaz están en `app-client/src/main/java/com/mycompany/app_distribuidas/vista/`:

- `FrmCliente.java`: catálogo de clientes.
- `FrmCiudadEntrega.java`: catálogo de ciudades.
- `FrmFactura.java`: cabecera, detalles y confirmación de ventas.
- `FrmReporteFacturacion.java`: dos reportes por rango de fechas.

Los cuatro formularios nuevos tienen pares `.java` y `.form` compatibles con GUI Builder de NetBeans. La lógica de los botones permanece fuera de `initComponents()` para protegerla cuando NetBeans regenere la interfaz. Desde el menú **Facturación** se abren Clientes, Ciudades, Facturas y Reportes.

Punto de entrada: clase `com.mycompany.app_distribuidas.Client` en archivo `Client.java`. El servidor usa `Router` para resolver `action` y llamar a Negocio.

## Convenciones de nombres y formularios

- Entrada de escritorio: `app-client/src/main/java/com/mycompany/app_distribuidas/Client.java` (`com.mycompany.app_distribuidas.Client`).
- Enrutamiento de acciones: `app-server/src/main/java/com/mycompany/app_distribuidas/escucha/Router.java`.
- Facturación en `app-client/.../vista/`: `FrmCliente.java`, `FrmCiudadEntrega.java`, `FrmFactura.java`, `FrmReporteFacturacion.java`.
- Los cuatro formularios de Facturación incluyen su archivo `.form` para diseño visual en NetBeans. Los archivos `.form` originales también se conservan.
- Los fuentes y documentación se guardaron con finales de línea LF; NetBeans puede mostrarlos en Windows.

## Diseño visual NetBeans

En `app-client` abre **Source Packages → com.mycompany.app_distribuidas.vista** y haz doble clic en `FrmCliente`, `FrmCiudadEntrega`, `FrmFactura` o `FrmReporteFacturacion`. Usa la pestaña **Design**; sus pares `.form` están junto al código Java. Evita editar manualmente `initComponents()` porque es código administrado por NetBeans.
