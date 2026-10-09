<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.time.format.DateTimeFormatter"%>
<%@page import="modelo.Usuario"%>
<%!
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Escapa texto para mostrarlo de forma segura dentro del HTML. */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    // El navegador no debe guardar esta página (al cerrar sesión no se puede volver con "Atrás")
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);

    String usuarioLogueado = (String) session.getAttribute("usuarioLogueado");
    if (usuarioLogueado == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    if (usuarios == null) {
        // Esta página se muestra desde UsuarioServlet, que carga los datos
        response.sendRedirect("UsuarioServlet");
        return;
    }

    int totalUsuarios = (Integer) request.getAttribute("totalUsuarios");
    int totalActivos = (Integer) request.getAttribute("totalActivos");
    int totalBloqueados = (Integer) request.getAttribute("totalBloqueados");
    int porcentajeActivos = (Integer) request.getAttribute("porcentajeActivos");
    String rolActual = (String) request.getAttribute("rolActual");
    String mensaje = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
    String inicialesActual = usuarioLogueado.substring(0, Math.min(2, usuarioLogueado.length())).toUpperCase();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>FleetPro – Gestión de usuarios</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

  <style>
    :root {
      --fleet-navy: #0F172A;
      --fleet-indigo: #312E81;
      --fleet-blue: #2563EB;
      --fleet-blue-hover: #1D4ED8;
      --fleet-orange: #F97316;
      --fleet-green: #10B981;
      --fleet-red: #DC2626;
      --fleet-bg: #F8FAFC;
      --fleet-border: #E2E8F0;
    }
    body { font-family: 'Plus Jakarta Sans', sans-serif; background-color: var(--fleet-bg); color: #1E293B; min-height: 100vh; overflow-x: hidden; }

    .sidebar { width: 270px; min-height: 100vh; background: linear-gradient(185deg, var(--fleet-navy) 0%, var(--fleet-indigo) 100%); color: #ffffff; display: flex; flex-direction: column; position: fixed; left: 0; top: 0; bottom: 0; z-index: 1030; box-shadow: 4px 0 24px rgba(15, 23, 42, 0.15); }
    .main-wrapper { margin-left: 270px; min-height: 100vh; display: flex; flex-direction: column; }
    @media (max-width: 991.98px) {
      .sidebar { position: static; width: 100%; min-height: auto; box-shadow: none; }
      .main-wrapper { margin-left: 0; }
    }

    .brand-logo-icon { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, var(--fleet-blue) 0%, var(--fleet-orange) 100%); display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4); }

    .nav-item-custom { display: flex; align-items: center; padding: 0.75rem 1rem; border-radius: 10px; color: #94A3B8; text-decoration: none; font-weight: 500; font-size: 0.93rem; transition: all 0.2s ease; margin-bottom: 0.35rem; }
    .nav-item-custom:hover:not(.disabled) { color: #ffffff; background: rgba(255, 255, 255, 0.08); }
    .nav-item-custom.active { color: #ffffff; background: linear-gradient(90deg, rgba(37, 99, 235, 0.9) 0%, rgba(49, 46, 129, 0.7) 100%); font-weight: 600; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3); border-left: 4px solid var(--fleet-orange); }
    .nav-item-custom.disabled { color: #64748B; cursor: not-allowed; opacity: 0.7; }
    .badge-soon { background-color: rgba(249, 115, 22, 0.18); color: #FDBA74; border: 1px solid rgba(249, 115, 22, 0.3); font-size: 0.68rem; padding: 0.2rem 0.5rem; border-radius: 6px; font-weight: 600; }

    .btn-logout { background: rgba(220, 38, 38, 0.12); color: #FCA5A5; border: 1px solid rgba(220, 38, 38, 0.25); border-radius: 10px; padding: 0.7rem 1rem; font-weight: 600; transition: all 0.2s ease; display: flex; align-items: center; justify-content: center; gap: 0.5rem; text-decoration: none; }
    .btn-logout:hover { background: rgba(220, 38, 38, 0.22); color: #ffffff; border-color: rgba(220, 38, 38, 0.5); }

    .topbar { background: #ffffff; border-bottom: 1px solid var(--fleet-border); padding: 1.1rem 2rem; position: sticky; top: 0; z-index: 1020; }
    .avatar-admin { width: 42px; height: 42px; border-radius: 10px; background: linear-gradient(135deg, #EEF2F6 0%, #E2E8F0 100%); border: 2px solid var(--fleet-border); color: var(--fleet-indigo); font-weight: 700; display: flex; align-items: center; justify-content: center; font-size: 0.95rem; }

    .metric-card { background: #ffffff; border: 1px solid var(--fleet-border); border-radius: 14px; padding: 1.35rem; box-shadow: 0 2px 10px rgba(15, 23, 42, 0.03); position: relative; overflow: hidden; }
    .metric-card::after { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px; }
    .metric-card.card-blue::after { background: var(--fleet-blue); }
    .metric-card.card-green::after { background: var(--fleet-green); }
    .metric-card.card-orange::after { background: var(--fleet-orange); }
    .icon-badge { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.35rem; }
    .badge-icon-blue { background: rgba(37, 99, 235, 0.1); color: var(--fleet-blue); }
    .badge-icon-green { background: rgba(16, 185, 129, 0.1); color: var(--fleet-green); }
    .badge-icon-orange { background: rgba(249, 115, 22, 0.1); color: var(--fleet-orange); }

    .btn-fleet-primary { background-color: var(--fleet-blue); border-color: var(--fleet-blue); color: #ffffff; font-weight: 600; border-radius: 10px; padding: 0.55rem 1.25rem; box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25); transition: all 0.2s ease; }
    .btn-fleet-primary:hover, .btn-fleet-primary:focus { background-color: var(--fleet-blue-hover); border-color: var(--fleet-blue-hover); color: #ffffff; box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35); }
    .btn-action-sm { width: 32px; height: 32px; padding: 0; display: inline-flex; align-items: center; justify-content: center; border-radius: 8px; font-size: 0.88rem; }

    .table-container { background: #ffffff; border: 1px solid var(--fleet-border); border-radius: 14px; overflow: hidden; box-shadow: 0 2px 12px rgba(15, 23, 42, 0.03); }
    .table-fleet thead th { background-color: #F8FAFC; color: #64748B; font-weight: 600; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.6px; padding: 0.95rem 1.25rem; border-bottom: 1px solid var(--fleet-border); }
    .table-fleet tbody td { padding: 1rem 1.25rem; vertical-align: middle; color: #334155; font-size: 0.88rem; border-bottom: 1px solid #F1F5F9; }
    .table-fleet tbody tr:hover { background-color: #F8FAFC; }

    .pill-active, .pill-inactive, .pill-blocked, .pill-normal { font-weight: 600; padding: 0.35rem 0.7rem; border-radius: 20px; font-size: 0.76rem; display: inline-flex; align-items: center; gap: 0.35rem; }
    .pill-active { background-color: rgba(16, 185, 129, 0.1); color: #059669; }
    .pill-inactive { background-color: #F1F5F9; color: #64748B; }
    .pill-blocked { background-color: rgba(220, 38, 38, 0.1); color: var(--fleet-red); }
    .pill-normal { background-color: #F1F5F9; color: #475569; font-weight: 500; }

    .modal-content { border: none; border-radius: 16px; box-shadow: 0 20px 40px rgba(15, 23, 42, 0.15); }
    .modal-header { border-bottom: 1px solid var(--fleet-border); padding: 1.25rem 1.75rem; }
    .modal-body { padding: 1.5rem 1.75rem; }
    .modal-footer { border-top: 1px solid var(--fleet-border); padding: 1rem 1.75rem; }
    .form-control, .form-select { border-radius: 8px; border-color: var(--fleet-border); padding: 0.65rem 0.9rem; font-size: 0.9rem; }
    .form-control:focus, .form-select:focus { border-color: var(--fleet-blue); box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15); }
  </style>
</head>
<body>

  <!-- Cabecera móvil -->
  <div class="d-lg-none text-white p-3 d-flex justify-content-between align-items-center" style="background: var(--fleet-navy);">
    <div class="d-flex align-items-center gap-2">
      <div class="brand-logo-icon" style="width: 36px; height: 36px;"><i class="bi bi-truck text-white fs-5"></i></div>
      <div>
        <div class="fw-bold fs-6">FleetPro</div>
        <div class="text-white-50 small" style="font-size: 0.7rem;">Control de Flota</div>
      </div>
    </div>
    <button class="btn btn-outline-light btn-sm" type="button" data-bs-toggle="collapse" data-bs-target="#mobileSidebarNav" aria-label="Abrir menú">
      <i class="bi bi-list fs-5"></i>
    </button>
  </div>

  <!-- Barra lateral (escritorio) -->
  <aside class="sidebar d-none d-lg-flex" id="desktopSidebar">
    <div class="p-4 d-flex align-items-center gap-3 border-bottom border-light border-opacity-10">
      <div class="brand-logo-icon"><i class="bi bi-truck text-white fs-4"></i></div>
      <div>
        <h5 class="fw-bold mb-0 text-white">FleetPro</h5>
        <span class="text-white-50" style="font-size: 0.75rem; font-weight: 500;">Control &amp; Mantenimiento</span>
      </div>
    </div>

    <div class="p-3 flex-grow-1">
      <div class="text-white-50 small fw-bold px-3 mb-2 text-uppercase" style="font-size: 0.7rem; letter-spacing: 0.8px;">Menú principal</div>
      <nav class="nav flex-column">
        <a href="UsuarioServlet" class="nav-item-custom active">
          <i class="bi bi-people-fill fs-5 me-3 text-warning"></i><span>Usuarios</span>
        </a>
        <a href="#" class="nav-item-custom disabled" tabindex="-1" aria-disabled="true">
          <i class="bi bi-truck-front fs-5 me-3"></i><span class="flex-grow-1">Vehículos</span><span class="badge badge-soon">Pronto</span>
        </a>
        <a href="#" class="nav-item-custom disabled" tabindex="-1" aria-disabled="true">
          <i class="bi bi-tools fs-5 me-3"></i><span class="flex-grow-1">Mantenimiento</span><span class="badge badge-soon">Pronto</span>
        </a>
        <a href="#" class="nav-item-custom disabled" tabindex="-1" aria-disabled="true">
          <i class="bi bi-bar-chart-line fs-5 me-3"></i><span class="flex-grow-1">Reportes</span><span class="badge badge-soon">Pronto</span>
        </a>
      </nav>
    </div>

    <div class="p-3 border-top border-light border-opacity-10">
      <a href="LogoutServlet" class="btn-logout">
        <i class="bi bi-box-arrow-left fs-5"></i><span>Cerrar sesión</span>
      </a>
    </div>
  </aside>

  <!-- Menú móvil desplegable -->
  <div class="collapse d-lg-none" id="mobileSidebarNav" style="background: linear-gradient(185deg, var(--fleet-navy) 0%, var(--fleet-indigo) 100%);">
    <div class="p-3 border-bottom border-light border-opacity-10">
      <nav class="nav flex-column">
        <a href="UsuarioServlet" class="nav-item-custom active">
          <i class="bi bi-people-fill fs-5 me-3 text-warning"></i><span>Usuarios</span>
        </a>
        <a href="#" class="nav-item-custom disabled">
          <i class="bi bi-truck-front fs-5 me-3"></i><span class="flex-grow-1">Vehículos</span><span class="badge badge-soon">Pronto</span>
        </a>
        <a href="#" class="nav-item-custom disabled">
          <i class="bi bi-tools fs-5 me-3"></i><span class="flex-grow-1">Mantenimiento</span><span class="badge badge-soon">Pronto</span>
        </a>
        <a href="#" class="nav-item-custom disabled">
          <i class="bi bi-bar-chart-line fs-5 me-3"></i><span class="flex-grow-1">Reportes</span><span class="badge badge-soon">Pronto</span>
        </a>
      </nav>
      <div class="mt-3 pt-3 border-top border-light border-opacity-10">
        <a href="LogoutServlet" class="btn-logout">
          <i class="bi bi-box-arrow-left fs-5"></i><span>Cerrar sesión</span>
        </a>
      </div>
    </div>
  </div>

  <div class="main-wrapper">
    <!-- Barra superior -->
    <header class="topbar d-flex justify-content-between align-items-center">
      <div>
        <h4 class="fw-bold mb-1" style="color: #0F172A;">Gestión de usuarios</h4>
        <p class="text-muted small mb-0">Administra cuentas, roles y estados de acceso al sistema</p>
      </div>
      <div class="d-flex align-items-center gap-3">
        <div class="text-end d-none d-sm-block">
          <div class="fw-bold small text-dark"><%= esc(usuarioLogueado) %></div>
          <div class="text-muted" style="font-size: 0.75rem;"><%= esc(rolActual) %></div>
        </div>
        <div class="avatar-admin" title="<%= esc(usuarioLogueado) %>"><%= esc(inicialesActual) %></div>
      </div>
    </header>

    <main class="p-4 flex-grow-1">

      <% if (mensaje != null) { %>
      <!-- Mensaje de resultado de la última acción -->
      <div class="alert alert-<%= "success".equals(tipoMensaje) ? "success" : "danger" %> alert-dismissible fade show border-0 shadow-sm d-flex align-items-center mb-4" role="alert">
        <i class="bi <%= "success".equals(tipoMensaje) ? "bi-check-circle-fill" : "bi-exclamation-triangle-fill" %> fs-5 me-3"></i>
        <div><%= esc(mensaje) %></div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
      </div>
      <% } %>

      <!-- Tarjetas de resumen (datos reales) -->
      <div class="row g-3 mb-4">
        <div class="col-12 col-md-4">
          <div class="metric-card card-blue d-flex align-items-center justify-content-between">
            <div>
              <div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size: 0.75rem;">Total de usuarios</div>
              <div class="h2 fw-bold mb-0" style="color: #0F172A;"><%= totalUsuarios %></div>
              <div class="text-muted small mt-1" style="font-size: 0.78rem;"><i class="bi bi-people me-1 text-primary"></i>Registrados en la plataforma</div>
            </div>
            <div class="icon-badge badge-icon-blue"><i class="bi bi-people-fill"></i></div>
          </div>
        </div>

        <div class="col-12 col-md-4">
          <div class="metric-card card-green d-flex align-items-center justify-content-between">
            <div>
              <div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size: 0.75rem;">Usuarios activos</div>
              <div class="h2 fw-bold mb-0" style="color: #0F172A;"><%= totalActivos %></div>
              <div class="text-muted small mt-1" style="font-size: 0.78rem;"><i class="bi bi-check2-circle me-1 text-success"></i><%= porcentajeActivos %>% con acceso habilitado</div>
            </div>
            <div class="icon-badge badge-icon-green"><i class="bi bi-person-check-fill"></i></div>
          </div>
        </div>

        <div class="col-12 col-md-4">
          <div class="metric-card card-orange d-flex align-items-center justify-content-between">
            <div>
              <div class="text-muted small fw-semibold text-uppercase mb-1" style="font-size: 0.75rem;">Cuentas bloqueadas</div>
              <div class="h2 fw-bold mb-0" style="color: #0F172A;"><%= totalBloqueados %></div>
              <div class="text-muted small mt-1" style="font-size: 0.78rem;"><i class="bi bi-shield-exclamation me-1 text-danger"></i>Por intentos fallidos</div>
            </div>
            <div class="icon-badge badge-icon-orange"><i class="bi bi-lock-fill"></i></div>
          </div>
        </div>
      </div>

      <!-- Barra de herramientas -->
      <div class="bg-white p-3 rounded-3 border mb-4 shadow-sm" style="border-color: var(--fleet-border) !important;">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-md-5">
            <div class="input-group">
              <span class="input-group-text bg-white border-end-0 text-muted"><i class="bi bi-search"></i></span>
              <input type="text" id="inputBuscar" class="form-control border-start-0 ps-0" placeholder="Buscar por usuario..." onkeyup="filtrarTabla()">
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-4">
            <div class="d-flex align-items-center gap-2">
              <label for="filtroEstado" class="small text-muted fw-semibold mb-0 text-nowrap">Estado:</label>
              <select id="filtroEstado" class="form-select" onchange="filtrarTabla()">
                <option value="Todos">Todos</option>
                <option value="Activo">Activo</option>
                <option value="Inactivo">Inactivo</option>
              </select>
            </div>
          </div>
          <div class="col-12 col-sm-6 col-md-3 text-md-end">
            <button class="btn btn-fleet-primary w-100 d-inline-flex align-items-center justify-content-center gap-2" data-bs-toggle="modal" data-bs-target="#modalCrearUsuario">
              <i class="bi bi-plus-lg fs-6"></i><span>Nuevo usuario</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Tabla de usuarios (datos reales de la base) -->
      <div class="table-container mb-4">
        <div class="table-responsive">
          <table class="table table-fleet align-middle mb-0">
            <thead>
              <tr>
                <th scope="col" style="width: 70px;">ID</th>
                <th scope="col">Usuario</th>
                <th scope="col">Rol</th>
                <th scope="col">Estado</th>
                <th scope="col">Acceso</th>
                <th scope="col" class="text-center">Intentos fallidos</th>
                <th scope="col">Último acceso</th>
                <th scope="col" class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody id="tablaUsuarios">
              <% for (Usuario u : usuarios) {
                   boolean esYo = u.getNombreUsuario().equals(usuarioLogueado);
                   String ultimo = (u.getUltimoAcceso() != null) ? u.getUltimoAcceso().format(FMT) : "Nunca";
              %>
              <tr data-estado="<%= esc(u.getEstado()) %>" data-usuario="<%= esc(u.getNombreUsuario()) %>" class="<%= u.isBloqueado() ? "table-warning bg-opacity-10" : "" %>">
                <td class="fw-bold text-muted">#<%= u.getIdUsuario() %></td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="rounded-circle bg-light d-flex align-items-center justify-content-center fw-bold <%= u.isBloqueado() ? "text-danger" : "text-primary" %>" style="width: 32px; height: 32px; font-size: 0.8rem; border: 1px solid #CBD5E1;">
                      <%= esc(u.getIniciales()) %>
                    </div>
                    <div>
                      <span class="fw-bold text-dark d-block"><%= esc(u.getNombreUsuario()) %></span>
                      <% if (esYo) { %><small class="text-muted" style="font-size: 0.72rem;">Es usted</small><% } %>
                    </div>
                  </div>
                </td>
                <td><span class="badge text-bg-light border px-2 py-1 text-dark fw-semibold"><%= esc(u.getRolNombre()) %></span></td>
                <td>
                  <% if (u.isActivo()) { %>
                  <span class="pill-active"><i class="bi bi-dot fs-6"></i>Activo</span>
                  <% } else { %>
                  <span class="pill-inactive"><i class="bi bi-dot fs-6"></i><%= esc(u.getEstado()) %></span>
                  <% } %>
                </td>
                <td>
                  <% if (u.isBloqueado()) { %>
                  <span class="pill-blocked"><i class="bi bi-lock-fill"></i>Bloqueado</span>
                  <% } else { %>
                  <span class="pill-normal"><i class="bi bi-shield-check text-muted"></i>Normal</span>
                  <% } %>
                </td>
                <td class="text-center">
                  <% if (u.isBloqueado()) { %>
                  <span class="badge bg-danger-subtle text-danger border border-danger-subtle fw-bold"><%= u.getIntentosFallidos() %> (límite)</span>
                  <% } else { %>
                  <span class="badge bg-light text-secondary border"><%= u.getIntentosFallidos() %></span>
                  <% } %>
                </td>
                <td class="text-muted small"><%= ultimo %></td>
                <td class="text-end">
                  <div class="btn-group gap-1" role="group">
                    <button type="button" class="btn btn-sm btn-outline-primary btn-action-sm" title="Editar usuario"
                            onclick="abrirEditar(<%= u.getIdUsuario() %>, '<%= esc(u.getNombreUsuario()) %>', '<%= u.getRolId() %>', '<%= esc(u.getEstado()) %>')">
                      <i class="bi bi-pencil-fill"></i>
                    </button>
                    <% if (u.isBloqueado()) { %>
                    <form action="UsuarioServlet" method="POST" class="d-inline" onsubmit="return confirm('¿Desbloquear el acceso para este usuario?');">
                      <input type="hidden" name="accion" value="desbloquear">
                      <input type="hidden" name="idUsuario" value="<%= u.getIdUsuario() %>">
                      <button type="submit" class="btn btn-sm btn-action-sm" title="Desbloquear cuenta" style="background-color: #FFF7ED; border: 1px solid var(--fleet-orange); color: var(--fleet-orange);">
                        <i class="bi bi-unlock-fill"></i>
                      </button>
                    </form>
                    <% } %>
                    <button type="button" class="btn btn-sm btn-outline-danger btn-action-sm"
                            title="<%= esYo ? "No puede eliminar su propia cuenta" : "Eliminar usuario" %>"
                            <%= esYo ? "disabled" : "" %>
                            onclick="abrirEliminar(<%= u.getIdUsuario() %>, '<%= esc(u.getNombreUsuario()) %>')">
                      <i class="bi bi-trash-fill"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <% } %>
            </tbody>
          </table>
        </div>

        <!-- Estado vacío -->
        <div id="estadoVacio" class="text-center py-5 <%= usuarios.isEmpty() ? "" : "d-none" %>">
          <div class="mb-3">
            <div class="d-inline-flex align-items-center justify-content-center rounded-circle" style="width: 72px; height: 72px; background-color: #F1F5F9; color: #94A3B8;">
              <i class="bi bi-person-x fs-1"></i>
            </div>
          </div>
          <h5 class="fw-bold text-dark mb-1">Aún no hay usuarios registrados</h5>
          <p class="text-muted small mb-3">No se encontraron cuentas con los criterios seleccionados.</p>
          <button type="button" class="btn btn-sm btn-outline-primary" onclick="restablecerFiltros()">
            <i class="bi bi-arrow-clockwise me-1"></i>Restablecer filtros
          </button>
        </div>

        <div class="p-3 bg-light bg-opacity-50 border-top">
          <span class="text-muted small">Total: <strong><%= totalUsuarios %></strong> usuarios registrados</span>
        </div>
      </div>
    </main>
  </div>

  <!-- ================= MODALES ================= -->

  <!-- Nuevo usuario -->
  <div class="modal fade" id="modalCrearUsuario" tabindex="-1" aria-labelledby="modalCrearUsuarioLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <form action="UsuarioServlet" method="POST" autocomplete="off">
          <input type="hidden" name="accion" value="crear">

          <div class="modal-header">
            <div class="d-flex align-items-center gap-2">
              <div class="badge-icon-blue rounded-3 p-2 d-flex align-items-center justify-content-center" style="width: 36px; height: 36px;"><i class="bi bi-person-plus-fill fs-5"></i></div>
              <h5 class="modal-title fw-bold text-dark" id="modalCrearUsuarioLabel">Nuevo usuario</h5>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label for="crearNombreUsuario" class="form-label fw-semibold small text-dark">Nombre de usuario <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text bg-light text-muted"><i class="bi bi-person"></i></span>
                <input type="text" class="form-control" id="crearNombreUsuario" name="nombreUsuario" placeholder="ej. analista.flota" required minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]{3,50}" title="Letras, números, punto, guion o guion bajo (3 a 50 caracteres)">
              </div>
            </div>

            <div class="mb-3">
              <label for="crearContrasena" class="form-label fw-semibold small text-dark">Contraseña <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text bg-light text-muted"><i class="bi bi-key"></i></span>
                <input type="password" class="form-control" id="crearContrasena" name="contrasena" placeholder="Ingrese una contraseña segura" required minlength="8" maxlength="72" autocomplete="new-password">
                <button class="btn btn-outline-secondary" type="button" onclick="togglePasswordVisibility('crearContrasena', this)" aria-label="Mostrar u ocultar contraseña"><i class="bi bi-eye"></i></button>
              </div>
              <div class="form-text" style="font-size: 0.75rem;">Mínimo 8 caracteres, al menos un número o símbolo.</div>
            </div>

            <div class="row g-2">
              <div class="col-md-6 mb-3">
                <label for="crearRolId" class="form-label fw-semibold small text-dark">Rol <span class="text-danger">*</span></label>
                <select class="form-select" id="crearRolId" name="rol_id" required>
                  <option value="1">Administrador</option>
                  <option value="2" selected>Usuario</option>
                </select>
              </div>
              <div class="col-md-6 mb-3">
                <label for="crearEstado" class="form-label fw-semibold small text-dark">Estado <span class="text-danger">*</span></label>
                <select class="form-select" id="crearEstado" name="estado" required>
                  <option value="Activo" selected>Activo</option>
                  <option value="Inactivo">Inactivo</option>
                </select>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="submit" class="btn btn-fleet-primary"><i class="bi bi-check-lg me-1"></i>Guardar usuario</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Editar usuario -->
  <div class="modal fade" id="modalEditarUsuario" tabindex="-1" aria-labelledby="modalEditarUsuarioLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <form action="UsuarioServlet" method="POST" autocomplete="off">
          <input type="hidden" name="accion" value="editar">
          <input type="hidden" id="editIdUsuario" name="idUsuario" value="">

          <div class="modal-header">
            <div class="d-flex align-items-center gap-2">
              <div class="badge-icon-blue rounded-3 p-2 d-flex align-items-center justify-content-center" style="width: 36px; height: 36px;"><i class="bi bi-pencil-square fs-5"></i></div>
              <h5 class="modal-title fw-bold text-dark" id="modalEditarUsuarioLabel">Editar usuario</h5>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>

          <div class="modal-body">
            <div class="mb-3">
              <label for="editNombreUsuario" class="form-label fw-semibold small text-dark">Nombre de usuario <span class="text-danger">*</span></label>
              <div class="input-group">
                <span class="input-group-text bg-light text-muted"><i class="bi bi-person"></i></span>
                <input type="text" class="form-control" id="editNombreUsuario" name="nombreUsuario" required minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]{3,50}" title="Letras, números, punto, guion o guion bajo (3 a 50 caracteres)">
              </div>
            </div>

            <div class="mb-3">
              <label for="editContrasena" class="form-label fw-semibold small text-dark">Contraseña (opcional)</label>
              <div class="input-group">
                <span class="input-group-text bg-light text-muted"><i class="bi bi-key"></i></span>
                <input type="password" class="form-control" id="editContrasena" name="contrasena" placeholder="Dejar en blanco para mantener la actual" maxlength="72" autocomplete="new-password">
                <button class="btn btn-outline-secondary" type="button" onclick="togglePasswordVisibility('editContrasena', this)" aria-label="Mostrar u ocultar contraseña"><i class="bi bi-eye"></i></button>
              </div>
              <div class="form-text" style="font-size: 0.75rem;">Complete este campo solo si desea cambiar la contraseña.</div>
            </div>

            <div class="row g-2">
              <div class="col-md-6 mb-3">
                <label for="editRolId" class="form-label fw-semibold small text-dark">Rol <span class="text-danger">*</span></label>
                <select class="form-select" id="editRolId" name="rol_id" required>
                  <option value="1">Administrador</option>
                  <option value="2">Usuario</option>
                </select>
              </div>
              <div class="col-md-6 mb-3">
                <label for="editEstado" class="form-label fw-semibold small text-dark">Estado <span class="text-danger">*</span></label>
                <select class="form-select" id="editEstado" name="estado" required>
                  <option value="Activo">Activo</option>
                  <option value="Inactivo">Inactivo</option>
                </select>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-light">
            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="submit" class="btn btn-fleet-primary"><i class="bi bi-check-lg me-1"></i>Guardar cambios</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- Eliminar usuario -->
  <div class="modal fade" id="modalEliminarUsuario" tabindex="-1" aria-labelledby="modalEliminarUsuarioLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <form action="UsuarioServlet" method="POST">
          <input type="hidden" name="accion" value="eliminar">
          <input type="hidden" id="eliminarIdUsuario" name="idUsuario" value="">

          <div class="modal-header border-0 pb-0">
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
          </div>

          <div class="modal-body text-center px-4 pt-0 pb-4">
            <div class="d-inline-flex align-items-center justify-content-center rounded-circle mb-3" style="width: 68px; height: 68px; background-color: rgba(220, 38, 38, 0.1); color: var(--fleet-red);">
              <i class="bi bi-exclamation-triangle-fill fs-2"></i>
            </div>
            <h5 class="fw-bold text-dark mb-2" id="modalEliminarUsuarioLabel">¿Eliminar usuario?</h5>
            <p class="text-muted small mb-1">Está a punto de eliminar al usuario <strong id="eliminarNombreTexto" class="text-dark"></strong>.</p>
            <p class="text-danger small fw-semibold mb-0"><i class="bi bi-info-circle me-1"></i>Esta acción no se puede deshacer.</p>
          </div>

          <div class="modal-footer bg-light justify-content-center border-0 pt-0 pb-4">
            <button type="button" class="btn btn-outline-secondary px-4" data-bs-dismiss="modal">Cancelar</button>
            <button type="submit" class="btn btn-danger px-4" style="background-color: var(--fleet-red); border-color: var(--fleet-red);"><i class="bi bi-trash-fill me-1"></i>Eliminar</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    // Mostrar u ocultar la contraseña en los formularios
    function togglePasswordVisibility(inputId, btn) {
      var input = document.getElementById(inputId);
      var icon = btn.querySelector('i');
      if (input.type === 'password') {
        input.type = 'text';
        icon.classList.remove('bi-eye');
        icon.classList.add('bi-eye-slash');
      } else {
        input.type = 'password';
        icon.classList.remove('bi-eye-slash');
        icon.classList.add('bi-eye');
      }
    }

    // Abre el modal de edición con los datos del usuario
    function abrirEditar(id, usuario, rolId, estado) {
      document.getElementById('editIdUsuario').value = id;
      document.getElementById('editNombreUsuario').value = usuario;
      document.getElementById('editRolId').value = rolId;
      document.getElementById('editEstado').value = estado;
      document.getElementById('editContrasena').value = '';
      new bootstrap.Modal(document.getElementById('modalEditarUsuario')).show();
    }

    // Abre el modal de confirmación para eliminar
    function abrirEliminar(id, usuario) {
      document.getElementById('eliminarIdUsuario').value = id;
      document.getElementById('eliminarNombreTexto').textContent = usuario;
      new bootstrap.Modal(document.getElementById('modalEliminarUsuario')).show();
    }

    // Búsqueda y filtro por estado en la tabla
    function filtrarTabla() {
      var query = document.getElementById('inputBuscar').value.toLowerCase().trim();
      var estadoFiltro = document.getElementById('filtroEstado').value;
      var filas = document.querySelectorAll('#tablaUsuarios tr');
      var visibles = 0;

      filas.forEach(function (fila) {
        var usuario = fila.getAttribute('data-usuario').toLowerCase();
        var estado = fila.getAttribute('data-estado');
        var coincide = usuario.indexOf(query) !== -1 && (estadoFiltro === 'Todos' || estado === estadoFiltro);
        fila.style.display = coincide ? '' : 'none';
        if (coincide) { visibles++; }
      });

      document.getElementById('estadoVacio').classList.toggle('d-none', visibles > 0);
    }

    function restablecerFiltros() {
      document.getElementById('inputBuscar').value = '';
      document.getElementById('filtroEstado').value = 'Todos';
      filtrarTabla();
    }
  </script>
</body>
</html>