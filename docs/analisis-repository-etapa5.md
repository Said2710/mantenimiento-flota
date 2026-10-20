# Análisis Repository de clientes y proveedores

## Propósito

Separar las pantallas y reglas del sistema del código que consulta SQL Server. Los módulos de clientes, proveedores y repuestos siguen el mismo flujo y pueden cambiar su almacenamiento sin rehacer las páginas.

## Bloques del diseño

| Bloque | Archivos | Responsabilidad |
|---|---|---|
| Interfaz | `clientes.jsp`, `proveedores.jsp`, `repuestos.jsp` | Capturar datos, mostrar registros y enviar acciones HTTP. |
| Controlador | `ClienteServlet`, `ProveedorServlet`, `RepuestoServlet` | Validar sesión, datos y acción; invocar el repositorio y devolver mensajes. |
| Entidades | `Cliente`, `Proveedor`, `Repuesto` | Representar cada registro con tipos Java. |
| Contrato Repository | `ClienteRepository`, `ProveedorRepository`, `RepuestoRepository` | Definir listar, buscar, crear, editar e inactivar. |
| Implementación JDBC | Clases `*RepositoryJdbc` | Ejecutar SQL parametrizado, mapear filas y manejar conexiones. |
| Conexión | `ConexionBD` | Entregar conexiones a Azure SQL Server usando variables de entorno. |
| Persistencia | Tablas `Cliente`, `Proveedor`, `Repuesto` | Guardar los datos y aplicar claves, restricciones y valores predeterminados. |

## Recorrido de una operación

1. La página envía una acción `POST` al Servlet.
2. El Servlet confirma que la sesión tiene un administrador y valida los campos.
3. El Servlet crea o actualiza una entidad y llama al contrato Repository.
4. La implementación JDBC usa `PreparedStatement` para ejecutar el SQL.
5. SQL Server responde; JDBC devuelve un resultado al Servlet.
6. El Servlet guarda un mensaje temporal y redirige a la página del módulo.

## Decisiones de persistencia

- Los valores se envían con parámetros JDBC; no se concatenan en la consulta.
- Los campos opcionales se convierten a `NULL` cuando están vacíos.
- La opción de eliminar cambia el estado a `Inactivo` para conservar el historial.
- Los registros se presentan con texto escapado para evitar interpretar entradas como HTML.

## Alcance y evidencia

En la aplicación se probaron registro, edición e inactivación de proveedores y repuestos. La prueba dejó sus filas de verificación inactivas en la base de datos. Los cambios del módulo de clientes se probaron previamente desde la aplicación.
