# Análisis del módulo de ventas de servicio

## Propósito

Registrar una solicitud de mantenimiento por cliente y vehículo, aprobarla, convertirla en una venta de servicio y emitir su factura, boleta o guía.

## Flujo

`SOLPED pendiente → SOLPED aprobada → Venta de servicio → Factura o boleta y, cuando corresponde, guía`

La conversión se ejecuta dentro de una transacción. Si no se copian el encabezado y todos los detalles de la solicitud, se revierte la operación y la solicitud conserva su estado.

## Bloques del diseño

| Bloque | Archivos | Función |
|---|---|---|
| Interfaz de solicitudes | `solicitudes-servicio.jsp` | Registra el cliente y uno o varios servicios asociados a vehículos; lista y permite aprobar, editar, anular o convertir SOLPED. |
| Controlador de solicitudes | `SolicitudServicioServlet` | Comprueba sesión de administrador, valida datos y coordina las acciones sobre la SOLPED. |
| Interfaz de ventas | `ventas-servicio.jsp` | Registra y edita ventas, muestra detalles y permite emitir o anular documentos. |
| Controlador de ventas | `VentaServicioServlet` | Valida cliente, servicios, cantidad, precio, estado y numeración documental. |
| Entidades | `SolicitudServicio`, `VentaServicio`, `DetalleServicio`, `DocumentoVenta` | Representan encabezados y detalles de cada etapa. |
| Contratos Repository | `SolicitudServicioRepository`, `VentaServicioRepository`, `DocumentoVentaRepository` | Aíslan a los controladores de la tecnología de persistencia. |
| Implementaciones JDBC | `SolicitudServicioRepositoryJdbc`, `VentaServicioRepositoryJdbc`, `DocumentoVentaRepositoryJdbc` | Consultan SQL Server, convierten filas en entidades y agrupan encabezados con sus detalles. |
| Conexión | `ConexionBD` | Abre las conexiones seguras hacia Azure SQL Server. |

## Modelo de datos

- `SolicitudServicio`: encabezado SOLPED, cliente, fecha, estado, observación, usuario que registra y venta generada.
- `DetalleSolicitudServicio`: vehículo, servicio, ruta opcional, fecha programada, cantidad y precio estimado.
- `VentaServicio` y `DetalleVentaServicio`: encabezado y detalle de la venta de servicio.
- `DocumentoVenta`: tipo, serie, número, fecha, estado y venta asociada.
- Las claves foráneas enlazan solicitudes con clientes, ventas y usuarios; los detalles enlazan vehículos y encabezados.

## Reglas de integridad

- Solo un administrador puede gestionar estos módulos.
- La SOLPED debe tener cliente y por lo menos un servicio con vehículo, cantidad positiva y precio no negativo.
- Una SOLPED pendiente se puede editar; la conversión requiere que esté aprobada.
- La solicitud y la venta se enlazan dentro de la misma transacción, evitando duplicar una conversión.
- La serie y el número son únicos por tipo de documento.
- La factura o boleta se emite cuando la venta está confirmada; solo una puede quedar emitida a la vez. Puede asociarse una guía.
- Si se anula la factura o boleta vigente, la venta vuelve al estado `Confirmada`.
- Anular una venta conserva el registro y marca como anulados sus documentos emitidos.

## Secuencia para explicar en la exposición

1. Crear una SOLPED y agregar uno o más vehículos y servicios.
2. Aprobar la solicitud.
3. Convertirla en venta y verificar que se copiaron todos los detalles.
4. Emitir factura o boleta; mostrar que la venta cambia a `Facturada`.
5. Registrar una guía si aplica.
6. Anular un documento o una venta para mostrar el control de estado y la conservación del historial.

## Preparación de la base de datos

Ejecutar `database/semana10_ventas.sql` una vez en la base Azure SQL antes de publicar el proyecto. Para cargar una SOLPED sintética reproducible, ejecutar opcionalmente `database/datos_demo_semana10.sql` después de confirmar que existen un cliente activo y un vehículo operativo. El flujo de venta y documento se completa desde la interfaz.
