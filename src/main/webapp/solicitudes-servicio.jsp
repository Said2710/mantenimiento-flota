<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.ArrayList,java.math.BigDecimal,java.time.format.DateTimeFormatter,modelo.Cliente,modelo.Vehiculo,modelo.SolicitudServicio,modelo.DetalleServicio" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    private BigDecimal totalSolicitud(List<DetalleServicio> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        if (detalles != null) for (DetalleServicio detalle : detalles) {
            if (detalle.getCantidad() != null && detalle.getPrecioUnitario() != null)
                total = total.add(detalle.getCantidad().multiply(detalle.getPrecioUnitario()));
        }
        return total;
    }
%>
<%
    List<SolicitudServicio> solicitudes = (List<SolicitudServicio>) request.getAttribute("solicitudes");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    List<Vehiculo> vehiculos = (List<Vehiculo>) request.getAttribute("vehiculos");
    SolicitudServicio editar = (SolicitudServicio) request.getAttribute("solicitudEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
    DateTimeFormatter fechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    DateTimeFormatter fechaCampo = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    List<DetalleServicio> lineas = editar == null ? new ArrayList<>() : editar.getDetalles();
    if (lineas == null || lineas.isEmpty()) lineas = new ArrayList<>();
    if (lineas.isEmpty()) lineas.add(new DetalleServicio());
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Solicitudes de servicio | FleetPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<%@ include file="/WEB-INF/jspf/fleet-navigation.jspf" %>
<main class="container-fluid px-3 px-lg-5 py-4 fleet-page-main">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h1 class="h3 mb-1">Solicitudes de servicio</h1><p class="text-secondary mb-0">SOLPED de mantenimiento por cliente y vehículo</p></div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white"><h2 class="h5 mb-0"><%= editar == null ? "Registrar SOLPED" : "Editar SOLPED #" + editar.getIdSolicitud() %></h2></div>
        <div class="card-body">
            <form action="SolicitudServicioServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %><input type="hidden" name="idSolicitud" value="<%= editar.getIdSolicitud() %>"><% } %>
                <div class="col-md-6">
                    <label for="idCliente" class="form-label">Cliente *</label>
                    <select id="idCliente" name="idCliente" class="form-select" required>
                        <option value="">Selecciona un cliente</option>
                        <% if (clientes != null) for (Cliente cliente : clientes) if ("Activo".equals(cliente.getEstado())) { %>
                        <option value="<%= cliente.getIdCliente() %>" <%= editar != null && editar.getIdCliente() == cliente.getIdCliente() ? "selected" : "" %>><%= esc(cliente.getRazonSocial()) %></option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-6">
                    <label for="observacion" class="form-label">Observación</label>
                    <input id="observacion" name="observacion" class="form-control" maxlength="300" value="<%= esc(editar == null ? "" : editar.getObservacion()) %>">
                </div>
                <div class="col-12 d-flex justify-content-between align-items-center mt-4">
                    <h3 class="h6 mb-0">Detalle de servicios</h3>
                    <button class="btn btn-sm btn-outline-primary" type="button" id="agregarDetalle">Agregar servicio</button>
                </div>
                <div class="col-12" id="detalles">
                    <% for (DetalleServicio detalle : lineas) { %>
                    <div class="row g-2 border rounded p-3 mb-3 detalle-servicio">
                        <div class="col-lg-3"><label class="form-label">Vehículo *</label>
                            <select name="idVehiculo" class="form-select" required><option value="">Selecciona</option>
                                <% if (vehiculos != null) for (Vehiculo vehiculo : vehiculos) if ("Operativo".equals(vehiculo.getEstado())) { %>
                                <option value="<%= vehiculo.getIdVehiculo() %>" <%= detalle.getIdVehiculo() != null && detalle.getIdVehiculo() == vehiculo.getIdVehiculo() ? "selected" : "" %>><%= esc(vehiculo.getPlaca()) %> — <%= esc(vehiculo.getMarca()) %> <%= esc(vehiculo.getModelo()) %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-lg-5"><label class="form-label">Servicio *</label><input name="descripcionServicio" class="form-control" maxlength="200" required value="<%= esc(detalle.getDescripcionServicio()) %>"></div>
                        <div class="col-lg-2"><label class="form-label">Fecha programada</label><input name="fechaServicio" type="datetime-local" class="form-control" value="<%= detalle.getFechaServicio() == null ? "" : detalle.getFechaServicio().format(fechaCampo) %>"></div>
                        <div class="col-lg-1"><label class="form-label">Cantidad</label><input name="cantidad" type="number" class="form-control" min="0.01" step="0.01" required value="<%= detalle.getCantidad() == null ? "1" : detalle.getCantidad().toPlainString() %>"></div>
                        <div class="col-lg-1"><label class="form-label">Precio</label><input name="precioEstimado" type="number" class="form-control" min="0" step="0.01" required value="<%= detalle.getPrecioUnitario() == null ? "0" : detalle.getPrecioUnitario().toPlainString() %>"></div>
                        <div class="col-lg-5"><label class="form-label">Origen</label><input name="origen" class="form-control" maxlength="150" value="<%= esc(detalle.getOrigen()) %>"></div>
                        <div class="col-lg-5"><label class="form-label">Destino</label><input name="destino" class="form-control" maxlength="150" value="<%= esc(detalle.getDestino()) %>"></div>
                        <div class="col-lg-2 d-flex align-items-end"><button type="button" class="btn btn-outline-danger w-100 quitarDetalle">Quitar</button></div>
                    </div>
                    <% } %>
                </div>
                <div class="col-12"><button class="btn btn-primary" type="submit"><%= editar == null ? "Registrar solicitud" : "Guardar cambios" %></button>
                    <% if (editar != null) { %><a href="SolicitudServicioServlet" class="btn btn-outline-secondary">Cancelar edición</a><% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Solicitudes registradas</h2></div>
        <div class="table-responsive"><table class="table table-striped table-hover align-middle mb-0">
            <thead><tr><th>N.º</th><th>Fecha</th><th>Cliente</th><th>Servicios</th><th>Total estimado</th><th>Estado</th><th>Venta</th><th>Acciones</th></tr></thead>
            <tbody>
            <% if (solicitudes == null || solicitudes.isEmpty()) { %>
                <tr><td colspan="8" class="text-center text-secondary py-4">No hay solicitudes registradas.</td></tr>
            <% } else for (SolicitudServicio solicitud : solicitudes) { %>
                <tr>
                    <td>#<%= solicitud.getIdSolicitud() %></td>
                    <td><%= solicitud.getFechaSolicitud() == null ? "—" : solicitud.getFechaSolicitud().format(fechaHora) %></td>
                    <td><%= esc(solicitud.getCliente()) %></td>
                    <td><% if (solicitud.getDetalles() != null) for (DetalleServicio detalle : solicitud.getDetalles()) { %><div><%= esc(detalle.getPlaca()) %> · <%= esc(detalle.getDescripcionServicio()) %></div><% } %></td>
                    <td>S/ <%= totalSolicitud(solicitud.getDetalles()).toPlainString() %></td>
                    <td><span class="badge text-bg-<%= "Convertida".equals(solicitud.getEstado()) ? "primary" : "Pendiente".equals(solicitud.getEstado()) ? "warning" : "Anulada".equals(solicitud.getEstado()) ? "secondary" : "success" %>"><%= esc(solicitud.getEstado()) %></span></td>
                    <td><%= solicitud.getIdVenta() == null ? "—" : "#" + solicitud.getIdVenta() %></td>
                    <td class="text-nowrap">
                        <% if ("Pendiente".equals(solicitud.getEstado())) { %>
                        <a class="btn btn-sm btn-outline-primary" href="SolicitudServicioServlet?editar=<%= solicitud.getIdSolicitud() %>">Editar</a>
                        <form action="SolicitudServicioServlet" method="post" class="d-inline"><input type="hidden" name="accion" value="aprobar"><input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>"><button class="btn btn-sm btn-outline-success" type="submit">Aprobar</button></form>
                        <form action="SolicitudServicioServlet" method="post" class="d-inline" onsubmit="return confirm('¿Anular esta solicitud?');"><input type="hidden" name="accion" value="anular"><input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>"><button class="btn btn-sm btn-outline-danger" type="submit">Anular</button></form>
                        <% } else if ("Aprobada".equals(solicitud.getEstado())) { %>
                        <form action="SolicitudServicioServlet" method="post" class="d-inline" onsubmit="return confirm('¿Convertir la solicitud en una venta?');"><input type="hidden" name="accion" value="convertir"><input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>"><button class="btn btn-sm btn-primary" type="submit">Convertir en venta</button></form>
                        <form action="SolicitudServicioServlet" method="post" class="d-inline" onsubmit="return confirm('¿Anular esta solicitud?');"><input type="hidden" name="accion" value="anular"><input type="hidden" name="idSolicitud" value="<%= solicitud.getIdSolicitud() %>"><button class="btn btn-sm btn-outline-danger" type="submit">Anular</button></form>
                        <% } else if (solicitud.getIdVenta() != null) { %>
                        <a class="btn btn-sm btn-outline-primary" href="VentaServicioServlet?editar=<%= solicitud.getIdVenta() %>">Ver venta</a>
                        <% } %>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table></div>
    </section>
</main>
<script>
const contenedor = document.getElementById('detalles');
document.getElementById('agregarDetalle').addEventListener('click', () => {
  const fila = contenedor.querySelector('.detalle-servicio').cloneNode(true);
  fila.querySelectorAll('input').forEach((campo) => campo.value = campo.name === 'cantidad' ? '1' : campo.name === 'precioEstimado' ? '0' : '');
  fila.querySelector('select').selectedIndex = 0;
  contenedor.appendChild(fila);
});
contenedor.addEventListener('click', (evento) => {
  if (evento.target.classList.contains('quitarDetalle')) {
    if (contenedor.querySelectorAll('.detalle-servicio').length > 1) evento.target.closest('.detalle-servicio').remove();
  }
});
</script>
</body>
</html>
