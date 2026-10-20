package controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Componente;
import modelo.ComponenteRepository;
import modelo.ComponenteRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "ComponenteServlet", urlPatterns = {"/ComponenteServlet"})
public class ComponenteServlet extends HttpServlet {

    private final ComponenteRepository componentes = new ComponenteRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        List<Componente> lista = componentes.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar la lista de componentes.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                Componente componente = componentes.buscarPorId(Integer.parseInt(idEditar));
                if (componente != null) request.setAttribute("componenteEditar", componente);
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del componente no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("componentes", lista);
        request.getRequestDispatcher("componentes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        String accion = request.getParameter("accion");
        if ("crear".equals(accion)) {
            guardar(request, session, false);
        } else if ("editar".equals(accion)) {
            guardar(request, session, true);
        } else if ("eliminar".equals(accion)) {
            inactivar(request, session);
        } else {
            mensaje(session, "danger", "La acción solicitada no es válida.");
        }
        response.sendRedirect("ComponenteServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, boolean editar) {
        Componente componente = new Componente();
        componente.setNombre(limpiar(request.getParameter("nombre")));
        componente.setDescripcion(limpiar(request.getParameter("descripcion")));
        componente.setEstado(limpiar(request.getParameter("estado")));

        String error = validar(componente);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                componente.setIdComponente(Integer.parseInt(request.getParameter("idComponente")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del componente no es válido.");
                return;
            }
            resultado = componentes.editar(componente);
        } else {
            resultado = componentes.crear(componente);
        }

        if (ComponenteRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Componente actualizado." : "Componente registrado.");
        } else if (ComponenteRepository.DUPLICADO.equals(resultado)) {
            mensaje(session, "warning", "Ya existe un componente con ese nombre.");
        } else if (ComponenteRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró el componente.");
        } else {
            mensaje(session, "danger", "No se pudo guardar el componente. Revisa la conexión con la base de datos.");
        }
    }

    private String validar(Componente componente) {
        if (componente.getNombre().isBlank()) return "El nombre es obligatorio.";
        if (componente.getNombre().length() > 80 || componente.getDescripcion().length() > 250) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (!"Activo".equals(componente.getEstado()) && !"Inactivo".equals(componente.getEstado())) {
            return "Selecciona un estado válido.";
        }
        return null;
    }

    private void inactivar(HttpServletRequest request, HttpSession session) {
        try {
            String resultado = componentes.eliminar(Integer.parseInt(request.getParameter("idComponente")));
            if (ComponenteRepository.OK.equals(resultado)) {
                mensaje(session, "success", "Componente inactivado.");
            } else if (ComponenteRepository.NO_ENCONTRADO.equals(resultado)) {
                mensaje(session, "warning", "No se encontró el componente.");
            } else {
                mensaje(session, "danger", "No se pudo inactivar el componente.");
            }
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador del componente no es válido.");
        }
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
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar componentes.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("componenteFlashTipo", tipo);
        session.setAttribute("componenteFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("componenteFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("componenteFlashTipo"));
            session.removeAttribute("componenteFlashMensaje");
            session.removeAttribute("componenteFlashTipo");
        }
    }
}
