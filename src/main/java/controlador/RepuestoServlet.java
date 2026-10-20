package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import modelo.Repuesto;
import modelo.RepuestoRepository;
import modelo.RepuestoRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "RepuestoServlet", urlPatterns = {"/RepuestoServlet"})
public class RepuestoServlet extends HttpServlet {

    private final RepuestoRepository repuestos = new RepuestoRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        List<Repuesto> lista = repuestos.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar la lista de repuestos.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                Repuesto repuesto = repuestos.buscarPorId(Integer.parseInt(idEditar));
                if (repuesto != null) request.setAttribute("repuestoEditar", repuesto);
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del repuesto no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("repuestos", lista);
        request.getRequestDispatcher("repuestos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        String accion = request.getParameter("accion");
        if ("crear".equals(accion)) guardar(request, session, actual, false);
        else if ("editar".equals(accion)) guardar(request, session, actual, true);
        else if ("eliminar".equals(accion)) inactivar(request, session);
        else mensaje(session, "danger", "La acción solicitada no es válida.");
        response.sendRedirect("RepuestoServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, Usuario actual, boolean editar) {
        Repuesto repuesto = leerRepuesto(request);
        String error = validar(repuesto);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                repuesto.setIdRepuesto(Integer.parseInt(request.getParameter("idRepuesto")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del repuesto no es válido.");
                return;
            }
            resultado = repuestos.editar(repuesto);
        } else {
            repuesto.setIdUsuarioRegistro(actual.getIdUsuario());
            resultado = repuestos.crear(repuesto);
        }

        if (RepuestoRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Repuesto actualizado." : "Repuesto registrado.");
        } else if (RepuestoRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró el repuesto.");
        } else {
            mensaje(session, "danger", "No se pudo guardar el repuesto. Revisa la conexión con la base de datos.");
        }
    }

    private void inactivar(HttpServletRequest request, HttpSession session) {
        try {
            String resultado = repuestos.eliminar(Integer.parseInt(request.getParameter("idRepuesto")));
            if (RepuestoRepository.OK.equals(resultado)) mensaje(session, "success", "Repuesto inactivado.");
            else if (RepuestoRepository.NO_ENCONTRADO.equals(resultado)) mensaje(session, "warning", "No se encontró el repuesto.");
            else mensaje(session, "danger", "No se pudo inactivar el repuesto.");
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador del repuesto no es válido.");
        }
    }

    private Repuesto leerRepuesto(HttpServletRequest request) {
        Repuesto repuesto = new Repuesto();
        repuesto.setCodigo(limpiar(request.getParameter("codigo")));
        repuesto.setNombre(limpiar(request.getParameter("nombre")));
        repuesto.setDescripcion(limpiar(request.getParameter("descripcion")));
        repuesto.setUnidadMedida(limpiar(request.getParameter("unidadMedida")));
        repuesto.setEstado(limpiar(request.getParameter("estado")));
        try {
            String stock = limpiar(request.getParameter("stockMinimo"));
            repuesto.setStockMinimo(stock.isBlank() ? BigDecimal.ZERO : new BigDecimal(stock));
        } catch (NumberFormatException e) {
            repuesto.setStockMinimo(null);
        }
        return repuesto;
    }

    private String validar(Repuesto repuesto) {
        if (repuesto.getCodigo().isBlank() || repuesto.getNombre().isBlank()
                || repuesto.getUnidadMedida().isBlank()) {
            return "El código, nombre y unidad de medida son obligatorios.";
        }
        if (supera(repuesto.getCodigo(), 40) || supera(repuesto.getNombre(), 120)
                || supera(repuesto.getDescripcion(), 300) || supera(repuesto.getUnidadMedida(), 20)) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (repuesto.getStockMinimo() == null || repuesto.getStockMinimo().signum() < 0) {
            return "El stock mínimo debe ser un número mayor o igual que cero.";
        }
        if (!"Activo".equals(repuesto.getEstado()) && !"Inactivo".equals(repuesto.getEstado())) {
            return "Selecciona un estado válido.";
        }
        return null;
    }

    private boolean supera(String valor, int maximo) { return valor != null && valor.length() > maximo; }

    private Usuario validarAdministrador(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
            throws ServletException, IOException {
        String nombre = session == null ? null : (String) session.getAttribute("usuarioLogueado");
        if (nombre == null) {
            response.sendRedirect("login.jsp");
            return null;
        }
        Usuario actual = usuarios.buscarPorNombre(nombre);
        if (actual == null || !actual.esAdministrador()) {
            session.invalidate();
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar repuestos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("repuestoFlashTipo", tipo);
        session.setAttribute("repuestoFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("repuestoFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("repuestoFlashTipo"));
            session.removeAttribute("repuestoFlashMensaje");
            session.removeAttribute("repuestoFlashTipo");
        }
    }
}
