package controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Usuario;
import modelo.UsuarioCrudDao;

/**
 * Controlador del CRUD de usuarios.
 * GET: carga la lista y muestra dashboard.jsp.
 * POST: crear, editar, eliminar o desbloquear según el campo "accion".
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private final UsuarioCrudDao dao = new UsuarioCrudDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario yo = validarAdministrador(request, response, session);
        if (yo == null) {
            return;
        }

        List<Usuario> usuarios = dao.listar();
        if (usuarios == null) {
            request.setAttribute("mensaje", "No se pudo cargar la lista de usuarios. Intente nuevamente.");
            request.setAttribute("tipoMensaje", "danger");
            usuarios = new ArrayList<>();
        } else {
            recuperarMensaje(request, session);
        }

        int total = usuarios.size();
        int activos = 0;
        int bloqueados = 0;
        for (Usuario u : usuarios) {
            if (u.isActivo()) {
                activos++;
            }
            if (u.isBloqueado()) {
                bloqueados++;
            }
        }

        request.setAttribute("usuarios", usuarios);
        request.setAttribute("totalUsuarios", total);
        request.setAttribute("totalActivos", activos);
        request.setAttribute("totalBloqueados", bloqueados);
        request.setAttribute("porcentajeActivos", total == 0 ? 0 : (activos * 100) / total);
        request.setAttribute("rolActual", yo.getRolNombre());
        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario yo = validarAdministrador(request, response, session);
        if (yo == null) {
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        switch (accion) {
            case "crear":
                crear(request, session, yo);
                break;
            case "editar":
                editar(request, session, yo);
                break;
            case "eliminar":
                eliminar(request, session, yo);
                break;
            case "desbloquear":
                desbloquear(request, session);
                break;
            default:
                mensaje(session, "danger", "Acción no reconocida.");
                break;
        }
        // Patrón Post/Redirect/Get: evita reenviar el formulario al recargar
        response.sendRedirect("UsuarioServlet");
    }

    // ---------------------------------------------------------------- acciones

    private void crear(HttpServletRequest request, HttpSession session, Usuario yo) {
        String nombre = limpiar(request.getParameter("nombreUsuario"));
        String contrasena = request.getParameter("contrasena");
        String estado = request.getParameter("estado");
        Integer rolId = entero(request.getParameter("rol_id"));

        String error = validarNombre(nombre);
        if (error == null) {
            error = validarContrasena(contrasena);
        }
        if (error == null && (rolId == null || !estadoValido(estado))) {
            error = "Seleccione un rol y un estado válidos.";
        }
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado = dao.crear(nombre, contrasena, rolId, estado, yo.getNombreUsuario());
        if (UsuarioCrudDao.OK.equals(resultado)) {
            mensaje(session, "success", "Usuario \"" + nombre + "\" creado correctamente.");
        } else if (UsuarioCrudDao.DUPLICADO.equals(resultado)) {
            mensaje(session, "danger", "El nombre de usuario \"" + nombre + "\" ya existe.");
        } else {
            mensaje(session, "danger", "No se pudo crear el usuario. Verifique el rol e intente nuevamente.");
        }
    }

    private void editar(HttpServletRequest request, HttpSession session, Usuario yo) {
        Integer id = entero(request.getParameter("idUsuario"));
        Usuario destino = (id != null) ? dao.buscarPorId(id) : null;
        if (destino == null) {
            mensaje(session, "danger", "El usuario que intenta editar no existe.");
            return;
        }

        String nombre = limpiar(request.getParameter("nombreUsuario"));
        String contrasena = request.getParameter("contrasena");
        String estado = request.getParameter("estado");
        Integer rolId = entero(request.getParameter("rol_id"));

        String error = validarNombre(nombre);
        if (error == null && contrasena != null && !contrasena.isBlank()) {
            error = validarContrasena(contrasena);
        }
        if (error == null && (rolId == null || !estadoValido(estado))) {
            error = "Seleccione un rol y un estado válidos.";
        }
        boolean esYo = destino.getNombreUsuario().equals(yo.getNombreUsuario());
        if (error == null && esYo && (!"Activo".equals(estado) || rolId != destino.getRolId())) {
            error = "No puede desactivar su propia cuenta ni cambiar su propio rol.";
        }
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado = dao.editar(id, nombre, contrasena, rolId, estado);
        if (UsuarioCrudDao.OK.equals(resultado)) {
            if (esYo) {
                // Si el administrador cambió su propio nombre, se actualiza la sesión
                session.setAttribute("usuarioLogueado", nombre);
            }
            mensaje(session, "success", "Usuario \"" + nombre + "\" actualizado correctamente.");
        } else if (UsuarioCrudDao.DUPLICADO.equals(resultado)) {
            mensaje(session, "danger", "El nombre de usuario \"" + nombre + "\" ya existe.");
        } else {
            mensaje(session, "danger", "No se pudo actualizar el usuario. Intente nuevamente.");
        }
    }

    private void eliminar(HttpServletRequest request, HttpSession session, Usuario yo) {
        Integer id = entero(request.getParameter("idUsuario"));
        Usuario destino = (id != null) ? dao.buscarPorId(id) : null;
        if (destino == null) {
            mensaje(session, "danger", "El usuario que intenta eliminar no existe.");
            return;
        }
        if (destino.getNombreUsuario().equals(yo.getNombreUsuario())) {
            mensaje(session, "danger", "No puede eliminar su propia cuenta.");
            return;
        }
        if (UsuarioCrudDao.OK.equals(dao.eliminar(id))) {
            mensaje(session, "success", "Usuario \"" + destino.getNombreUsuario() + "\" eliminado.");
        } else {
            mensaje(session, "danger", "No se pudo eliminar el usuario. Intente nuevamente.");
        }
    }

    private void desbloquear(HttpServletRequest request, HttpSession session) {
        Integer id = entero(request.getParameter("idUsuario"));
        if (id != null && UsuarioCrudDao.OK.equals(dao.desbloquear(id))) {
            mensaje(session, "success", "Cuenta desbloqueada correctamente.");
        } else {
            mensaje(session, "danger", "No se pudo desbloquear la cuenta.");
        }
    }

    // ------------------------------------------------------------- utilidades

    /**
     * Comprueba que haya sesión y que el usuario sea administrador.
     * Si no, redirige o muestra el login y devuelve null.
     */
    private Usuario validarAdministrador(HttpServletRequest request, HttpServletResponse response,
                                         HttpSession session) throws ServletException, IOException {
        String actual = (session != null) ? (String) session.getAttribute("usuarioLogueado") : null;
        if (actual == null) {
            response.sendRedirect("login.jsp");
            return null;
        }
        Usuario yo = dao.buscarPorNombre(actual);
        if (yo == null || !yo.esAdministrador()) {
            session.invalidate();
            request.setAttribute("error", "Su cuenta no tiene permiso para gestionar usuarios.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return yo;
    }

    private String validarNombre(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            return "El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo.";
        }
        return null;
    }

    private String validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (contrasena.length() > 72) {
            return "La contraseña no puede superar los 72 caracteres.";
        }
        if (!contrasena.matches(".*[^A-Za-z].*")) {
            return "La contraseña debe incluir al menos un número o un símbolo.";
        }
        return null;
    }

    private boolean estadoValido(String estado) {
        return "Activo".equals(estado) || "Inactivo".equals(estado);
    }

    private String limpiar(String texto) {
        return texto == null ? null : texto.trim();
    }

    private Integer entero(String texto) {
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Guarda un mensaje en la sesión para mostrarlo tras la redirección. */
    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("flashTipo", tipo);
        session.setAttribute("flashMensaje", texto);
    }

    /** Pasa el mensaje guardado en la sesión a la petición actual y lo borra. */
    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("flashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("flashTipo"));
            session.removeAttribute("flashMensaje");
            session.removeAttribute("flashTipo");
        }
    }
}