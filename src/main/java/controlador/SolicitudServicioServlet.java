package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.ClienteRepository;
import modelo.ClienteRepositoryJdbc;
import modelo.DetalleServicio;
import modelo.SolicitudServicio;
import modelo.SolicitudServicioRepository;
import modelo.SolicitudServicioRepositoryJdbc;
import modelo.Usuario;
import modelo.Vehiculo;
import modelo.VehiculoRepository;
import modelo.VehiculoRepositoryJdbc;

@WebServlet(name = "SolicitudServicioServlet", urlPatterns = {"/SolicitudServicioServlet"})
public class SolicitudServicioServlet extends AdministradorServletBase {

    private final SolicitudServicioRepository solicitudes = new SolicitudServicioRepositoryJdbc();
    private final ClienteRepository clientes = new ClienteRepositoryJdbc();
    private final VehiculoRepository vehiculos = new VehiculoRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario actual = validarAdministrador(request, response);
        if (actual == null) return;

        HttpSession session = request.getSession(false);
        List<SolicitudServicio> lista = solicitudes.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar el registro de solicitudes.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                SolicitudServicio solicitud = solicitudes.buscarPorId(Integer.parseInt(idEditar));
                if (solicitud != null && "Pendiente".equals(solicitud.getEstado())) {
                    request.setAttribute("solicitudEditar", solicitud);
                }
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador de la solicitud no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("solicitudes", lista);
        request.setAttribute("clientes", clientes.listar());
        request.setAttribute("vehiculos", vehiculos.listar());
        request.getRequestDispatcher("solicitudes-servicio.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Usuario actual = validarAdministrador(request, response);
        if (actual == null) return;
        HttpSession session = request.getSession(false);

        String accion = request.getParameter("accion");
        if ("crear".equals(accion)) guardar(request, session, actual, false);
        else if ("editar".equals(accion)) guardar(request, session, actual, true);
        else if ("aprobar".equals(accion)) cambiarEstado(request, session, "aprobar");
        else if ("convertir".equals(accion)) convertir(request, session, actual);
        else if ("anular".equals(accion)) cambiarEstado(request, session, "anular");
        else mensaje(session, "danger", "La acción solicitada no es válida.");
        response.sendRedirect("SolicitudServicioServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, Usuario actual, boolean editar) {
        SolicitudServicio solicitud = leerSolicitud(request);
        String error = validar(solicitud);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            Integer id = entero(request.getParameter("idSolicitud"));
            if (id == null) {
                mensaje(session, "danger", "El identificador de la solicitud no es válido.");
                return;
            }
            solicitud.setIdSolicitud(id);
            resultado = solicitudes.editar(solicitud);
        } else {
            resultado = solicitudes.crear(solicitud, actual.getIdUsuario());
        }
        if ("OK".equals(resultado)) mensaje(session, "success", editar ? "Solicitud actualizada." : "Solicitud registrada.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "Solo se pueden editar solicitudes pendientes.");
        else mensaje(session, "danger", "No se pudo guardar la solicitud. Revisa la conexión y las tablas de ventas.");
    }

    private SolicitudServicio leerSolicitud(HttpServletRequest request) {
        SolicitudServicio solicitud = new SolicitudServicio();
        Integer idCliente = entero(request.getParameter("idCliente"));
        solicitud.setIdCliente(idCliente == null ? 0 : idCliente);
        solicitud.setObservacion(limpiar(request.getParameter("observacion")));

        String[] idsVehiculo = request.getParameterValues("idVehiculo");
        String[] descripciones = request.getParameterValues("descripcionServicio");
        String[] origenes = request.getParameterValues("origen");
        String[] destinos = request.getParameterValues("destino");
        String[] fechas = request.getParameterValues("fechaServicio");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precioEstimado");
        int cantidad = minimo(idsVehiculo, descripciones, origenes, destinos, fechas, cantidades, precios);
        List<DetalleServicio> detalles = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            DetalleServicio detalle = new DetalleServicio();
            Integer idVehiculo = entero(idsVehiculo[i]);
            detalle.setIdVehiculo(idVehiculo);
            detalle.setDescripcionServicio(limpiar(descripciones[i]));
            detalle.setOrigen(limpiar(origenes[i]));
            detalle.setDestino(limpiar(destinos[i]));
            detalle.setFechaServicio(fecha(fechas[i]));
            detalle.setCantidad(decimal(cantidades[i]));
            detalle.setPrecioUnitario(decimal(precios[i]));
            detalles.add(detalle);
        }
        solicitud.setDetalles(detalles);
        return solicitud;
    }

