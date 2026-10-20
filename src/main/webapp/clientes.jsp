<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Cliente" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    Cliente editar = (Cliente) request.getAttribute("clienteEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
    String accion = editar == null ? "crear" : "editar";
    String tituloFormulario = editar == null ? "Registrar cliente" : "Editar cliente";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Clientes | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Clientes</h1>
            <p class="text-secondary mb-0">Administración de clientes de la flota</p>
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
            <h2 class="h5 mb-0"><%= tituloFormulario %></h2>
        </div>
        <div class="card-body">
            <form action="ClienteServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= accion %>">
                <% if (editar != null) { %>
                <input type="hidden" name="idCliente" value="<%= editar.getIdCliente() %>">
                <% } %>

                <div class="col-md-6">
                    <label for="razonSocial" class="form-label">Razón social *</label>
                    <input id="razonSocial" name="razonSocial" class="form-control" maxlength="150" required
                           value="<%= esc(editar == null ? "" : editar.getRazonSocial()) %>">
                </div>
                <div class="col-md-6">
                    <label for="numeroDocumento" class="form-label">Número de documento</label>
                    <input id="numeroDocumento" name="numeroDocumento" class="form-control" maxlength="20"
                           value="<%= esc(editar == null ? "" : editar.getNumeroDocumento()) %>">
                </div>
                <div class="col-md-6">
                    <label for="correo" class="form-label">Correo</label>
                    <input id="correo" name="correo" type="email" class="form-control" maxlength="150"
                           value="<%= esc(editar == null ? "" : editar.getCorreo()) %>">
                </div>
                <div class="col-md-6">
                    <label for="telefono" class="form-label">Teléfono</label>
                    <input id="telefono" name="telefono" class="form-control" maxlength="30"
                           value="<%= esc(editar == null ? "" : editar.getTelefono()) %>">
                </div>
                <div class="col-md-8">
                    <label for="direccion" class="form-label">Dirección</label>
                    <input id="direccion" name="direccion" class="form-control" maxlength="250"
                           value="<%= esc(editar == null ? "" : editar.getDireccion()) %>">
                </div>
                <div class="col-md-4">
                    <label for="estado" class="form-label">Estado</label>
                    <select id="estado" name="estado" class="form-select" required>
                        <option value="Activo" <%= editar == null || "Activo".equals(editar.getEstado()) ? "selected" : "" %>>Activo</option>
                        <option value="Inactivo" <%= editar != null && "Inactivo".equals(editar.getEstado()) ? "selected" : "" %>>Inactivo</option>
                    </select>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><%= editar == null ? "Registrar" : "Guardar cambios" %></button>
                    <% if (editar != null) { %>
                    <a href="ClienteServlet" class="btn btn-outline-secondary">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white">
            <h2 class="h5 mb-0">Clientes registrados</h2>
        </div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Razón social</th>
                        <th>Documento</th>
                        <th>Correo</th>
                        <th>Teléfono</th>
                        <th>Estado</th>
                        <th class="text-end">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                <% if (clientes == null || clientes.isEmpty()) { %>
                    <tr><td colspan="7" class="text-center text-secondary py-4">No hay clientes registrados.</td></tr>
                <% } else {
                    for (Cliente cliente : clientes) { %>
                    <tr>
                        <td><%= cliente.getIdCliente() %></td>
                        <td><%= esc(cliente.getRazonSocial()) %></td>
                        <td><%= esc(cliente.getNumeroDocumento()) %></td>
                        <td><%= esc(cliente.getCorreo()) %></td>
                        <td><%= esc(cliente.getTelefono()) %></td>
                        <td><span class="badge text-bg-<%= "Activo".equals(cliente.getEstado()) ? "success" : "secondary" %>"><%= esc(cliente.getEstado()) %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="ClienteServlet?editar=<%= cliente.getIdCliente() %>">Editar</a>
                            <% if ("Activo".equals(cliente.getEstado())) { %>
                            <form action="ClienteServlet" method="post" class="d-inline"
                                  onsubmit="return confirm('¿Inactivar este cliente?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="idCliente" value="<%= cliente.getIdCliente() %>">
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
