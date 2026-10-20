# Demostración de semana 10

## Preparación

1. Ejecutar `database/semana10_ventas.sql` en `db-mantenimiento-flota` desde Azure Query Editor o SSMS.
2. Si se desea una solicitud de demostración, ejecutar `database/datos_demo_semana10.sql`.
3. Reconstruir y desplegar el WAR en Apache Tomcat o desde NetBeans.
4. Iniciar sesión con una cuenta administradora.

## Recorrido

1. Abrir **Solicitudes de servicio**.
2. Registrar un cliente activo, elegir un vehículo y añadir un servicio. Guardar.
3. Aprobar la solicitud; después convertirla en venta.
4. Abrir **Ventas de servicio** y revisar cliente, vehículo, detalle e importe.
5. Emitir una factura o boleta con una serie y número que no existan. Registrar una guía si corresponde.
6. Revisar la lista de documentos y el estado de la venta.
7. Mostrar la anulación como operación que conserva el historial.

## Datos de muestra

El script idempotente `database/datos_demo_semana10.sql` crea o reutiliza un cliente `DEMO10-W10`, un vehículo operativo `DEMO10` y una SOLPED pendiente con un servicio preventivo, cantidad `1` y precio estimado `250.00`. Las filas de muestra quedan guardadas y activas para poder repetir la demostración. Usa una serie y numeración documental reservadas para pruebas.

## Puntos técnicos para explicar

- La interfaz JSP no ejecuta SQL.
- El Servlet valida permisos y datos.
- Las interfaces Repository separan los controladores de JDBC.
- La conversión de SOLPED a venta se confirma como una sola transacción.
- Las anulaciones actualizan el estado y preservan las filas.
