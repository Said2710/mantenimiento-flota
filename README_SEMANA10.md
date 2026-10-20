# Entrega de semana 10

## Incluye

- Solicitudes de servicio (SOLPED) con encabezado y detalle.
- Conversión transaccional de una SOLPED aprobada a venta de servicio.
- Gestión de ventas, líneas de servicio y documentos Factura, Boleta y Guía.
- Migración SQL Server y datos de demostración opcionales.
- Análisis Repository de las etapas 5 y 6, y recorrido para la exposición.
- Presentación editable de semana 10.

## Instalación

1. Haz una copia de seguridad de la base `db-mantenimiento-flota`.
2. Abre `database/semana10_ventas.sql` en Azure Query Editor o SSMS, selecciona `db-mantenimiento-flota` y ejecútalo completo.
3. Para cargar una SOLPED de ejemplo, ejecuta `database/datos_demo_semana10.sql`. Crea o reutiliza las filas claramente marcadas `DEMO10-W10` y `DEMO10`, y las deja activas para la demostración.
4. Abre en NetBeans el proyecto Maven completo, el que contiene `pom.xml`.
5. Conserva tu variable `DB_PASSWORD` configurada. No la escribas en el código ni la incluyas en la entrega.
6. Ejecuta **Clean and Build** y publica el WAR en tu servidor Tomcat compatible con Jakarta EE 10.
7. Inicia sesión como administrador y abre **Solicitudes de servicio** desde el panel.

## Presentación

Abre `outputs/Semana10_FleetPro.pptx`. Incluye el proceso, el esquema, el análisis por bloques, un guion de demostración y el avance según la hoja de etapas.

## Estado de la base de demostración

La migración se aplicó en `db-mantenimiento-flota` y se verificó de forma idempotente. La prueba web completó la SOLPED, su conversión a la venta N.º 1, la confirmación de la venta y la emisión de una factura. La factura de verificación `TST102026-000001` se anuló para comprobar la recuperación del estado; luego se emitió `TST102026-000002`, que quedó vigente. La venta de demostración queda en estado `Facturada`. El cliente `DEMO10-W10` y el vehículo `DEMO10` son datos de prueba. `PCK373` permanece inactivo.

## Nota de verificación

El código Java compila y el WAR se empaqueta correctamente con Java 17. En Tomcat se verificaron las rutas de SOLPED y ventas, la aprobación y conversión transaccional, la edición de la venta, la emisión de factura y la anulación del documento. La base requiere la variable `DB_PASSWORD` configurada en el entorno de Tomcat.
