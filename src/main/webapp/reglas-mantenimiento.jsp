<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,modelo.Componente,modelo.ReglaMantenimiento" %>
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
    List<ReglaMantenimiento> reglas = (List<ReglaMantenimiento>) request.getAttribute("reglas");
    ReglaMantenimiento editar = (ReglaMantenimiento) request.getAttribute("reglaEditar");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Reglas de mantenimiento | FleetPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Reglas de mantenimiento</h1>
            <p class="text-secondary mb-0">Configura los intervalos preventivos por componente</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <% if (componentes == null || componentes.isEmpty()) { %>
    <div class="alert alert-warning">Registra primero un componente para poder crear reglas.</div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white">
            <h2 class="h5 mb-0"><%= editar == null ? "Registrar regla" : "Editar regla" %></h2>
        </div>
        <div class="card-body">
            <form action="ReglaMantenimientoServlet" method="post" class="row g-3">
                <input type="hidden" name="accion" value="<%= editar == null ? "crear" : "editar" %>">
                <% if (editar != null) { %>
                <input type="hidden" name="idRegla" value="<%= editar.getIdRegla() %>">
                <% } %>
                <div class="col-md-6">
                    <label for="idComponente" class="form-label">Componente *</label>
                    <select id="idComponente" name="idComponente" class="form-select" required>
                        <option value="">Selecciona un componente</option>
                        <% if (componentes != null) {
                            for (Componente componente : componentes) { %>
                        <option value="<%= componente.getIdComponente() %>"
                                <%= editar != null && editar.getIdComponente() == componente.getIdComponente() ? "selected" : "" %>>
                            <%= esc(componente.getNombre()) %> (<%= esc(componente.getEstado()) %>)
                        </option>
                        <%  }
                           } %>
                    </select>
                </div>
                <div class="col-md-6">
                    <label for="nombre" class="form-label">Nombre de la regla *</label>
                    <input id="nombre" name="nombre" class="form-control" maxlength="100" required
                           value="<%= esc(editar == null ? "" : editar.getNombre()) %>">
                </div>
                <div class="col-md-4">
                    <label for="tipoMetrica" class="form-label">Métrica *</label>
                    <select id="tipoMetrica" name="tipoMetrica" class="form-select" required>
                        <option value="KILOMETRAJE" <%= editar == null || "KILOMETRAJE".equals(editar.getTipoMetrica()) ? "selected" : "" %>>Kilometraje</option>
                        <option value="HORAS_MOTOR" <%= editar != null && "HORAS_MOTOR".equals(editar.getTipoMetrica()) ? "selected" : "" %>>Horas de motor</option>
                        <option value="DIAS" <%= editar != null && "DIAS".equals(editar.getTipoMetrica()) ? "selected" : "" %>>Días</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label for="intervalo" class="form-label">Intervalo *</label>
                    <input id="intervalo" name="intervalo" type="number" class="form-control"
                           min="0.01" step="0.01" required
                           value="<%= editar == null ? "" : editar.getIntervalo().toPlainString() %>">
                </div>
                <div class="col-md-4">
                    <label for="activo" class="form-label">Estado</label>
                    <select id="activo" name="activo" class="form-select" required>
                        <option value="true" <%= editar == null || editar.isActivo() ? "selected" : "" %>>Activa</option>
                        <option value="false" <%= editar != null && !editar.isActivo() ? "selected" : "" %>>Inactiva</option>
                    </select>
                </div>
                <div class="col-12">
                    <label for="descripcion" class="form-label">Descripción</label>
                    <textarea id="descripcion" name="descripcion" class="form-control" maxlength="300" rows="2"><%= esc(editar == null ? "" : editar.getDescripcion()) %></textarea>
                </div>
                <div class="col-12 d-flex gap-2">
                    <button type="submit" class="btn btn-primary" <%= componentes == null || componentes.isEmpty() ? "disabled" : "" %>>
                        <%= editar == null ? "Registrar" : "Guardar cambios" %>
                    </button>
                    <% if (editar != null) { %>
                    <a href="ReglaMantenimientoServlet" class="btn btn-outline-secondary">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Reglas registradas</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>ID</th><th>Componente</th><th>Regla</th><th>Métrica</th><th>Intervalo</th><th>Descripción</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
                <tbody>
                <% if (reglas == null || reglas.isEmpty()) { %>
                    <tr><td colspan="8" class="text-center text-secondary py-4">No hay reglas registradas.</td></tr>
                <% } else {
                    for (ReglaMantenimiento regla : reglas) { %>
                    <tr>
                        <td><%= regla.getIdRegla() %></td>
                        <td><%= esc(regla.getNombreComponente()) %></td>
                        <td><%= esc(regla.getNombre()) %></td>
                        <td><%= "KILOMETRAJE".equals(regla.getTipoMetrica()) ? "Kilometraje" : "HORAS_MOTOR".equals(regla.getTipoMetrica()) ? "Horas de motor" : "Días" %></td>
                        <td><%= regla.getIntervalo().toPlainString() %></td>
                        <td><%= esc(regla.getDescripcion()) %></td>
                        <td><span class="badge text-bg-<%= regla.isActivo() ? "success" : "secondary" %>"><%= regla.isActivo() ? "Activa" : "Inactiva" %></span></td>
                        <td class="text-end text-nowrap">
                            <a class="btn btn-sm btn-outline-primary" href="ReglaMantenimientoServlet?editar=<%= regla.getIdRegla() %>">Editar</a>
                            <% if (regla.isActivo()) { %>
                            <form action="ReglaMantenimientoServlet" method="post" class="d-inline"
                                  onsubmit="return confirm('¿Desactivar esta regla?');">
                                <input type="hidden" name="accion" value="desactivar">
                                <input type="hidden" name="idRegla" value="<%= regla.getIdRegla() %>">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Desactivar</button>
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