    private String validar(SolicitudServicio solicitud) {
        if (solicitud.getIdCliente() <= 0) return "Selecciona un cliente válido.";
        if (solicitud.getObservacion() != null && solicitud.getObservacion().length() > 300) {
            return "La observación no puede superar 300 caracteres.";
        }
        if (solicitud.getDetalles().isEmpty()) return "Agrega al menos un servicio a la solicitud.";
        for (DetalleServicio detalle : solicitud.getDetalles()) {
            if (detalle.getIdVehiculo() == null || detalle.getIdVehiculo() <= 0) return "Selecciona el vehículo para cada servicio.";
            if (detalle.getDescripcionServicio().isBlank()) return "La descripción del servicio es obligatoria.";
            if (detalle.getDescripcionServicio().length() > 200
                    || supera(detalle.getOrigen(), 150) || supera(detalle.getDestino(), 150)) {
                return "Revisa la longitud de la descripción y las rutas.";
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().signum() <= 0
                    || detalle.getCantidad().precision() > 9 || detalle.getCantidad().scale() > 2) {
                return "La cantidad debe ser válida.";
            }
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().signum() < 0
                    || detalle.getPrecioUnitario().precision() > 9
                    || detalle.getPrecioUnitario().scale() > 2) return "El precio estimado debe ser válido.";
        }
        return null;
    }

    private void cambiarEstado(HttpServletRequest request, HttpSession session, String accion) {
        Integer id = entero(request.getParameter("idSolicitud"));
        if (id == null) {
            mensaje(session, "danger", "El identificador de la solicitud no es válido.");
            return;
        }
        String resultado = "aprobar".equals(accion) ? solicitudes.aprobar(id) : solicitudes.anular(id);
        if ("OK".equals(resultado)) mensaje(session, "success", "aprobar".equals(accion) ? "Solicitud aprobada." : "Solicitud anulada.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "La solicitud ya no admite esta acción.");
        else mensaje(session, "danger", "No se pudo actualizar la solicitud.");
    }

    private void convertir(HttpServletRequest request, HttpSession session, Usuario actual) {
        Integer id = entero(request.getParameter("idSolicitud"));
        if (id == null) {
            mensaje(session, "danger", "El identificador de la solicitud no es válido.");
            return;
        }
        int idVenta = solicitudes.convertirEnVenta(id, actual.getIdUsuario());
        if (idVenta > 0) mensaje(session, "success", "Solicitud convertida en venta N.º " + idVenta + ".");
        else if (idVenta == -1) mensaje(session, "warning", "Aprueba primero la solicitud o revisa si ya fue convertida.");
        else mensaje(session, "danger", "No se pudo convertir la solicitud en venta.");
    }

    private int minimo(String[]... arrays) {
        int minimo = Integer.MAX_VALUE;
        for (String[] array : arrays) minimo = Math.min(minimo, array == null ? 0 : array.length);
        return minimo == Integer.MAX_VALUE ? 0 : minimo;
    }

    private Integer entero(String value) {
        try { return Integer.valueOf(limpiar(value)); } catch (NumberFormatException e) { return null; }
    }

    private BigDecimal decimal(String value) {
        try { return new BigDecimal(limpiar(value)); } catch (NumberFormatException e) { return null; }
    }

    private LocalDateTime fecha(String value) {
        try { return value == null || value.isBlank() ? null : LocalDateTime.parse(value); }
        catch (RuntimeException e) { return null; }
    }

    private boolean supera(String value, int max) { return value != null && value.length() > max; }
    private String limpiar(String value) { return value == null ? "" : value.trim(); }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("solicitudFlashTipo", tipo);
        session.setAttribute("solicitudFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("solicitudFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("solicitudFlashTipo"));
            session.removeAttribute("solicitudFlashMensaje");
            session.removeAttribute("solicitudFlashTipo");
        }
    }
}
