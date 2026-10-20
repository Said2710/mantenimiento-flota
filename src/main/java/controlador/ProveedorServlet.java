package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.Proveedor;
import modelo.ProveedorRepository;
import modelo.ProveedorRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "ProveedorServlet", urlPatterns = {"/ProveedorServlet"})
public class ProveedorServlet extends HttpServlet {

    private final ProveedorRepository proveedores = new ProveedorRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        List<Proveedor> lista = proveedores.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar la lista de proveedores.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                Proveedor proveedor = proveedores.buscarPorId(Integer.parseInt(idEditar));
                if (proveedor != null) request.setAttribute("proveedorEditar", proveedor);
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del proveedor no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("proveedores", lista);
        request.getRequestDispatcher("proveedores.jsp").forward(request, response);
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
        response.sendRedirect("ProveedorServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, Usuario actual, boolean editar) {
        Proveedor proveedor = leerProveedor(request);
        String error = validar(proveedor);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                proveedor.setIdProveedor(Integer.parseInt(request.getParameter("idProveedor")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del proveedor no es válido.");
                return;
            }
            resultado = proveedores.editar(proveedor);
        } else {
            proveedor.setIdUsuarioRegistro(actual.getIdUsuario());
            resultado = proveedores.crear(proveedor);
        }

        if (ProveedorRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Proveedor actualizado." : "Proveedor registrado.");
        } else if (ProveedorRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró el proveedor.");
        } else {
            mensaje(session, "danger", "No se pudo guardar el proveedor. Revisa la conexión con la base de datos.");
        }
    }

    private void inactivar(HttpServletRequest request, HttpSession session) {
        try {
            String resultado = proveedores.eliminar(Integer.parseInt(request.getParameter("idProveedor")));
            if (ProveedorRepository.OK.equals(resultado)) mensaje(session, "success", "Proveedor inactivado.");
            else if (ProveedorRepository.NO_ENCONTRADO.equals(resultado)) mensaje(session, "warning", "No se encontró el proveedor.");
            else mensaje(session, "danger", "No se pudo inactivar el proveedor.");
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador del proveedor no es válido.");
        }
    }

    private Proveedor leerProveedor(HttpServletRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(limpiar(request.getParameter("razonSocial")));
        proveedor.setTipoProveedor(limpiar(request.getParameter("tipoProveedor")));
        proveedor.setNumeroDocumento(limpiar(request.getParameter("numeroDocumento")));
        proveedor.setContacto(limpiar(request.getParameter("contacto")));
        proveedor.setCorreo(limpiar(request.getParameter("correo")));
        proveedor.setTelefono(limpiar(request.getParameter("telefono")));
        proveedor.setDireccion(limpiar(request.getParameter("direccion")));
        proveedor.setEstado(limpiar(request.getParameter("estado")));
        return proveedor;
    }

    private String validar(Proveedor proveedor) {
        if (proveedor.getRazonSocial().isBlank()) {
            return "La razón social es obligatoria.";
        }
        if (!"TALLER".equals(proveedor.getTipoProveedor())
                && !"REPUESTOS".equals(proveedor.getTipoProveedor())
                && !"AMBOS".equals(proveedor.getTipoProveedor())) {
            return "Selecciona un tipo de proveedor válido.";
        }
        if (supera(proveedor.getRazonSocial(), 150) || supera(proveedor.getTipoProveedor(), 20)
                || supera(proveedor.getNumeroDocumento(), 20) || supera(proveedor.getContacto(), 100)
                || supera(proveedor.getCorreo(), 150) || supera(proveedor.getTelefono(), 30)
                || supera(proveedor.getDireccion(), 250)) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (!"Activo".equals(proveedor.getEstado()) && !"Inactivo".equals(proveedor.getEstado())) {
            return "Selecciona un estado válido.";
        }
        return null;
    }

    private boolean supera(String valor, int maximo) {
        return valor != null && valor.length() > maximo;
    }

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
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar proveedores.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("proveedorFlashTipo", tipo);
        session.setAttribute("proveedorFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("proveedorFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("proveedorFlashTipo"));
            session.removeAttribute("proveedorFlashMensaje");
            session.removeAttribute("proveedorFlashTipo");
        }
    }
}
