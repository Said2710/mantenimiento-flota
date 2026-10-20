package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import modelo.ClienteRepository;
import modelo.ClienteRepositoryJdbc;
import modelo.DetalleServicio;
import modelo.DocumentoVenta;
import modelo.DocumentoVentaRepository;
import modelo.DocumentoVentaRepositoryJdbc;
import modelo.VehiculoRepository;
import modelo.VehiculoRepositoryJdbc;
import modelo.VentaServicio;
import modelo.VentaServicioRepository;
import modelo.VentaServicioRepositoryJdbc;
import modelo.Usuario;

@WebServlet(name = "VentaServicioServlet", urlPatterns = {"/VentaServicioServlet"})
public class VentaServicioServlet extends AdministradorServletBase {

    private final VentaServicioRepository ventas = new VentaServicioRepositoryJdbc();
    private final DocumentoVentaRepository documentos = new DocumentoVentaRepositoryJdbc();
    private final ClienteRepository clientes = new ClienteRepositoryJdbc();
    private final VehiculoRepository vehiculos = new VehiculoRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Usuario actual = validarAdministrador(request, response);
        if (actual == null) return;
        HttpSession session = request.getSession(false);

        List<VentaServicio> lista = ventas.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar el registro de ventas.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }
        for (VentaServicio venta : lista) {
            List<DocumentoVenta> emitidos = documentos.listarPorVenta(venta.getIdVenta());
            venta.setDocumentos(emitidos == null ? new ArrayList<>() : emitidos);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            Integer id = entero(idEditar);
            if (id != null) {
                VentaServicio venta = ventas.buscarPorId(id);
                if (venta != null && ("Pendiente".equals(venta.getEstado())
                        || "Confirmada".equals(venta.getEstado()))) {
                    request.setAttribute("ventaEditar", venta);
                }
            } else {
                mensaje(session, "danger", "El identificador de la venta no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("ventas", lista);
        request.setAttribute("clientes", clientes.listar());
        request.setAttribute("vehiculos", vehiculos.listar());
        request.getRequestDispatcher("ventas-servicio.jsp").forward(request, response);
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
        else if ("documento".equals(accion)) emitirDocumento(request, session, actual);
        else if ("anularDocumento".equals(accion)) anularDocumento(request, session);
        else if ("anular".equals(accion)) anularVenta(request, session);
        else mensaje(session, "danger", "La acción solicitada no es válida.");
        response.sendRedirect("VentaServicioServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, Usuario actual, boolean editar) {
        VentaServicio venta = leerVenta(request);
        String error = validar(venta);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }
        String resultado;
        if (editar) {
            Integer id = entero(request.getParameter("idVenta"));
            if (id == null) {
                mensaje(session, "danger", "El identificador de la venta no es válido.");
                return;
            }
            venta.setIdVenta(id);
            resultado = ventas.editar(venta);
        } else {
            venta.setIdUsuarioRegistro(actual.getIdUsuario());
            resultado = ventas.crear(venta);
        }
        if ("OK".equals(resultado)) mensaje(session, "success", editar ? "Venta actualizada." : "Venta registrada.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "La venta ya no se puede editar.");
        else mensaje(session, "danger", "No se pudo guardar la venta. Revisa la conexión y las tablas.");
    }

    private VentaServicio leerVenta(HttpServletRequest request) {
        VentaServicio venta = new VentaServicio();
        Integer idCliente = entero(request.getParameter("idCliente"));
        venta.setIdCliente(idCliente == null ? 0 : idCliente);
        venta.setEstado(limpiar(request.getParameter("estado")));
        venta.setObservacion(limpiar(request.getParameter("observacion")));

        String[] idsVehiculo = request.getParameterValues("idVehiculo");
        String[] descripciones = request.getParameterValues("descripcionServicio");
        String[] origenes = request.getParameterValues("origen");
        String[] destinos = request.getParameterValues("destino");
        String[] fechas = request.getParameterValues("fechaServicio");
        String[] cantidades = request.getParameterValues("cantidad");
        String[] precios = request.getParameterValues("precioUnitario");
        int cantidad = minimo(idsVehiculo, descripciones, origenes, destinos, fechas, cantidades, precios);
        List<DetalleServicio> detalles = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            DetalleServicio detalle = new DetalleServicio();
            detalle.setIdVehiculo(entero(idsVehiculo[i]));
            detalle.setDescripcionServicio(limpiar(descripciones[i]));
            detalle.setOrigen(limpiar(origenes[i]));
            detalle.setDestino(limpiar(destinos[i]));
            detalle.setFechaServicio(fecha(fechas[i]));
            detalle.setCantidad(decimal(cantidades[i]));
            detalle.setPrecioUnitario(decimal(precios[i]));
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);
        return venta;
    }

    private String validar(VentaServicio venta) {
        if (venta.getIdCliente() <= 0) return "Selecciona un cliente válido.";
        if (venta.getObservacion() != null && venta.getObservacion().length() > 300) return "La observación no puede superar 300 caracteres.";
        if (!"Pendiente".equals(venta.getEstado()) && !"Confirmada".equals(venta.getEstado())) {
            return "Selecciona un estado válido para la venta.";
        }
        if (venta.getDetalles().isEmpty()) return "Agrega al menos un servicio a la venta.";
        for (DetalleServicio detalle : venta.getDetalles()) {
            if (detalle.getIdVehiculo() == null || detalle.getIdVehiculo() <= 0) return "Selecciona un vehículo para cada servicio.";
            if (detalle.getDescripcionServicio().isBlank()) return "La descripción del servicio es obligatoria.";
            if (detalle.getDescripcionServicio().length() > 200
                    || supera(detalle.getOrigen(), 150) || supera(detalle.getDestino(), 150)) return "Revisa la longitud de la descripción y las rutas.";
            if (detalle.getCantidad() == null || detalle.getCantidad().signum() <= 0
                    || detalle.getCantidad().precision() > 9 || detalle.getCantidad().scale() > 2) return "La cantidad debe ser válida.";
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().signum() < 0
                    || detalle.getPrecioUnitario().precision() > 9 || detalle.getPrecioUnitario().scale() > 2) return "El precio unitario debe ser válido.";
        }
        return null;
    }

    private void emitirDocumento(HttpServletRequest request, HttpSession session, Usuario actual) {
        Integer idVenta = entero(request.getParameter("idVenta"));
        String tipo = limpiar(request.getParameter("tipoDocumento"));
        String serie = limpiar(request.getParameter("serie"));
        String numero = limpiar(request.getParameter("numero"));
        String observacion = limpiar(request.getParameter("observacionDocumento"));
        if (idVenta == null || !("Factura".equals(tipo) || "Boleta".equals(tipo) || "Guia".equals(tipo))
                || serie.isBlank() || numero.isBlank()) {
            mensaje(session, "danger", "Completa el tipo, serie y número del documento.");
            return;
        }
        if (serie.length() > 10 || numero.length() > 20 || observacion.length() > 300) {
            mensaje(session, "danger", "El documento supera la longitud permitida.");
            return;
        }
        DocumentoVenta documento = new DocumentoVenta();
        documento.setIdVenta(idVenta);
        documento.setTipoDocumento(tipo);
        documento.setSerie(serie);
        documento.setNumero(numero);
        documento.setObservacion(observacion);
        String resultado = documentos.crear(documento, actual.getIdUsuario());
        if ("OK".equals(resultado)) mensaje(session, "success", tipo + " emitida y vinculada a la venta.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "La venta está anulada o ya tiene una factura/boleta emitida.");
        else mensaje(session, "danger", "No se pudo emitir el documento. Verifica que la serie y el número no estén repetidos.");
    }

    private void anularDocumento(HttpServletRequest request, HttpSession session) {
        Integer id = entero(request.getParameter("idDocumento"));
        if (id == null) {
            mensaje(session, "danger", "El identificador del documento no es válido.");
            return;
        }
        String resultado = documentos.anular(id);
        if ("OK".equals(resultado)) mensaje(session, "success", "Documento anulado.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "El documento ya estaba anulado.");
        else mensaje(session, "danger", "No se pudo anular el documento.");
    }

    private void anularVenta(HttpServletRequest request, HttpSession session) {
        Integer id = entero(request.getParameter("idVenta"));
        if (id == null) {
            mensaje(session, "danger", "El identificador de la venta no es válido.");
            return;
        }
        String resultado = ventas.anular(id);
        if ("OK".equals(resultado)) mensaje(session, "success", "Venta anulada. Sus documentos emitidos también se marcaron como anulados.");
        else if ("NO_EDITABLE".equals(resultado)) mensaje(session, "warning", "La venta ya estaba anulada.");
        else mensaje(session, "danger", "No se pudo anular la venta.");
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

    private java.time.LocalDateTime fecha(String value) {
        try { return value == null || value.isBlank() ? null : java.time.LocalDateTime.parse(value); }
        catch (RuntimeException e) { return null; }
    }

    private boolean supera(String value, int max) { return value != null && value.length() > max; }
    private String limpiar(String value) { return value == null ? "" : value.trim(); }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("ventaFlashTipo", tipo);
        session.setAttribute("ventaFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("ventaFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("ventaFlashTipo"));
            session.removeAttribute("ventaFlashMensaje");
            session.removeAttribute("ventaFlashTipo");
        }
    }
}
