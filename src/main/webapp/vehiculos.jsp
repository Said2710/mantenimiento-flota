<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Vehiculo" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Vehiculo> vehiculos = (List<Vehiculo>) request.getAttribute("vehiculos");
    Vehiculo editar = (Vehiculo) request.getAttribute("vehiculoEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Vehículos | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<%@ include file="/WEB-INF/jspf/fleet-navigation.jspf" %>
<main class="container py-4 fleet-page-main">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Vehículos</h1>
            <p class="text-secondary mb-0">Administración de unidades de la flota</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert">
        <%= esc(mensaje) %>
    </div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white">
            <h2 class="h5 mb-0"><%= editar == null ? "Registrar vehículo" : "Editar vehículo" %></h2>
        </div>
        <div class="card-body">
            <form action="VehiculoServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %>
                <input type="hidden" name="idVehiculo" value="<%= editar.getIdVehiculo() %>">
                <% } %>
                <div class="col-md-4">
                    <label for="placa" class="form-label">Placa *</label>
                    <input id="placa" name="placa" class="form-control" maxlength="12" required
                           value="<%= esc(editar == null ? "" : editar.getPlaca()) %>">
                </div>
                <div class="col-md-4">
                    <label for="marca" class="form-label">Marca *</label>
                    <input id="marca" name="marca" class="form-control" maxlength="60" required
                           value="<%= esc(editar == null ? "" : editar.getMarca()) %>">
                </div>
                <div class="col-md-4">
                    <label for="modelo" class="form-label">Modelo *</label>
                    <input id="modelo" name="modelo" class="form-control" maxlength="80" required
                           value="<%= esc(editar == null ? "" : editar.getModelo()) %>">
                </div>
                <div class="col-md-4">
                    <label for="anio" class="form-label">Año</label>
                    <input id="anio" name="anio" type="number" class="form-control" min="1950" max="2100"
                           value="<%= editar == null || editar.getAnio() == null ? "" : editar.getAnio() %>">
                </div>
                <div class="col-md-4">
                    <label for="estado" class="form-label">Estado</label>
                    <select id="estado" name="estado" class="form-select" required>
                        <option value="Operativo" <%= editar == null || "Operativo".equals(editar.getEstado()) ? "selected" : "" %>>Operativo</option>
                        <option value="En mantenimiento" <%= editar != null && "En mantenimiento".equals(editar.getEstado()) ? "selected" : "" %>>En mantenimiento</option>
                        <option value="Inactivo" <%= editar != null && "Inactivo".equals(editar.getEstado()) ? "selected" : "" %>>Inactivo</option>
                    </select>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><%= editar == null ? "Registrar" : "Guardar cambios" %></button>
                    <% if (editar != null) { %>
                    <a href="VehiculoServlet" class="btn btn-outline-secondary">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Vehículos registrados</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>ID</th><th>Placa</th><th>Marca</th><th>Modelo</th><th>Año</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                <tbody>
                <% if (vehiculos == null || vehiculos.isEmpty()) { %>
                    <tr><td colspan="7" class="text-center text-secondary py-4">No hay vehículos registrados.</td></tr>
                <% } else {
                    for (Vehiculo vehiculo : vehiculos) { %>
                    <tr>
                        <td><%= vehiculo.getIdVehiculo() %></td>
                        <td><%= esc(vehiculo.getPlaca()) %></td>
                        <td><%= esc(vehiculo.getMarca()) %></td>
                        <td><%= esc(vehiculo.getModelo()) %></td>
                        <td><%= vehiculo.getAnio() == null ? "—" : vehiculo.getAnio() %></td>
                        <td><span class="badge text-bg-<%= "Operativo".equals(vehiculo.getEstado()) ? "success" : "secondary" %>"><%= esc(vehiculo.getEstado()) %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="VehiculoServlet?editar=<%= vehiculo.getIdVehiculo() %>">Editar</a>
                            <% if (!"Inactivo".equals(vehiculo.getEstado())) { %>
                            <form action="VehiculoServlet" method="post" class="d-inline"
                                  onsubmit="return confirm('¿Inactivar este vehículo?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="idVehiculo" value="<%= vehiculo.getIdVehiculo() %>">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Inactivar</button>
                            </form>
                            <% } %>
                        </td>
                    </tr>
                <%  }
                   } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
