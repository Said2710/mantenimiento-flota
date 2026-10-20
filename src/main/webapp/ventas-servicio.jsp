<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.ArrayList,java.math.BigDecimal,java.time.format.DateTimeFormatter,modelo.Cliente,modelo.Vehiculo,modelo.VentaServicio,modelo.DetalleServicio,modelo.DocumentoVenta" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    private BigDecimal totalVenta(List<DetalleServicio> detalles) {
        BigDecimal total = BigDecimal.ZERO;
        if (detalles != null) for (DetalleServicio detalle : detalles) {
            if (detalle.getCantidad() != null && detalle.getPrecioUnitario() != null)
                total = total.add(detalle.getCantidad().multiply(detalle.getPrecioUnitario()));
        }
        return total;
    }
%>
<%
    List<VentaServicio> ventas = (List<VentaServicio>) request.getAttribute("ventas");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    List<Vehiculo> vehiculos = (List<Vehiculo>) request.getAttribute("vehiculos");
    VentaServicio editar = (VentaServicio) request.getAttribute("ventaEditar");
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
    <title>Ventas de servicio | FleetPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<%@ include file="/WEB-INF/jspf/fleet-navigation.jspf" %>
<main class="container-fluid px-3 px-lg-5 py-4 fleet-page-main">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h1 class="h3 mb-1">Ventas de servicio</h1><p class="text-secondary mb-0">Servicios de mantenimiento de la flota y documentos asociados</p></div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white"><h2 class="h5 mb-0"><%= editar == null ? "Registrar venta" : "Editar venta #" + editar.getIdVenta() %></h2></div>
        <div class="card-body">
            <form action="VentaServicioServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %><input type="hidden" name="idVenta" value="<%= editar.getIdVenta() %>"><% } %>
                <div class="col-md-5"><label for="idCliente" class="form-label">Cliente *</label>
                    <select id="idCliente" name="idCliente" class="form-select" required><option value="">Selecciona un cliente</option>
                        <% if (clientes != null) for (Cliente cliente : clientes) if ("Activo".equals(cliente.getEstado())) { %>
                        <option value="<%= cliente.getIdCliente() %>" <%= editar != null && editar.getIdCliente() == cliente.getIdCliente() ? "selected" : "" %>><%= esc(cliente.getRazonSocial()) %></option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-3"><label for="estado" class="form-label">Estado</label>
                    <select id="estado" name="estado" class="form-select">
                        <option value="Pendiente" <%= editar == null || "Pendiente".equals(editar.getEstado()) ? "selected" : "" %>>Pendiente</option>
                        <option value="Confirmada" <%= editar != null && "Confirmada".equals(editar.getEstado()) ? "selected" : "" %>>Confirmada</option>
                    </select>
                </div>
                <div class="col-md-4"><label for="observacion" class="form-label">Observación</label><input id="observacion" name="observacion" class="form-control" maxlength="300" value="<%= esc(editar == null ? "" : editar.getObservacion()) %>"></div>
                <div class="col-12 d-flex justify-content-between align-items-center mt-4"><h3 class="h6 mb-0">Detalle de servicios</h3><button class="btn btn-sm btn-outline-primary" type="button" id="agregarDetalle">Agregar servicio</button></div>
                <div class="col-12" id="detalles">
                    <% for (DetalleServicio detalle : lineas) { %>
                    <div class="row g-2 border rounded p-3 mb-3 detalle-servicio">
                        <div class="col-lg-3"><label class="form-label">Vehículo *</label><select name="idVehiculo" class="form-select" required><option value="">Selecciona</option>
                            <% if (vehiculos != null) for (Vehiculo vehiculo : vehiculos) { %>
                            <option value="<%= vehiculo.getIdVehiculo() %>" <%= detalle.getIdVehiculo() != null && detalle.getIdVehiculo() == vehiculo.getIdVehiculo() ? "selected" : "" %>><%= esc(vehiculo.getPlaca()) %> — <%= esc(vehiculo.getMarca()) %> <%= esc(vehiculo.getModelo()) %> · <%= esc(vehiculo.getEstado()) %></option>
                            <% } %>
                        </select></div>
                        <div class="col-lg-5"><label class="form-label">Servicio *</label><input name="descripcionServicio" class="form-control" maxlength="200" required value="<%= esc(detalle.getDescripcionServicio()) %>"></div>
                        <div class="col-lg-2"><label class="form-label">Fecha</label><input name="fechaServicio" type="datetime-local" class="form-control" value="<%= detalle.getFechaServicio() == null ? "" : detalle.getFechaServicio().format(fechaCampo) %>"></div>
                        <div class="col-lg-1"><label class="form-label">Cantidad</label><input name="cantidad" type="number" class="form-control" min="0.01" step="0.01" required value="<%= detalle.getCantidad() == null ? "1" : detalle.getCantidad().toPlainString() %>"></div>
                        <div class="col-lg-1"><label class="form-label">Precio</label><input name="precioUnitario" type="number" class="form-control" min="0" step="0.01" required value="<%= detalle.getPrecioUnitario() == null ? "0" : detalle.getPrecioUnitario().toPlainString() %>"></div>
                        <div class="col-lg-5"><label class="form-label">Origen</label><input name="origen" class="form-control" maxlength="150" value="<%= esc(detalle.getOrigen()) %>"></div>
                        <div class="col-lg-5"><label class="form-label">Destino</label><input name="destino" class="form-control" maxlength="150" value="<%= esc(detalle.getDestino()) %>"></div>
                        <div class="col-lg-2 d-flex align-items-end"><button type="button" class="btn btn-outline-danger w-100 quitarDetalle">Quitar</button></div>
                    </div>
                    <% } %>
                </div>
                <div class="col-12"><button class="btn btn-primary" type="submit"><%= editar == null ? "Registrar venta" : "Guardar cambios" %></button>
                    <% if (editar != null) { %><a href="VentaServicioServlet" class="btn btn-outline-secondary">Cancelar edición</a><% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Ventas registradas</h2></div>
        <div class="table-responsive"><table class="table table-striped table-hover align-middle mb-0">
            <thead><tr><th>N.º</th><th>Fecha</th><th>Cliente</th><th>Detalle</th><th>Total</th><th>Estado</th><th>Documentos y acciones</th></tr></thead>
            <tbody>
            <% if (ventas == null || ventas.isEmpty()) { %>
                <tr><td colspan="7" class="text-center text-secondary py-4">No hay ventas registradas.</td></tr>
            <% } else for (VentaServicio venta : ventas) { %>
                <tr>
                    <td>#<%= venta.getIdVenta() %></td>
                    <td><%= venta.getFechaVenta() == null ? "—" : venta.getFechaVenta().format(fechaHora) %></td>
                    <td><%= esc(venta.getCliente()) %></td>
                    <td><% if (venta.getDetalles() != null) for (DetalleServicio detalle : venta.getDetalles()) { %><div><%= esc(detalle.getPlaca()) %> · <%= esc(detalle.getDescripcionServicio()) %> (x<%= detalle.getCantidad().toPlainString() %>)</div><% } %></td>
                    <td>S/ <%= totalVenta(venta.getDetalles()).toPlainString() %></td>
                    <td><span class="badge text-bg-<%= "Facturada".equals(venta.getEstado()) ? "success" : "Anulada".equals(venta.getEstado()) ? "secondary" : "warning" %>"><%= esc(venta.getEstado()) %></span></td>
                    <td style="min-width:300px">
                        <% if (venta.getDocumentos() != null && !venta.getDocumentos().isEmpty()) for (DocumentoVenta documento : venta.getDocumentos()) { %>
                        <div class="d-flex align-items-center gap-2 mb-2"><span class="badge text-bg-<%= "Emitido".equals(documento.getEstado()) ? "info" : "secondary" %>"><%= esc(documento.getTipoDocumento()) %> <%= esc(documento.getSerie()) %>-<%= esc(documento.getNumero()) %> · <%= esc(documento.getEstado()) %></span>
                            <% if ("Emitido".equals(documento.getEstado()) && !"Anulada".equals(venta.getEstado())) { %>
                            <form action="VentaServicioServlet" method="post" onsubmit="return confirm('¿Anular este documento?');"><input type="hidden" name="accion" value="anularDocumento"><input type="hidden" name="idDocumento" value="<%= documento.getIdDocumento() %>"><button class="btn btn-sm btn-outline-danger" type="submit">Anular doc.</button></form>
                            <% } %>
                        </div>
                        <% } %>
                        <% if (!"Anulada".equals(venta.getEstado())) { %>
                        <% if ("Confirmada".equals(venta.getEstado()) || "Facturada".equals(venta.getEstado())) { %>
                        <details class="mb-2"><summary class="text-primary">Emitir documento</summary>
                            <form action="VentaServicioServlet" method="post" class="row g-2 mt-1">
                                <input type="hidden" name="accion" value="documento"><input type="hidden" name="idVenta" value="<%= venta.getIdVenta() %>">
                                <div class="col-4"><select name="tipoDocumento" class="form-select form-select-sm" required><option value="">Tipo</option><option value="Factura">Factura</option><option value="Boleta">Boleta</option><option value="Guia">Guía</option></select></div>
                                <div class="col-3"><input name="serie" class="form-control form-control-sm" maxlength="10" placeholder="Serie" required></div>
                                <div class="col-3"><input name="numero" class="form-control form-control-sm" maxlength="20" placeholder="Número" required></div>
                                <div class="col-2"><button class="btn btn-sm btn-primary w-100" type="submit">Emitir</button></div>
                            </form>
                        </details>
                        <% } %>
                        <% if ("Pendiente".equals(venta.getEstado()) || "Confirmada".equals(venta.getEstado())) { %>
                        <a class="btn btn-sm btn-outline-primary" href="VentaServicioServlet?editar=<%= venta.getIdVenta() %>">Editar</a>
                        <% } %>
                        <form action="VentaServicioServlet" method="post" class="d-inline" onsubmit="return confirm('¿Anular esta venta y sus documentos emitidos?');"><input type="hidden" name="accion" value="anular"><input type="hidden" name="idVenta" value="<%= venta.getIdVenta() %>"><button class="btn btn-sm btn-outline-danger" type="submit">Anular venta</button></form>
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
  fila.querySelectorAll('input').forEach((campo) => campo.value = campo.name === 'cantidad' ? '1' : campo.name === 'precioUnitario' ? '0' : '');
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
