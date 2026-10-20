<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Proveedor" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    Proveedor editar = (Proveedor) request.getAttribute("proveedorEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Proveedores | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<%@ include file="/WEB-INF/jspf/fleet-navigation.jspf" %>
<main class="container py-4 fleet-page-main">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Proveedores</h1>
            <p class="text-secondary mb-0">Administración de proveedores de repuestos y servicios</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white"><h2 class="h5 mb-0"><%= editar == null ? "Registrar proveedor" : "Editar proveedor" %></h2></div>
        <div class="card-body">
            <form action="ProveedorServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %><input type="hidden" name="idProveedor" value="<%= editar.getIdProveedor() %>"><% } %>
                <div class="col-md-6">
                    <label for="razonSocial" class="form-label">Razón social *</label>
                    <input id="razonSocial" name="razonSocial" class="form-control" maxlength="150" required value="<%= esc(editar == null ? "" : editar.getRazonSocial()) %>">
                </div>
                <div class="col-md-3">
                    <label for="tipoProveedor" class="form-label">Tipo de proveedor *</label>
                    <select id="tipoProveedor" name="tipoProveedor" class="form-select" required>
                        <option value="">Selecciona un tipo</option>
                        <option value="TALLER" <%= editar != null && "TALLER".equals(editar.getTipoProveedor()) ? "selected" : "" %>>Taller</option>
                        <option value="REPUESTOS" <%= editar != null && "REPUESTOS".equals(editar.getTipoProveedor()) ? "selected" : "" %>>Repuestos</option>
                        <option value="AMBOS" <%= editar != null && "AMBOS".equals(editar.getTipoProveedor()) ? "selected" : "" %>>Ambos</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label for="numeroDocumento" class="form-label">Número de documento</label>
                    <input id="numeroDocumento" name="numeroDocumento" class="form-control" maxlength="20" value="<%= esc(editar == null ? "" : editar.getNumeroDocumento()) %>">
                </div>
                <div class="col-md-4">
                    <label for="contacto" class="form-label">Contacto</label>
                    <input id="contacto" name="contacto" class="form-control" maxlength="100" value="<%= esc(editar == null ? "" : editar.getContacto()) %>">
                </div>
                <div class="col-md-4">
                    <label for="correo" class="form-label">Correo</label>
                    <input id="correo" name="correo" type="email" class="form-control" maxlength="150" value="<%= esc(editar == null ? "" : editar.getCorreo()) %>">
                </div>
                <div class="col-md-4">
                    <label for="telefono" class="form-label">Teléfono</label>
                    <input id="telefono" name="telefono" class="form-control" maxlength="30" value="<%= esc(editar == null ? "" : editar.getTelefono()) %>">
                </div>
                <div class="col-md-9">
                    <label for="direccion" class="form-label">Dirección</label>
                    <input id="direccion" name="direccion" class="form-control" maxlength="250" value="<%= esc(editar == null ? "" : editar.getDireccion()) %>">
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
                    <% if (editar != null) { %><a href="ProveedorServlet" class="btn btn-outline-secondary">Cancelar</a><% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Proveedores registrados</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>ID</th><th>Razón social</th><th>Tipo</th><th>Documento</th><th>Contacto</th><th>Correo</th><th>Teléfono</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                <tbody>
                <% if (proveedores == null || proveedores.isEmpty()) { %>
                    <tr><td colspan="9" class="text-center text-secondary py-4">No hay proveedores registrados.</td></tr>
                <% } else { for (Proveedor proveedor : proveedores) { %>
                    <tr>
                        <td><%= proveedor.getIdProveedor() %></td>
                        <td><%= esc(proveedor.getRazonSocial()) %></td>
                        <td><%= esc(proveedor.getTipoProveedor()) %></td>
                        <td><%= esc(proveedor.getNumeroDocumento()) %></td>
                        <td><%= esc(proveedor.getContacto()) %></td>
                        <td><%= esc(proveedor.getCorreo()) %></td>
                        <td><%= esc(proveedor.getTelefono()) %></td>
                        <td><span class="badge text-bg-<%= "Activo".equals(proveedor.getEstado()) ? "success" : "secondary" %>"><%= esc(proveedor.getEstado()) %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="ProveedorServlet?editar=<%= proveedor.getIdProveedor() %>">Editar</a>
                            <% if ("Activo".equals(proveedor.getEstado())) { %>
                            <form action="ProveedorServlet" method="post" class="d-inline" onsubmit="return confirm('¿Inactivar este proveedor?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="idProveedor" value="<%= proveedor.getIdProveedor() %>">
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
