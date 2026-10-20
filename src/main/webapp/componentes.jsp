<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Componente" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>
<%
    List<Componente> componentes = (List<Componente>) request.getAttribute("componentes");
    Componente editar = (Componente) request.getAttribute("componenteEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Componentes | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Componentes</h1>
            <p class="text-secondary mb-0">Catálogo de componentes de la flota</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white">
            <h2 class="h5 mb-0"><%= editar == null ? "Registrar componente" : "Editar componente" %></h2>
        </div>
        <div class="card-body">
            <form action="ComponenteServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %>
                <input type="hidden" name="idComponente" value="<%= editar.getIdComponente() %>">
                <% } %>
                <div class="col-md-6">
                    <label for="nombre" class="form-label">Nombre *</label>
                    <input id="nombre" name="nombre" class="form-control" maxlength="80" required
                           value="<%= esc(editar == null ? "" : editar.getNombre()) %>">
                </div>
                <div class="col-md-6">
                    <label for="estado" class="form-label">Estado</label>
                    <select id="estado" name="estado" class="form-select" required>
                        <option value="Activo" <%= editar == null || "Activo".equals(editar.getEstado()) ? "selected" : "" %>>Activo</option>
                        <option value="Inactivo" <%= editar != null && "Inactivo".equals(editar.getEstado()) ? "selected" : "" %>>Inactivo</option>
                    </select>
                </div>
                <div class="col-12">
                    <label for="descripcion" class="form-label">Descripción</label>
                    <textarea id="descripcion" name="descripcion" class="form-control" maxlength="250" rows="2"><%= esc(editar == null ? "" : editar.getDescripcion()) %></textarea>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary"><%= editar == null ? "Registrar" : "Guardar cambios" %></button>
                    <% if (editar != null) { %>
                    <a href="ComponenteServlet" class="btn btn-outline-secondary">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Componentes registrados</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                <tbody>
                <% if (componentes == null || componentes.isEmpty()) { %>
                    <tr><td colspan="5" class="text-center text-secondary py-4">No hay componentes registrados.</td></tr>
                <% } else {
                    for (Componente componente : componentes) { %>
                    <tr>
                        <td><%= componente.getIdComponente() %></td>
                        <td><%= esc(componente.getNombre()) %></td>
                        <td><%= esc(componente.getDescripcion()) %></td>
                        <td><span class="badge text-bg-<%= "Activo".equals(componente.getEstado()) ? "success" : "secondary" %>"><%= esc(componente.getEstado()) %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="ComponenteServlet?editar=<%= componente.getIdComponente() %>">Editar</a>
                            <% if ("Activo".equals(componente.getEstado())) { %>
                            <form action="ComponenteServlet" method="post" class="d-inline"
                                  onsubmit="return confirm('¿Inactivar este componente?');">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="idComponente" value="<%= componente.getIdComponente() %>">
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
