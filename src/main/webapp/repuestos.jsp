<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Repuesto" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Repuesto> repuestos = (List<Repuesto>) request.getAttribute("repuestos");
    Repuesto editar = (Repuesto) request.getAttribute("repuestoEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Repuestos | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Repuestos</h1>
            <p class="text-secondary mb-0">Catálogo de repuestos para mantenimiento de la flota</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white"><h2 class="h5 mb-0"><%= editar == null ? "Registrar repuesto" : "Editar repuesto" %></h2></div>
        <div class="card-body">
            <form action="RepuestoServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %><input type="hidden" name="idRepuesto" value="<%= editar.getIdRepuesto() %>"><% } %>
                <div class="col-md-3">
                    <label for="codigo" class="form-label">Código *</label>
                    <input id="codigo" name="codigo" class="form-control" maxlength="40" required value="<%= esc(editar == null ? "" : editar.getCodigo()) %>">
                </div>
                <div class="col-md-5">
                    <label for="nombre" class="form-label">Nombre *</label>
                    <input id="nombre" name="nombre" class="form-control" maxlength="120" required value="<%= esc(editar == null ? "" : editar.getNombre()) %>">
                </div>
                <div class="col-md-2">
                    <label for="unidadMedida" class="form-label">Unidad *</label>
                    <input id="unidadMedida" name="unidadMedida" class="form-control" maxlength="20" required value="<%= esc(editar == null ? "" : editar.getUnidadMedida()) %>">
                </div>
                <div class="col-md-2">
                    <label for="stockMinimo" class="form-label">Stock mínimo</label>
                    <input id="stockMinimo" name="stockMinimo" type="number" class="form-control" min="0" step="0.01" required value="<%= editar == null || editar.getStockMinimo() == null ? "0" : editar.getStockMinimo().toPlainString() %>">
                </div>
                <div class="col-md-9">
                    <label for="descripcion" class="form-label">Descripción</label>
                    <textarea id="descripcion" name="descripcion" class="form-control" maxlength="300" rows="2"><%= esc(editar == null ? "" : editar.getDescripcion()) %></textarea>
                </div>
                <div class="col-md-3">
                    <label for="estado" class="form-label">Estado</label>
                    <select id="estado" name="estado" class="form-select" required>
                        <option value="Activo" <%= editar == null || "Activo".equals(editar.getEstado()) ? "selected" : "" %>>Activo</option>
                        <option value="Inactivo" <%= editar != null && "Inactivo".equals(editar.getEstado()) ? "selected" : "" %>>Inactivo</option>
                    </select>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><%= editar == null ? "Registrar" : "Guardar cambios" %></button>
                    <% if (editar != null) { %><a href="RepuestoServlet" class="btn btn-outline-secondary">Cancelar</a><% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Repuestos registrados</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>ID</th><th>Código</th><th>Nombre</th><th>Descripción</th><th>Unidad</th><th>Stock mínimo</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                <tbody>
                <% if (repuestos == null || repuestos.isEmpty()) { %>
                    <tr><td colspan="8" class="text-center text-secondary py-4">No hay repuestos registrados.</td></tr>
                <% } else { for (Repuesto repuesto : repuestos) { %>
                    <tr>
                        <td><%= repuesto.getIdRepuesto() %></td>
                        <td><%= esc(repuesto.getCodigo()) %></td>
                        <td><%= esc(repuesto.getNombre()) %></td>
                        <td><%= esc(repuesto.getDescripcion()) %></td>
                        <td><%= esc(repuesto.getUnidadMedida()) %></td>
                        <td><%= repuesto.getStockMinimo() == null ? "0" : repuesto.getStockMinimo().toPlainString() %></td>
                        <td><span class="badge text-bg-<%= "Activo".equals(repuesto.getEstado()) ? "success" : "secondary" %>"><%= esc(repuesto.getEstado()) %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="RepuestoServlet?editar=<%= repuesto.getIdRepuesto() %>">Editar</a>
                            <% if ("Activo".equals(repuesto.getEstado())) { %>
                            <form action="RepuestoServlet" method="post" class="d-inline" onsubmit="return confirm('¿Inactivar este repuesto?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="idRepuesto" value="<%= repuesto.getIdRepuesto() %>">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Inactivar</button>
                            </form>
                            <% } %>
                        </td>
                    </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
