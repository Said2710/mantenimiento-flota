<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.time.format.DateTimeFormatter,modelo.RegistroOperacion,modelo.Vehiculo" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    List<RegistroOperacion> registros = (List<RegistroOperacion>) request.getAttribute("registros");
    List<Vehiculo> vehiculos = (List<Vehiculo>) request.getAttribute("vehiculos");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lecturas de operación | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Lecturas de operación</h1>
            <p class="text-secondary mb-0">Registra kilometraje y horas de motor por vehículo</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <section class="card shadow-sm mb-4">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Registrar lectura</h2></div>
        <div class="card-body">
            <% if (vehiculos == null || vehiculos.isEmpty()) { %>
            <p class="mb-0 text-secondary">No hay vehículos operativos para registrar lecturas.</p>
            <% } else { %>
            <form action="RegistroOperacionServlet" method="post" class="row g-3">
                <div class="col-md-4">
                    <label for="idVehiculo" class="form-label">Vehículo *</label>
                    <select id="idVehiculo" name="idVehiculo" class="form-select" required>
                        <option value="">Selecciona una unidad</option>
                        <% for (Vehiculo vehiculo : vehiculos) { %>
                        <option value="<%= vehiculo.getIdVehiculo() %>"><%= esc(vehiculo.getPlaca()) %> — <%= esc(vehiculo.getMarca()) %> <%= esc(vehiculo.getModelo()) %></option>
                        <% } %>
                    </select>
                </div>
                <div class="col-md-4">
                    <label for="kilometraje" class="form-label">Kilometraje *</label>
                    <input id="kilometraje" name="kilometraje" type="number" class="form-control" min="0" step="0.01" required>
                </div>
                <div class="col-md-4">
                    <label for="horasMotor" class="form-label">Horas de motor *</label>
                    <input id="horasMotor" name="horasMotor" type="number" class="form-control" min="0" step="0.01" required>
                </div>
                <div class="col-12">
                    <label for="observacion" class="form-label">Observación</label>
                    <textarea id="observacion" name="observacion" class="form-control" maxlength="300" rows="2"></textarea>
                </div>
                <div class="col-12"><button type="submit" class="btn btn-primary">Registrar lectura</button></div>
            </form>
            <% } %>
        </div>
    </section>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Lecturas recientes</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>Fecha</th><th>Vehículo</th><th>Kilometraje</th><th>Horas de motor</th><th>Observación</th><th>Registrado por</th></tr></thead>
                <tbody>
                <% if (registros == null || registros.isEmpty()) { %>
                    <tr><td colspan="6" class="text-center text-secondary py-4">Todavía no hay lecturas registradas.</td></tr>
                <% } else { for (RegistroOperacion registro : registros) { %>
                    <tr>
                        <td><%= registro.getFechaHora() == null ? "—" : registro.getFechaHora().format(formato) %></td>
                        <td><%= esc(registro.getPlaca()) %></td>
                        <td><%= registro.getKilometraje().toPlainString() %> km</td>
                        <td><%= registro.getHorasMotor().toPlainString() %> h</td>
                        <td><%= esc(registro.getObservacion()) %></td>
                        <td><%= esc(registro.getUsuarioRegistro()) %></td>
                    </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
