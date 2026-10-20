<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.time.format.DateTimeFormatter,modelo.EvaluacionMantenimiento" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String textoMetrica(String metrica) {
        if ("KILOMETRAJE".equals(metrica)) return "km";
        if ("HORAS_MOTOR".equals(metrica)) return "h";
        if ("DIAS".equals(metrica)) return "días";
        return "";
    }

    private String textoEstado(String estado) {
        if ("AL_DIA".equals(estado)) return "Al día";
        if ("PROXIMO".equals(estado)) return "Próximo";
        if ("VENCIDO".equals(estado)) return "Vencido";
        if ("SIN_LECTURA".equals(estado)) return "Sin lectura";
        if ("SIN_HISTORIAL".equals(estado)) return "Sin historial";
        if ("SIN_BASE".equals(estado)) return "Falta dato del servicio";
        if ("LECTURA_INCONSISTENTE".equals(estado)) return "Lectura inconsistente";
        return "Sin datos";
    }

    private String colorEstado(String estado) {
        if ("AL_DIA".equals(estado)) return "success";
        if ("PROXIMO".equals(estado)) return "warning";
        if ("VENCIDO".equals(estado)) return "danger";
        return "secondary";
    }
%>
<%
    List<EvaluacionMantenimiento> evaluaciones = (List<EvaluacionMantenimiento>) request.getAttribute("evaluaciones");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Evaluación preventiva | Mantenimiento de Flota</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="assets/css/fleetpro.css">
</head>
<body class="bg-light">
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 mb-1">Evaluación preventiva</h1>
            <p class="text-secondary mb-0">Estado de las reglas según lecturas y servicios completados</p>
        </div>
        <a class="btn btn-outline-secondary" href="UsuarioServlet">Volver al panel</a>
    </div>

    <% if (mensaje != null) { %>
    <div class="alert alert-<%= esc(tipoMensaje == null ? "info" : tipoMensaje) %>" role="alert"><%= esc(mensaje) %></div>
    <% } %>

    <div class="alert alert-info">
        Una regla aparece como próxima cuando queda el 10 % o menos de su intervalo. Sin un servicio completado que sirva de referencia, se muestra como sin historial.
    </div>

    <section class="card shadow-sm">
        <div class="card-header bg-white"><h2 class="h5 mb-0">Estado por vehículo y regla</h2></div>
        <div class="table-responsive">
            <table class="table table-striped table-hover align-middle mb-0">
                <thead><tr><th>Vehículo</th><th>Componente</th><th>Regla</th><th>Métrica</th><th>Último servicio</th><th>Lectura actual</th><th>Restante</th><th>Estado</th></tr></thead>
                <tbody>
                <% if (evaluaciones == null || evaluaciones.isEmpty()) { %>
                    <tr><td colspan="8" class="text-center text-secondary py-4">No hay vehículos operativos con componentes y reglas activos para evaluar.</td></tr>
                <% } else { for (EvaluacionMantenimiento e : evaluaciones) { %>
                    <tr>
                        <td><%= esc(e.getPlaca()) %></td>
                        <td><%= esc(e.getNombreComponente()) %></td>
                        <td><%= esc(e.getNombreRegla()) %></td>
                        <td><%= e.getIntervalo().toPlainString() %> <%= textoMetrica(e.getTipoMetrica()) %></td>
                        <td><%= e.getFechaUltimoServicio() == null ? "—" : e.getFechaUltimoServicio().format(formato) %></td>
                        <td><%= e.getValorActual() == null ? "—" : e.getValorActual().toPlainString() + " " + textoMetrica(e.getTipoMetrica()) %></td>
                        <td><%= e.getValorRestante() == null ? "—" : e.getValorRestante().toPlainString() + " " + textoMetrica(e.getTipoMetrica()) %></td>
                        <td><span class="badge text-bg-<%= colorEstado(e.getEstado()) %>"><%= textoEstado(e.getEstado()) %></span></td>
                    </tr>
                <% } } %>
                </tbody>
            </table>
        </div>
    </section>
</main>
</body>
</html>
